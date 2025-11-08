package com.game.arkanoid.repository;

import com.game.arkanoid.config.DatabaseConfig;
import com.game.arkanoid.models.GameStateSnapshot;

import java.sql.*;
import java.util.Optional;

public final class GameStateRepository {

    public void upsertInProgress(int userId, GameStateSnapshot snap) throws SQLException {
        String select = "SELECT id FROM game_states WHERE user_id = ? AND in_progress = TRUE ORDER BY updated_at DESC LIMIT 1";
        try (Connection c = DatabaseConfig.getConnection()) {
            Integer existingId = null;
            try (PreparedStatement ps = c.prepareStatement(select)) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) existingId = rs.getInt(1);
                }
            }

            if (existingId == null) {
                insert(c, userId, snap);
            } else {
                update(c, existingId, snap);
            }
        }
    }

    public Optional<GameStateSnapshot> findLatestInProgress(int userId) throws SQLException {
        String sql = "SELECT current_level, score, lives, paddle_x, ball_x, ball_y, bricks::text, powerups::text FROM game_states WHERE user_id=? AND in_progress=TRUE ORDER BY updated_at DESC LIMIT 1";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    GameStateSnapshot s = new GameStateSnapshot();
                    s.currentLevel = rs.getInt("current_level");
                    s.score = rs.getInt("score");
                    s.lives = rs.getInt("lives");
                    s.paddleX = rs.getDouble("paddle_x");
                    s.ballX = rs.getDouble("ball_x");
                    s.ballY = rs.getDouble("ball_y");
                    // bricks/powerups decoded via simple JSON parser below
                    String bricksJson = rs.getString(7);
                    String powerupsJson = rs.getString(8);
                    Json.decodeBricks(bricksJson, s);
                    Json.decodePowerUps(powerupsJson, s);
                    return Optional.of(s);
                }
                return Optional.empty();
            }
        }
    }

    public void clearInProgressForUser(int userId) throws SQLException {
        String sql = "UPDATE game_states SET in_progress = FALSE WHERE user_id = ?";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        }
    }

    private void insert(Connection c, int userId, GameStateSnapshot s) throws SQLException {
        String sql = "INSERT INTO game_states (user_id, current_level, score, lives, paddle_x, ball_x, ball_y, bricks, powerups, in_progress, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?::jsonb, ?::jsonb, TRUE, CURRENT_TIMESTAMP)";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, userId, s);
            ps.executeUpdate();
        }
    }

    private void update(Connection c, int id, GameStateSnapshot s) throws SQLException {
        String sql = "UPDATE game_states SET current_level=?, score=?, lives=?, paddle_x=?, ball_x=?, ball_y=?, bricks=?::jsonb, powerups=?::jsonb, in_progress=TRUE, updated_at=CURRENT_TIMESTAMP WHERE id=?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            int idx = bindCore(ps, s);
            ps.setString(idx++, Json.encodeBricks(s));
            ps.setString(idx++, Json.encodePowerUps(s));
            ps.setInt(idx, id);
            ps.executeUpdate();
        }
    }

    private void bind(PreparedStatement ps, int userId, GameStateSnapshot s) throws SQLException {
        ps.setInt(1, userId);
        int idx = bindCore(ps, s, 2);
        ps.setString(idx++, Json.encodeBricks(s));
        ps.setString(idx, Json.encodePowerUps(s));
    }

    private int bindCore(PreparedStatement ps, GameStateSnapshot s) throws SQLException { return bindCore(ps, s, 1); }

    private int bindCore(PreparedStatement ps, GameStateSnapshot s, int startIdx) throws SQLException {
        int i = startIdx;
        ps.setInt(i++, s.currentLevel);
        ps.setInt(i++, s.score);
        ps.setInt(i++, s.lives);
        ps.setDouble(i++, s.paddleX);
        ps.setDouble(i++, s.ballX);
        ps.setDouble(i++, s.ballY);
        return i;
    }

    /** Minimal JSON encoder/decoder for our snapshot (avoid external deps). */
    private static final class Json {
        static String encodeBricks(GameStateSnapshot s) {
            StringBuilder sb = new StringBuilder();
            sb.append('[');
            for (int i = 0; i < s.bricks.size(); i++) {
                GameStateSnapshot.BrickState b = s.bricks.get(i);
                if (i > 0) sb.append(',');
                sb.append('{')
                  .append("\"x\":").append(trim(b.x)).append(',')
                  .append("\"y\":").append(trim(b.y)).append(',')
                  .append("\"health\":").append(b.health)
                  .append('}');
            }
            sb.append(']');
            return sb.toString();
        }

        static String encodePowerUps(GameStateSnapshot s) {
            StringBuilder sb = new StringBuilder();
            sb.append('{').append("\"falling\":");
            sb.append('[');
            for (int i = 0; i < s.fallingPowerUps.size(); i++) {
                GameStateSnapshot.PowerUpState p = s.fallingPowerUps.get(i);
                if (i > 0) sb.append(',');
                sb.append('{')
                  .append("\"type\":\"").append(escape(p.type)).append("\",")
                  .append("\"x\":").append(trim(p.x)).append(',')
                  .append("\"y\":").append(trim(p.y)).append(',')
                  .append("\"collected\":").append(p.collected)
                  .append('}');
            }
            sb.append(']');
            sb.append('}');
            return sb.toString();
        }

        static void decodeBricks(String json, GameStateSnapshot out) {
            if (json == null || json.isBlank()) return;
            // Very small permissive parser for array of {x,y,health}
            String s = json.trim();
            if (!s.startsWith("[")) return;
            s = s.substring(1, s.lastIndexOf(']'));
            if (s.isBlank()) return;
            String[] objs = s.split("\\},\\s*\\{");
            for (String raw : objs) {
                String obj = raw.replace('{', ' ').replace('}', ' ').trim();
                double x = readDouble(obj, "\"x\"");
                double y = readDouble(obj, "\"y\"");
                int health = (int) readDouble(obj, "\"health\"");
                out.bricks.add(new GameStateSnapshot.BrickState(x, y, health));
            }
        }

        static void decodePowerUps(String json, GameStateSnapshot out) {
            if (json == null || json.isBlank()) return;
            String s = json.trim();
            int arrIdx = s.indexOf("\"falling\"");
            if (arrIdx < 0) return;
            int start = s.indexOf('[', arrIdx);
            int end = s.indexOf(']', start);
            if (start < 0 || end < 0) return;
            String arr = s.substring(start + 1, end);
            if (arr.isBlank()) return;
            String[] objs = arr.split("\\},\\s*\\{");
            for (String raw : objs) {
                String obj = raw.replace('{', ' ').replace('}', ' ').trim();
                String type = readString(obj, "\"type\"");
                double x = readDouble(obj, "\"x\"");
                double y = readDouble(obj, "\"y\"");
                boolean collected = readBoolean(obj, "\"collected\"");
                out.fallingPowerUps.add(new GameStateSnapshot.PowerUpState(type, x, y, collected));
            }
        }

        private static String escape(String s) { return s == null ? "" : s.replace("\\", "\\\\").replace("\"", "\\\""); }
        private static String trim(double d) { String s = Double.toString(d); return s.endsWith(".0") ? s.substring(0, s.length()-2) : s; }
        private static double readDouble(String src, String key) {
            int i = src.indexOf(key);
            if (i < 0) return 0;
            i = src.indexOf(':', i);
            int j = src.indexOf(',', i+1);
            if (j < 0) j = src.length();
            String sub = src.substring(i+1, j).replace(":", "").trim();
            try { return Double.parseDouble(sub); } catch (Exception e) { return 0; }
        }
        private static String readString(String src, String key) {
            int i = src.indexOf(key);
            if (i < 0) return "";
            i = src.indexOf('"', i + key.length());
            int j = src.indexOf('"', i + 1);
            if (i < 0 || j < 0) return "";
            return src.substring(i + 1, j);
        }
        private static boolean readBoolean(String src, String key) {
            int i = src.indexOf(key);
            if (i < 0) return false;
            i = src.indexOf(':', i);
            int j = src.indexOf(',', i+1);
            if (j < 0) j = src.length();
            String sub = src.substring(i+1, j).replace(":", "").trim();
            return "true".equalsIgnoreCase(sub);
        }
    }
}


package com.game.arkanoid.repository;

import com.game.arkanoid.config.DatabaseConfig;
import com.game.arkanoid.models.GameStateSnapshot;

import java.sql.*;
import java.util.Optional;

public final class GameStateRepository {

    public void upsertInProgress(int userId, GameStateSnapshot snap) throws SQLException {
        try (Connection c = DatabaseConfig.getConnection()) {
            // Try update-by-user first (single-row-per-user policy)
            String updateByUser = com.game.arkanoid.utils.SqlLoader.load(
                "/com/game/arkanoid/sql/game_state/update_state_by_user.sql"
            );
            try (PreparedStatement ps = c.prepareStatement(updateByUser)) {
                int idx = bindCore(ps, snap); // sets 1..6
                ps.setString(idx++, Json.encodeBricks(snap));
                ps.setString(idx++, Json.encodePowerUps(snap));
                ps.setString(idx++, Json.encodeEnemies(snap));
                ps.setString(idx++, Json.encodeBalls(snap));
                ps.setString(idx++, Json.encodeEffects(snap)); // ✅ FIX: add idx++
                ps.setInt(idx, userId);                        // ✅ now has its own index
                int updated = ps.executeUpdate();
                if (updated > 0) return;
            }
            // No row existed → insert
            insert(c, userId, snap);
        }
    }

    public Optional<GameStateSnapshot> findLatestInProgress(int userId) throws SQLException {
        String sql = com.game.arkanoid.utils.SqlLoader.load(
            "/com/game/arkanoid/sql/game_state/select_latest_state.sql"
        );
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
                    s.paddleWidth = rs.getDouble("paddle_width");
                    s.ballX = rs.getDouble("ball_x");
                    s.ballY = rs.getDouble("ball_y");
                    s.ballDx = rs.getDouble("ball_dx");
                    s.ballDy = rs.getDouble("ball_dy");
                    s.ballMoving = rs.getBoolean("ball_moving");
                    s.ballDownward = rs.getBoolean("ball_downward");
                    s.ballStuck = rs.getBoolean("ball_stuck");
                    s.ballStuckOffsetX = rs.getDouble("ball_stuck_offset_x");
                    s.timeScale = rs.getDouble("time_scale");
                    s.laserCooldown = rs.getDouble("laser_cooldown");

                    // ✅ Read all JSON columns
                    String bricksJson = rs.getString("bricks");
                    String powerupsJson = rs.getString("powerups");
                    String enemiesJson = rs.getString("enemies");
                    String ballsJson = rs.getString("balls");
                    String effectsJson = rs.getString("effects");

                    // ✅ Decode JSON data
                    Json.decodeBricks(bricksJson, s);
                    Json.decodePowerUps(powerupsJson, s);
                    Json.decodeEnemies(enemiesJson, s);
                    Json.decodeBalls(ballsJson, s);
                    Json.decodeEffects(effectsJson, s);

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
        String sql = com.game.arkanoid.utils.SqlLoader.load(
            "/com/game/arkanoid/sql/game_state/insert_state.sql"
        );
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, userId, s);
            ps.executeUpdate();
        }
    }

    private void update(Connection c, int id, GameStateSnapshot s) throws SQLException {
        String sql = com.game.arkanoid.utils.SqlLoader.load(
            "/com/game/arkanoid/sql/game_state/update_state.sql"
        );
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            int idx = bindCore(ps, s);
            ps.setString(idx++, Json.encodeBricks(s));
            ps.setString(idx++, Json.encodePowerUps(s));
            ps.setString(idx++, Json.encodeEnemies(s));
            ps.setString(idx++, Json.encodeBalls(s));
            ps.setString(idx++, Json.encodeEffects(s)); // ✅ FIX: add idx++
            ps.setInt(idx, id);                         // ✅ correct index now
            ps.executeUpdate();
        }
    }

    private void bind(PreparedStatement ps, int userId, GameStateSnapshot s) throws SQLException {
        ps.setInt(1, userId);
        int idx = bindCore(ps, s, 2);
        ps.setString(idx++, Json.encodeBricks(s));
        ps.setString(idx++, Json.encodePowerUps(s));
        ps.setString(idx++, Json.encodeEnemies(s));
        ps.setString(idx++, Json.encodeBalls(s));
        ps.setString(idx++, Json.encodeEffects(s)); // ✅ FIX: add idx++
    }

    private int bindCore(PreparedStatement ps, GameStateSnapshot s) throws SQLException {
        return bindCore(ps, s, 1);
    }

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

        // ----------------------------
        // 🔹 BRICKS
        // ----------------------------
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

        static void decodeBricks(String json, GameStateSnapshot out) {
            if (json == null || json.isBlank()) return;
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

        // ----------------------------
        // 🔹 POWERUPS
        // ----------------------------
        static String encodePowerUps(GameStateSnapshot s) {
            StringBuilder sb = new StringBuilder();
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
            return sb.toString();
        }

        static void decodePowerUps(String json, GameStateSnapshot out) {
            if (json == null || json.isBlank()) return;
            String s = json.trim();
            if (!s.startsWith("[")) return;
            s = s.substring(1, s.lastIndexOf(']'));
            if (s.isBlank()) return;
            String[] objs = s.split("\\},\\s*\\{");
            for (String raw : objs) {
                String obj = raw.replace('{', ' ').replace('}', ' ').trim();
                String type = readString(obj, "\"type\"");
                double x = readDouble(obj, "\"x\"");
                double y = readDouble(obj, "\"y\"");
                boolean collected = readBoolean(obj, "\"collected\"");
                out.fallingPowerUps.add(new GameStateSnapshot.PowerUpState(type, x, y, collected));
            }
        }

        // ----------------------------
        // 🔹 ENEMIES (new separate column)
        // ----------------------------
        static String encodeEnemies(GameStateSnapshot s) {
            StringBuilder sb = new StringBuilder();
            sb.append('[');
            for (int i = 0; i < s.enemies.size(); i++) {
                GameStateSnapshot.EnemyState e = s.enemies.get(i);
                if (i > 0) sb.append(',');
                sb.append('{')
                .append("\"type\":\"").append(escape(e.type)).append("\",")
                .append("\"x\":").append(trim(e.x)).append(',')
                .append("\"y\":").append(trim(e.y)).append(',')
                .append("\"dx\":").append(trim(e.dx)).append(',')
                .append("\"dy\":").append(trim(e.dy))
                .append('}');
            }
            sb.append(']');
            return sb.toString();
        }

        static void decodeEnemies(String json, GameStateSnapshot out) {
            if (json == null || json.isBlank()) return;
            String s = json.trim();
            if (!s.startsWith("[")) return;
            s = s.substring(1, s.lastIndexOf(']'));
            if (s.isBlank()) return;
            String[] objs = s.split("\\},\\s*\\{");
            for (String raw : objs) {
                String obj = raw.replace('{', ' ').replace('}', ' ').trim();
                String type = readString(obj, "\"type\"");
                double x = readDouble(obj, "\"x\"");
                double y = readDouble(obj, "\"y\"");
                double dx = readDouble(obj, "\"dx\"");
                double dy = readDouble(obj, "\"dy\"");
                out.enemies.add(new GameStateSnapshot.EnemyState(type, x, y, dx, dy));
            }
        }

        // ----------------------------
        // 🔹 BALLS
        // ----------------------------
        static String encodeBalls(GameStateSnapshot s) {
            StringBuilder sb = new StringBuilder();
            sb.append('[');
            for (int i = 0; i < s.balls.size(); i++) {
                GameStateSnapshot.BallsState b = s.balls.get(i);
                if (i > 0) sb.append(',');
                sb.append('{')
                .append("\"x\":").append(trim(b.x)).append(',')
                .append("\"y\":").append(trim(b.y)).append(',')
                .append("\"dx\":").append(trim(b.dx)).append(',')
                .append("\"dy\":").append(trim(b.dy)).append(',')
                .append("\"moving\":").append(b.moving).append(',')
                .append("\"r\":").append(trim(b.radius))
                .append('}');
            }
            sb.append(']');
            return sb.toString();
        }

        static void decodeBalls(String json, GameStateSnapshot out) {
            if (json == null || json.isBlank()) return;
            String s = json.trim();
            if (!s.startsWith("[")) return;
            s = s.substring(1, s.lastIndexOf(']'));
            if (s.isBlank()) return;
            String[] objs = s.split("\\},\\s*\\{");
            for (String raw : objs) {
                String obj = raw.replace('{', ' ').replace('}', ' ').trim();
                double x = readDouble(obj, "\"x\"");
                double y = readDouble(obj, "\"y\"");
                double dx = readDouble(obj, "\"dx\"");
                double dy = readDouble(obj, "\"dy\"");
                boolean moving = readBoolean(obj, "\"moving\"");
                double r = readDouble(obj, "\"r\"");
                out.balls.add(new GameStateSnapshot.BallsState(x, y, dx, dy, moving, r));
            }
        }

        // ----------------------------
        // 🔹 EFFECTS
        // ----------------------------
        static String encodeEffects(GameStateSnapshot s) {
            StringBuilder sb = new StringBuilder();
            sb.append('[');
            for (int i = 0; i < s.activeEffects.size(); i++) {
                GameStateSnapshot.ActiveEffect ef = s.activeEffects.get(i);
                if (i > 0) sb.append(',');
                sb.append('{')
                .append("\"type\":\"").append(escape(ef.type)).append("\",")
                .append("\"rem\":").append(trim(ef.remaining))
                .append('}');
            }
            sb.append(']');
            return sb.toString();
        }

        static void decodeEffects(String json, GameStateSnapshot out) {
            if (json == null || json.isBlank()) return;
            String s = json.trim();
            if (!s.startsWith("[")) return;
            s = s.substring(1, s.lastIndexOf(']'));
            if (s.isBlank()) return;
            String[] objs = s.split("\\},\\s*\\{");
            for (String raw : objs) {
                String obj = raw.replace('{', ' ').replace('}', ' ').trim();
                String type = readString(obj, "\"type\"");
                double rem = readDouble(obj, "\"rem\"");
                out.activeEffects.add(new GameStateSnapshot.ActiveEffect(type, rem));
            }
        }

        // ----------------------------
        // 🔹 Utility helpers
        // ----------------------------
        private static String escape(String s) {
            return s == null ? "" : s.replace("\\", "\\\\").replace("\"", "\\\"");
        }
        private static String trim(double d) {
            String s = Double.toString(d);
            return s.endsWith(".0") ? s.substring(0, s.length() - 2) : s;
        }
        private static double readDouble(String src, String key) {
            int i = src.indexOf(key);
            if (i < 0) return 0;
            i = src.indexOf(':', i);
            int j = src.indexOf(',', i + 1);
            if (j < 0) j = src.length();
            String sub = src.substring(i + 1, j).replace(":", "").trim();
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
            int j = src.indexOf(',', i + 1);
            if (j < 0) j = src.length();
            String sub = src.substring(i + 1, j).replace(":", "").trim();
            return "true".equalsIgnoreCase(sub);
        }
    }
}

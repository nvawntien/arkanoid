package com.game.arkanoid.repository;

import com.game.arkanoid.config.DatabaseConfig;
import com.game.arkanoid.models.GameStateSnapshot;

import java.sql.*;
import java.util.Optional;

public final class GameStateRepository {

    public void upsertInProgress(int userId, GameStateSnapshot snap) throws SQLException {
        try (Connection c = DatabaseConfig.getConnection()) {
            // Try update-by-user first (single-row-per-user policy)
            String updateByUser = com.game.arkanoid.utils.SqlLoader.load("/com/game/arkanoid/sql/game_state/update_state_by_user.sql");
            try (PreparedStatement ps = c.prepareStatement(updateByUser)) {
                int idx = bindCore(ps, snap); // sets 1..6
                ps.setString(idx++, Json.encodeBricks(snap));
                ps.setString(idx++, Json.encodePowerUps(snap));
                ps.setString(idx++, Json.encodeBalls(snap));
                ps.setInt(idx, userId);
                int updated = ps.executeUpdate();
                if (updated > 0) return;
            }
            // No row existed → insert
            insert(c, userId, snap);
        }
    }

    public Optional<GameStateSnapshot> findLatestInProgress(int userId) throws SQLException {
        String sql = com.game.arkanoid.utils.SqlLoader.load("/com/game/arkanoid/sql/game_state/select_latest_state.sql");
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
                    String bricksJson = rs.getString("bricks");
                    String powerupsJson = rs.getString("powerups");
                    String ballsJson = rs.getString("balls");
                    String effectsJson = rs.getString("effects");
                    Json.decodeBricks(bricksJson, s);
                    Json.decodePowerUps(powerupsJson, s);
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
        String sql = com.game.arkanoid.utils.SqlLoader.load("/com/game/arkanoid/sql/game_state/insert_state.sql");
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, userId, s);
            ps.executeUpdate();
        }
    }

    private void update(Connection c, int id, GameStateSnapshot s) throws SQLException {
        String sql = com.game.arkanoid.utils.SqlLoader.load("/com/game/arkanoid/sql/game_state/update_state.sql");
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
        ps.setString(idx++, Json.encodePowerUps(s));
        ps.setString(idx, Json.encodeBalls(s));
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
            // unified balls list
            sb.append(',').append("\"balls\":");
            sb.append('[');
            for (int i = 0; i < s.balls.size(); i++) {
                GameStateSnapshot.BallsState bs = s.balls.get(i);
                if (i > 0) sb.append(',');
                sb.append('{')
                  .append("\"x\":").append(trim(bs.x)).append(',')
                  .append("\"y\":").append(trim(bs.y)).append(',')
                  .append("\"dx\":").append(trim(bs.dx)).append(',')
                  .append("\"dy\":").append(trim(bs.dy)).append(',')
                  .append("\"moving\":").append(bs.moving).append(',')
                  .append("\"r\":").append(trim(bs.radius))
                  .append('}');
            }
            sb.append(']');
            // balls (unified list) — preferred
            sb.append(',').append("\"balls\":");
            sb.append('[');
            for (int i = 0; i < s.balls.size(); i++) {
                GameStateSnapshot.BallsState bs = s.balls.get(i);
                if (i > 0) sb.append(',');
                sb.append('{')
                  .append("\"x\":").append(trim(bs.x)).append(',')
                  .append("\"y\":").append(trim(bs.y)).append(',')
                  .append("\"dx\":").append(trim(bs.dx)).append(',')
                  .append("\"dy\":").append(trim(bs.dy)).append(',')
                  .append("\"moving\":").append(bs.moving).append(',')
                  .append("\"r\":").append(trim(bs.radius))
                  .append('}');
            }
            sb.append(']');
            // extras (backward compatibility)
            sb.append(',').append("\"extras\":");
            sb.append('[');
            for (int i = 0; i < s.balls.size(); i++) {
                GameStateSnapshot.BallsState eb = s.balls.get(i);
                if (i > 0) sb.append(',');
                sb.append('{')
                  .append("\"x\":").append(trim(eb.x)).append(',')
                  .append("\"y\":").append(trim(eb.y)).append(',')
                  .append("\"dx\":").append(trim(eb.dx)).append(',')
                  .append("\"dy\":").append(trim(eb.dy)).append(',')
                  .append("\"moving\":").append(eb.moving).append(',')
                  .append("\"r\":").append(trim(eb.radius))
                  .append('}');
            }
            sb.append(']');
            // include ball velocity/moving info for resume
            sb.append(',').append("\"ball\":{")
              .append("\"dx\":").append(trim(s.ballDx)).append(',')
              .append("\"dy\":").append(trim(s.ballDy)).append(',')
              .append("\"moving\":").append(s.ballMoving).append(',')
              .append("\"down\":").append(s.ballDownward).append(',')
              .append("\"stuck\":").append(s.ballStuck).append(',')
              .append("\"offsetX\":").append(trim(s.ballStuckOffsetX))
              .append('}');
            // include active effects / timescale / laser cooldown / paddle width
            sb.append(',').append("\"effects\":");
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
            sb.append(',').append("\"timeScale\":").append(trim(s.timeScale));
            sb.append(',').append("\"laser\":").append(trim(s.laserCooldown));
            sb.append(',').append("\"paddleW\":").append(trim(s.paddleWidth));
            sb.append('}');
            return sb.toString();
        }

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
            // prefer balls[]; fallback to extras[] for backward compatibility
            int blIdx = s.indexOf("\"balls\"");
            if (blIdx >= 0) {
                int sx = s.indexOf('[', blIdx);
                int ex = s.indexOf(']', sx);
                if (sx > 0 && ex > sx) {
                    String arr2 = s.substring(sx + 1, ex);
                    if (!arr2.isBlank()) {
                        String[] objs2 = arr2.split("\\},\\s*\\{");
                        for (String raw2 : objs2) {
                            String obj = raw2.replace('{', ' ').replace('}', ' ').trim();
                            double x = readDouble(obj, "\"x\"");
                            double y = readDouble(obj, "\"y\"");
                            double dx = readDouble(obj, "\"dx\"");
                            double dy = readDouble(obj, "\"dy\"");
                            boolean moving = readBoolean(obj, "\"moving\"");
                            double r = readDouble(obj, "\"r\"");
                            out.balls.add(new GameStateSnapshot.BallsState(x, y, dx, dy, moving, r));
                        }
                    }
                }
            } else {
                // fallback: extras[] (old saves)
                int exIdx = s.indexOf("\"extras\"");
                if (exIdx >= 0) {
                    int sx2 = s.indexOf('[', exIdx);
                    int ex2 = s.indexOf(']', sx2);
                    if (sx2 > 0 && ex2 > sx2) {
                        String arr2 = s.substring(sx2 + 1, ex2);
                        if (!arr2.isBlank()) {
                            String[] objs2 = arr2.split("\\},\\s*\\{");
                            for (String raw2 : objs2) {
                                String obj = raw2.replace('{', ' ').replace('}', ' ').trim();
                                double x = readDouble(obj, "\"x\"");
                                double y = readDouble(obj, "\"y\"");
                                double dx = readDouble(obj, "\"dx\"");
                                double dy = readDouble(obj, "\"dy\"");
                                boolean moving = readBoolean(obj, "\"moving\"");
                                double r = readDouble(obj, "\"r\"");
                                out.balls.add(new GameStateSnapshot.BallsState(x, y, dx, dy, moving, r));
                            }
                        }
                    }
                }
            }
            // read ball info if present
            int ballIdx = s.indexOf("\"ball\"");
            if (ballIdx >= 0) {
                int bStart = s.indexOf('{', ballIdx);
                int bEnd = s.indexOf('}', bStart);
                if (bStart > 0 && bEnd > bStart) {
                    String obj = s.substring(bStart + 1, bEnd);
                    out.ballDx = readDouble(obj, "\"dx\"");
                    out.ballDy = readDouble(obj, "\"dy\"");
                    out.ballMoving = readBoolean(obj, "\"moving\"");
                    out.ballDownward = readBoolean(obj, "\"down\"");
                    out.ballStuck = readBoolean(obj, "\"stuck\"");
                    out.ballStuckOffsetX = readDouble(obj, "\"offsetX\"");
                }
            }

            // effects / timescale / laser / paddle width
            int effIdx = s.indexOf("\"effects\"");
            if (effIdx >= 0) {
                int eStart = s.indexOf('[', effIdx);
                int eEnd = s.indexOf(']', eStart);
                if (eStart > 0 && eEnd > eStart) {
                    String arr2 = s.substring(eStart + 1, eEnd);
                    if (!arr2.isBlank()) {
                        String[] objs2 = arr2.split("\\},\\s*\\{");
                        for (String raw2 : objs2) {
                            String obj = raw2.replace('{', ' ').replace('}', ' ').trim();
                            String type = readString(obj, "\"type\"");
                            double rem = readDouble(obj, "\"rem\"");
                            out.activeEffects.add(new GameStateSnapshot.ActiveEffect(type, rem));
                        }
                    }
                }
            }
            out.timeScale = readDouble(s, "\"timeScale\"");
            out.laserCooldown = readDouble(s, "\"laser\"");
            out.paddleWidth = readDouble(s, "\"paddleW\"");
        }

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
            String body = s.substring(1, s.lastIndexOf(']'));
            if (body.isBlank()) return;
            String[] objs = body.split("\\},\\s*\\{");
            for (String raw : objs) {
                String obj = raw.replace('{', ' ').replace('}', ' ').trim();
                String type = readString(obj, "\"type\"");
                double rem = readDouble(obj, "\"rem\"");
                out.activeEffects.add(new GameStateSnapshot.ActiveEffect(type, rem));
            }
        }

        static void decodeBalls(String json, GameStateSnapshot out) {
            if (json == null || json.isBlank()) return;
            String s = json.trim();
            if (!s.startsWith("[")) return;
            String body = s.substring(1, s.lastIndexOf(']'));
            if (body.isBlank()) return;
            String[] objs = body.split("\\},\\s*\\{");
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

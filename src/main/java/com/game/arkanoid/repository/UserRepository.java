package com.game.arkanoid.repository;

import com.game.arkanoid.config.DatabaseConfig;
import com.game.arkanoid.models.User;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.Optional;

public final class UserRepository {

    public Optional<User> findByName(String name) throws SQLException {
        String sql = "SELECT id, name, password, best_score, best_round, last_login FROM users WHERE name = ?";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapUser(rs));
                }
                return Optional.empty();
            }
        }
    }

    /** Case-insensitive lookup; returns one match if any (prefers highest bests). */
    public Optional<User> findByNameInsensitive(String name) throws SQLException {
        String sql = "SELECT id, name, password, best_score, best_round, last_login " +
                "FROM users WHERE LOWER(name) = LOWER(?) ORDER BY best_round DESC, best_score DESC LIMIT 1";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapUser(rs));
                return Optional.empty();
            }
        }
    }


    public void updatePasswordHash(int userId, String newHash) throws SQLException {
        String sql = "UPDATE users SET password = ? WHERE id = ?";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, newHash);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }

    /** Try to rename a user to a canonical name. May fail if unique constraint conflicts. */
    public boolean tryUpdateName(int userId, String newName) throws SQLException {
        String sql = "UPDATE users SET name = ? WHERE id = ?";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, newName);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            // If conflict due to UNIQUE(name), just ignore and keep existing
            return false;
        }
    }

    
    public User insert(String name, String passwordHash) throws SQLException {
        String sql = "INSERT INTO users (name, password) VALUES (?, ?) RETURNING id, name, password, best_score, best_round, last_login";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, passwordHash);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new SQLException("Insert user failed");
                return mapUser(rs);
            }
        }
    }

    public void updateBest(int userId, int bestRound, int bestScore) throws SQLException {
        String sql = "UPDATE users SET best_round = GREATEST(best_round, ?), best_score = GREATEST(best_score, ?), last_login = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, bestRound);
            ps.setInt(2, bestScore);
            ps.setInt(3, userId);
            ps.executeUpdate();
        }
    }

    private User mapUser(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String name = rs.getString("name");
        String pass = rs.getString("password");
        int bestScore = rs.getInt("best_score");
        int bestRound = rs.getInt("best_round");
        Timestamp ts = rs.getTimestamp("last_login");
        LocalDateTime lastLogin = ts != null ? ts.toLocalDateTime() : null;
        return new User(id, name, pass, bestScore, bestRound, lastLogin);
    }
}

package com.game.arkanoid.repository;

import com.game.arkanoid.config.DatabaseConfig;
import com.game.arkanoid.models.User;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Repository for user data.
 */
public final class UserRepository {

    /**
     * Find user by exact name.
     * @param name
     * @return
     * @throws SQLException
     */
    public Optional<User> findByName(String name) throws SQLException {
        String sql = com.game.arkanoid.utils.SqlLoader.load("/com/game/arkanoid/sql/user/select_user_by_name.sql");
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

    /**
     * Find user by name, case insensitive.
     * @param name
     * @return
     * @throws SQLException
     */
    public Optional<User> findByNameInsensitive(String name) throws SQLException {
        String sql = com.game.arkanoid.utils.SqlLoader.load("/com/game/arkanoid/sql/user/select_user_by_name_insensitive.sql");
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapUser(rs));
                return Optional.empty();
            }
        }
    }

    /**
     * Update password hash for user.
     * @param userId
     * @param newHash
     * @throws SQLException
     */
    public void updatePasswordHash(int userId, String newHash) throws SQLException {
        String sql = com.game.arkanoid.utils.SqlLoader.load("/com/game/arkanoid/sql/user/update_password.sql");
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, newHash);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }

    /**
     * Try to update user name.
     * @param userId
     * @param newName
     * @return
     * @throws SQLException
     */
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

    /**
     * Insert new user.
     * @param name
     * @param passwordHash
     * @return
     * @throws SQLException
     */
    public User insert(String name, String passwordHash) throws SQLException {
        String sql = com.game.arkanoid.utils.SqlLoader.load("/com/game/arkanoid/sql/user/insert_user.sql");
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

    /**
     * Update best score and round for user.
     * @param userId
     * @param bestRound
     * @param bestScore
     * @throws SQLException
     */
    public void updateBest(int userId, int bestRound, int bestScore) throws SQLException {
        String sql = com.game.arkanoid.utils.SqlLoader.load("/com/game/arkanoid/sql/user/update_best.sql");
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, bestRound);
            ps.setInt(2, bestScore);
            ps.setInt(3, userId);
            ps.executeUpdate();
        }
    }

    /**
     * Map ResultSet row to User object.
     * @param rs
     * @return
     * @throws SQLException
     */
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

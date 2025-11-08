package com.game.arkanoid.repository;

import com.game.arkanoid.config.DatabaseConfig;
import com.game.arkanoid.models.RankingEntry;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public final class ScoreRepository {

    public List<RankingEntry> fetchRankings(int limit) throws SQLException {
        // Aggregate duplicates that differ only by case, pick max stats
        String sql = "SELECT name_alias AS name, best_score, best_round FROM (" +
                "  SELECT LOWER(name) AS key, MAX(best_score) AS best_score, MAX(best_round) AS best_round, MIN(name) AS name_alias " +
                "  FROM users GROUP BY LOWER(name)" +
                ") t ORDER BY best_round DESC, best_score DESC LIMIT ?";
        List<RankingEntry> list = new ArrayList<>();
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, Math.max(1, limit));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new RankingEntry(
                            rs.getString("name"),
                            rs.getInt("best_score"),
                            rs.getInt("best_round")
                    ));
                }
            }
        }
        return list;
    }
}


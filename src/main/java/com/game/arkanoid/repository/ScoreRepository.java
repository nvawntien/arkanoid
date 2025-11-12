package com.game.arkanoid.repository;

import com.game.arkanoid.config.DatabaseConfig;
import com.game.arkanoid.models.RankingEntry;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public final class ScoreRepository {

    public List<RankingEntry> fetchRankings(int limit) throws SQLException {
        String sql = com.game.arkanoid.utils.SqlLoader.load("/com/game/arkanoid/sql/score/select_rankings.sql");
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


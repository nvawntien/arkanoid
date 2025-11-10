package com.game.arkanoid.services;

import com.game.arkanoid.config.DatabaseConfig;
import com.game.arkanoid.models.GameStateSnapshot;
import com.game.arkanoid.models.RankingEntry;
import com.game.arkanoid.models.User;
import com.game.arkanoid.repository.GameStateRepository;
import com.game.arkanoid.repository.ScoreRepository;
import com.game.arkanoid.repository.UserRepository;
import com.game.arkanoid.utils.PasswordHasher;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

/**
 * Handles all database operations on a background thread pool to keep JavaFX responsive.
 */
public final class DatabaseService {

    private final ExecutorService ioPool = Executors.newFixedThreadPool(2, r -> {
        Thread t = new Thread(r, "db-io");
        t.setDaemon(true);
        return t;
    });

    private final UserRepository users = new UserRepository();
    private final ScoreRepository scores = new ScoreRepository();
    private final GameStateRepository states = new GameStateRepository();

    /** No-op: schema is managed externally (e.g., Supabase). */
    public java.util.concurrent.CompletableFuture<Void> initializeSchema() {
        return java.util.concurrent.CompletableFuture.completedFuture(null);
    }

    public CompletableFuture<User> login(String username, String password) {
        return runAsync(() -> {
            try {
                String unameNorm = PasswordHasher.normalize(username);

                // 1) Try exact name first
                Optional<User> found = users.findByName(username);

                // 2) If not found, try case-insensitive match (merge duplicates-by-case)
                if (found.isEmpty()) {
                    found = users.findByNameInsensitive(username);
                }

                if (found.isPresent()) {
                    User u = found.get();
                    String computed = PasswordHasher.hash(unameNorm, password);
                    if (computed.equals(u.getPasswordHash())) {
                        // Optionally normalize stored name
                        if (!u.getName().equals(unameNorm)) {
                            users.tryUpdateName(u.getId(), unameNorm);
                        }
                        return u;
                    }
                    // Legacy plaintext support: if stored equals raw password, migrate
                    if (password != null && password.equals(u.getPasswordHash())) {
                        users.updatePasswordHash(u.getId(), computed);
                        if (!u.getName().equals(unameNorm)) {
                            users.tryUpdateName(u.getId(), unameNorm);
                        }
                        return new User(u.getId(), unameNorm, computed, u.getBestScore(), u.getBestRound(), u.getLastLogin());
                    }
                    throw new InvalidCredentialsException();
                }
                // Do NOT auto-create here; login only authenticates existing users
                throw new UserNotFoundException();
            } catch (SQLException e) {
                throw new CompletionException(e);
            }
        });
    }

    public CompletableFuture<Optional<GameStateSnapshot>> loadInProgressState(int userId) {
        return runAsync(() -> {
            try {
                return states.findLatestInProgress(userId);
            } catch (SQLException e) {
                throw new CompletionException(e);
            }
        });
    }

    public CompletableFuture<Void> saveInProgress(int userId, GameStateSnapshot snap) {
        return runAsync(() -> {
            try {
                states.upsertInProgress(userId, snap);
                return null;
            } catch (SQLException e) {
                throw new CompletionException(e);
            }
        });
    }

    public CompletableFuture<Void> clearInProgress(int userId) {
        return runAsync(() -> {
            try {
                states.clearInProgressForUser(userId);
                return null;
            } catch (SQLException e) {
                throw new CompletionException(e);
            }
        });
    }

    public CompletableFuture<Void> updateBest(int userId, int bestRound, int bestScore) {
        return runAsync(() -> {
            try {
                users.updateBest(userId, bestRound, bestScore);
                return null;
            } catch (SQLException e) {
                throw new CompletionException(e);
            }
        });
    }

    public CompletableFuture<List<RankingEntry>> getRankings(int limit) {
        return runAsync(() -> {
            try {
                return scores.fetchRankings(limit);
            } catch (SQLException e) {
                throw new CompletionException(e);
            }
        });
    }

    public CompletableFuture<User> signup(String username, String password) {
        return runAsync(() -> {
            try {
                String unameNorm = com.game.arkanoid.utils.PasswordHasher.normalize(username);
                // reject if exists (case-insensitive)
                if (users.findByNameInsensitive(unameNorm).isPresent()) {
                    throw new NameExistsException();
                }
                String hash = com.game.arkanoid.utils.PasswordHasher.hash(unameNorm, password);
                return users.insert(unameNorm, hash);
            } catch (SQLException e) {
                throw new CompletionException(e);
            }
        });
    }

    public void shutdown() { ioPool.shutdownNow(); }

    private <T> CompletableFuture<T> runAsync(Callable<T> task) {
        return CompletableFuture.supplyAsync(() -> {
            try { return task.call(); } catch (RuntimeException re) { throw re; } catch (Exception e) { throw new CompletionException(e); }
        }, ioPool);
    }

    // --- domain-specific exceptions ---
    public static final class InvalidCredentialsException extends RuntimeException {}
    public static final class UserNotFoundException extends RuntimeException {}
    public static final class NameExistsException extends RuntimeException {}
}

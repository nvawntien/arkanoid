package com.game.arkanoid.services;

import com.game.arkanoid.models.GameStateSnapshot;
import com.game.arkanoid.models.RankingEntry;
import com.game.arkanoid.models.User;
import com.game.arkanoid.repository.GameStateRepository;
import com.game.arkanoid.repository.ScoreRepository;
import com.game.arkanoid.repository.UserRepository;
import com.game.arkanoid.utils.PasswordHasher;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

/**
 * Handles all database operations asynchronously using a fixed thread pool
 * to keep the JavaFX UI responsive.
 * <p>
 * Provides methods for user authentication, registration, saving/loading
 * in-progress game states, updating best scores, and fetching rankings.
 */
public final class DatabaseService {

    /** Thread pool for performing database I/O tasks */
    private final ExecutorService ioPool = Executors.newFixedThreadPool(2, r -> {
        Thread t = new Thread(r, "db-io");
        t.setDaemon(true);
        return t;
    });

    private final UserRepository users = new UserRepository();
    private final ScoreRepository scores = new ScoreRepository();
    private final GameStateRepository states = new GameStateRepository();

    /**
     * Initializes the database schema if needed.
     * Currently a no-op because schema is managed externally.
     *
     * @return a completed CompletableFuture
     */
    public CompletableFuture<Void> initializeSchema() {
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Attempts to authenticate a user with a username and password.
     * Supports legacy plaintext passwords and normalizes username.
     *
     * @param username the user's username
     * @param password the user's password
     * @return a CompletableFuture resolving to the authenticated User
     * @throws InvalidCredentialsException if the password is incorrect
     * @throws UserNotFoundException if no user exists with the given username
     */
    public CompletableFuture<User> login(String username, String password) {
        return runAsync(() -> {
            try {
                String unameNorm = PasswordHasher.normalize(username);

                Optional<User> found = users.findByName(username);
                if (found.isEmpty()) {
                    found = users.findByNameInsensitive(username);
                }

                if (found.isPresent()) {
                    User u = found.get();
                    String computed = PasswordHasher.hash(unameNorm, password);
                    if (computed.equals(u.getPasswordHash())) {
                        if (!u.getName().equals(unameNorm)) {
                            users.tryUpdateName(u.getId(), unameNorm);
                        }
                        return u;
                    }
                    if (password != null && password.equals(u.getPasswordHash())) {
                        users.updatePasswordHash(u.getId(), computed);
                        if (!u.getName().equals(unameNorm)) {
                            users.tryUpdateName(u.getId(), unameNorm);
                        }
                        return new User(u.getId(), unameNorm, computed, u.getBestScore(), u.getBestRound(), u.getLastLogin());
                    }
                    throw new InvalidCredentialsException();
                }
                throw new UserNotFoundException();
            } catch (SQLException e) {
                throw new CompletionException(e);
            }
        });
    }

    /**
     * Loads the latest in-progress game state for a given user.
     *
     * @param userId ID of the user
     * @return a CompletableFuture resolving to an Optional containing the snapshot if present
     */
    public CompletableFuture<Optional<GameStateSnapshot>> loadInProgressState(int userId) {
        return runAsync(() -> {
            try {
                return states.findLatestInProgress(userId);
            } catch (SQLException e) {
                throw new CompletionException(e);
            }
        });
    }

    /**
     * Saves the current in-progress game state for a user.
     *
     * @param userId ID of the user
     * @param snap snapshot of the current game state
     * @return a CompletableFuture that completes when saving finishes
     */
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

    /**
     * Clears any in-progress game state for a user.
     *
     * @param userId ID of the user
     * @return a CompletableFuture that completes when clearing finishes
     */
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

    /**
     * Updates the best round and score for a user if higher than previous.
     *
     * @param userId ID of the user
     * @param bestRound best round achieved
     * @param bestScore best score achieved
     * @return a CompletableFuture that completes when update finishes
     */
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

    /**
     * Fetches top rankings up to a specified limit.
     *
     * @param limit maximum number of entries to return
     * @return a CompletableFuture resolving to a list of RankingEntry objects
     */
    public CompletableFuture<List<RankingEntry>> getRankings(int limit) {
        return runAsync(() -> {
            try {
                return scores.fetchRankings(limit);
            } catch (SQLException e) {
                throw new CompletionException(e);
            }
        });
    }

    /**
     * Registers a new user with the given username and password.
     *
     * @param username desired username
     * @param password desired password
     * @return a CompletableFuture resolving to the newly created User
     * @throws NameExistsException if a user with the same name already exists (case-insensitive)
     */
    public CompletableFuture<User> signup(String username, String password) {
        return runAsync(() -> {
            try {
                String unameNorm = PasswordHasher.normalize(username);
                if (users.findByNameInsensitive(unameNorm).isPresent()) {
                    throw new NameExistsException();
                }
                String hash = PasswordHasher.hash(unameNorm, password);
                return users.insert(unameNorm, hash);
            } catch (SQLException e) {
                throw new CompletionException(e);
            }
        });
    }

    /**
     * Shuts down the database I/O thread pool.
     */
    public void shutdown() { 
        ioPool.shutdownNow(); 
    }

    /**
     * Helper method to run a callable asynchronously in the I/O thread pool.
     *
     * @param task task to execute
     * @param <T> return type
     * @return a CompletableFuture that completes with the result or exception
     */
    private <T> CompletableFuture<T> runAsync(Callable<T> task) {
        return CompletableFuture.supplyAsync(() -> {
            try { 
                return task.call(); 
            } catch (RuntimeException re) { 
                throw re; 
            } catch (Exception e) { 
                throw new CompletionException(e); 
            }
        }, ioPool);
    }

    // --- Domain-specific exceptions ---
    
    /** Thrown when login fails due to invalid credentials */
    public static final class InvalidCredentialsException extends RuntimeException {}

    /** Thrown when no user is found for the provided username */
    public static final class UserNotFoundException extends RuntimeException {}

    /** Thrown when attempting to register a username that already exists */
    public static final class NameExistsException extends RuntimeException {}
}

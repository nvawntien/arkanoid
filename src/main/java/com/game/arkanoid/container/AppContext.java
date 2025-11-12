package com.game.arkanoid.container;

import com.game.arkanoid.models.User;
import com.game.arkanoid.services.DatabaseService;

/**
 * Global application context for non-game services and session state.
 */
public final class AppContext {
    private static AppContext instance;

    private final DatabaseService database = new DatabaseService();
    private User currentUser;

    /**
     * Get the singleton instance.
     * @return
     */
    public static synchronized AppContext getInstance() {
        if (instance == null) instance = new AppContext();
        return instance;
    }

    /**
     * Get the database service.
     * @return
     */
    public DatabaseService db() { return database; }

    /**
     * Get or set the current user.
     * @return
     */
    public User getCurrentUser() { return currentUser; }

    /**
     * Set the current user.
     * @param user
     */
    public void setCurrentUser(User user) { this.currentUser = user; }
}

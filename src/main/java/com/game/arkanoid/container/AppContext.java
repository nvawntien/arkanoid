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

    private AppContext() {
        // Ensure schema is ready before first login attempt
        database.initializeSchema().join();
    }

    public static synchronized AppContext getInstance() {
        if (instance == null) instance = new AppContext();
        return instance;
    }

    public DatabaseService db() { return database; }

    public User getCurrentUser() { return currentUser; }
    public void setCurrentUser(User user) { this.currentUser = user; }
}

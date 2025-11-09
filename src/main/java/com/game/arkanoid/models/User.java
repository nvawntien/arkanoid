package com.game.arkanoid.models;

import java.time.LocalDateTime;

/**
 * Represents an application user persisted in the database.
 */
public final class User {
    private final int id;
    private final String name;
    private final String passwordHash;
    private final int bestScore;
    private final int bestRound;
    private final LocalDateTime lastLogin;

    public User(int id, String name, String passwordHash, int bestScore, int bestRound, LocalDateTime lastLogin) {
        this.id = id;
        this.name = name;
        this.passwordHash = passwordHash;
        this.bestScore = bestScore;
        this.bestRound = bestRound;
        this.lastLogin = lastLogin;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getPasswordHash() { return passwordHash; }
    public int getBestScore() { return bestScore; }
    public int getBestRound() { return bestRound; }
    public LocalDateTime getLastLogin() { return lastLogin; }
}


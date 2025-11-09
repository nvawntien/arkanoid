package com.game.arkanoid.models;

/**
 * Row data for the Rankings view.
 */
public final class RankingEntry {
    private final String name;
    private final int bestScore;
    private final int bestRound;

    public RankingEntry(String name, int bestScore, int bestRound) {
        this.name = name;
        this.bestScore = bestScore;
        this.bestRound = bestRound;
    }

    public String getName() { return name; }
    public int getBestScore() { return bestScore; }
    public int getBestRound() { return bestRound; }
}


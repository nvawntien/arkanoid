package com.game.arkanoid.config;

import java.util.Objects;

/**
 * Centralised runtime settings selected from the UI.
 * Static helpers keep things simple while respecting the existing DI setup.
 */
public final class GameSettings {

    public enum Difficulty {
        EASY(0.8, 1.2),
        MEDIUM(1.0, 1.0),
        HARD(1.2, 0.9);

        private final double ballSpeedMultiplier;
        private final double paddleWidthMultiplier;

        Difficulty(double ballSpeedMultiplier, double paddleWidthMultiplier) {
            this.ballSpeedMultiplier = ballSpeedMultiplier;
            this.paddleWidthMultiplier = paddleWidthMultiplier;
        }

        public double ballSpeedMultiplier() {
            return ballSpeedMultiplier;
        }

        public double paddleWidthMultiplier() {
            return paddleWidthMultiplier;
        }
    }

    private static boolean soundEnabled = true;
    private static Difficulty difficulty = Difficulty.MEDIUM;

    private GameSettings() {
    }

    public static boolean isSoundEnabled() {
        return soundEnabled;
    }

    public static void setSoundEnabled(boolean enabled) {
        soundEnabled = enabled;
    }

    public static Difficulty getDifficulty() {
        return difficulty;
    }

    public static void setDifficulty(Difficulty newDifficulty) {
        difficulty = Objects.requireNonNull(newDifficulty, "difficulty");
    }

    public static double getBallSpeedMultiplier() {
        return difficulty.ballSpeedMultiplier();
    }

    public static double getPaddleWidthMultiplier() {
        return difficulty.paddleWidthMultiplier();
    }
}

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
    private static double masterVolume = 1.0;
    private static double musicVolume = 0.7;
    private static double sfxVolume = 0.8;
    private static Difficulty difficulty = Difficulty.MEDIUM;
    private static int highScore = 0;

    private GameSettings() {
    }

    public static boolean isSoundEnabled() {
        return soundEnabled;
    }

    public static void setSoundEnabled(boolean enabled) {
        soundEnabled = enabled;
    }

    public static double getMasterVolume() {
        return masterVolume;
    }

    public static void setMasterVolume(double value) {
        masterVolume = clamp01(value);
    }

    public static double getMusicVolume() {
        return musicVolume;
    }

    public static void setMusicVolume(double value) {
        musicVolume = clamp01(value);
    }

    public static double getSfxVolume() {
        return sfxVolume;
    }

    public static void setSfxVolume(double value) {
        sfxVolume = clamp01(value);
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

    public static int getHighScore() {
        return Math.max(0, highScore);
    }

    public static void setHighScore(int score) {
        highScore = Math.max(highScore, Math.max(0, score));
    }

    private static double clamp01(double value) {
        if (Double.isNaN(value)) {
            return 0.0;
        }
        return Math.max(0.0, Math.min(1.0, value));
    }
}

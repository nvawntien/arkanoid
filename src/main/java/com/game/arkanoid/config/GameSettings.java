package com.game.arkanoid.config;

import java.util.Objects;

/**
 * Centralised runtime settings selected from the UI.
 * Static helpers keep things simple while respecting the existing DI setup.
 */
public final class GameSettings {
    /**
     * Game difficulty levels.
     */
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

    /**
     * Is sound enabled?
     * @return
     */
    public static boolean isSoundEnabled() {
        return soundEnabled;
    }

    /**
     * Enable or disable sound.
     * @param enabled
     */
    public static void setSoundEnabled(boolean enabled) {
        soundEnabled = enabled;
    }

    /**
     * Get master volume.
     * @return
     */
    public static double getMasterVolume() {
        return masterVolume;
    }

    /**
     * Set master volume.
     * @param value
     */
    public static void setMasterVolume(double value) {
        masterVolume = clamp01(value);
    }

    /**
     * Get music volume.
     * @return
     */
    public static double getMusicVolume() {
        return musicVolume;
    }

    /**
     * Set music volume.
     * @param value
     */
    public static void setMusicVolume(double value) {
        musicVolume = clamp01(value);
    }

    /**
     * Get SFX volume.
     * @return
     */
    public static double getSfxVolume() {
        return sfxVolume;
    }

    /**
     * Set SFX volume.
     * @param value
     */
    public static void setSfxVolume(double value) {
        sfxVolume = clamp01(value);
    }

    /**
     * Get game difficulty.
     * @return
     */
    public static Difficulty getDifficulty() {
        return difficulty;
    }

    /**
     * Set game difficulty.
     * @param newDifficulty
     */
    public static void setDifficulty(Difficulty newDifficulty) {
        difficulty = Objects.requireNonNull(newDifficulty, "difficulty");
    }

    /**
     * Get ball speed multiplier based on difficulty.
     * @return
     */
    public static double getBallSpeedMultiplier() {
        return difficulty.ballSpeedMultiplier();
    }

    /**
     * Get paddle width multiplier based on difficulty.
     * @return
     */
    public static double getPaddleWidthMultiplier() {
        return difficulty.paddleWidthMultiplier();
    }

    /**
     * Get high score.
     * @return
     */
    public static int getHighScore() {
        return Math.max(0, highScore);
    }

    /**
     * Set high score.
     * @param score
     */
    public static void setHighScore(int score) {
        highScore = Math.max(highScore, Math.max(0, score));
    }

    /**
     * Clamp value between 0.0 and 1.0.
     * @param value
     * @return
     */
    private static double clamp01(double value) {
        if (Double.isNaN(value)) {
            return 0.0;
        }
        return Math.max(0.0, Math.min(1.0, value));
    }
}

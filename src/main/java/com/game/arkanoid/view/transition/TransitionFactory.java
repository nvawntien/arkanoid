package com.game.arkanoid.view.transition;

import javafx.util.Duration;

/**
 * Chooses suitable transition strategies depending on context.
 */
public final class TransitionFactory {
    /**
     * Transition for menu scene.
     * @return
     */
    public TransitionStrategy forMenuScene() {
        return new BlurFadeStrategy(Duration.millis(360), 28.0);
    }

    /**
     * Transition for game scene.
     * @return
     */
    public TransitionStrategy forGameScene() {
        return new FadeTransitionStrategy(Duration.millis(240), 0.0, 1.0);
    }

    /**
     * Transition for game over scene.
     * @return
     */
    public TransitionStrategy forGameOverScene() {
        return new BlurFadeStrategy(Duration.millis(420), 18.0);
    }

    /**
     * Transition for win scene.
     * @return
     */
    public TransitionStrategy forWinScene() {
        return new BlurFadeStrategy(Duration.millis(420), 18.0);
    }

    /**
     * Transition for settings scene.
     * @return
     */
    public TransitionStrategy forSettingsScene() {
        return new FadeTransitionStrategy(Duration.millis(220), 0.0, 1.0);
    }

    /**
     * Transition for level banner.
     * @return
     */
    public TransitionStrategy forLevelBanner() {
        return new SlideFadeStrategy(Duration.millis(320), 45.0);
    }

    /**
     * Transition for level scene.
     * @return
     */
    public TransitionStrategy forLevelScene() {
        return new FadeTransitionStrategy(Duration.millis(400), 0.0, 1.0);
    }
}

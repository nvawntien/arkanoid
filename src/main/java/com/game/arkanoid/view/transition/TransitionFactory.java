package com.game.arkanoid.view.transition;

import javafx.util.Duration;

/**
 * Chooses suitable transition strategies depending on context.
 */
public final class TransitionFactory {

    public TransitionStrategy forMenuScene() {
        return new BlurFadeStrategy(Duration.millis(360), 28.0);
    }

    public TransitionStrategy forGameScene() {
        return new FadeTransitionStrategy(Duration.millis(240), 0.0, 1.0);
    }

    public TransitionStrategy forGameOverScene() {
        return new BlurFadeStrategy(Duration.millis(420), 18.0);
    }

    public TransitionStrategy forSettingsScene() {
        return new FadeTransitionStrategy(Duration.millis(220), 0.0, 1.0);
    }

    public TransitionStrategy forLevelBanner() {
        return new SlideFadeStrategy(Duration.millis(320), 45.0);
    }
}

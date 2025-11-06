package com.game.arkanoid.view.transition;

import javafx.application.Platform;
import javafx.scene.Node;

/**
 * Facade for triggering transitions and exposing pre-configured strategies.
 */
public final class TransitionManager {

    private final TransitionFactory factory = new TransitionFactory();

    public TransitionStrategy menuTransition() {
        return factory.forMenuScene();
    }

    public TransitionStrategy gameTransition() {
        return factory.forGameScene();
    }

    public TransitionStrategy gameOverTransition() {
        return factory.forGameOverScene();
    }

    public TransitionStrategy settingsTransition() {
        return factory.forSettingsScene();
    }

    public TransitionStrategy levelBannerTransition() {
        return factory.forLevelBanner();
    }

    public void play(Node root, TransitionStrategy strategy, Runnable onFinished) {
        if (strategy == null || root == null) {
            if (onFinished != null) {
                onFinished.run();
            }
            return;
        }
        if (Platform.isFxApplicationThread()) {
            strategy.play(root, onFinished);
        } else {
            Platform.runLater(() -> strategy.play(root, onFinished));
        }
    }
}

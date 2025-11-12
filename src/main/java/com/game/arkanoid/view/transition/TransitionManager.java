package com.game.arkanoid.view.transition;

import javafx.application.Platform;
import javafx.scene.Node;

/**
 * Facade for triggering transitions and exposing pre-configured strategies.
 */
public final class TransitionManager {

    private final TransitionFactory factory = new TransitionFactory();

    /**
     * Menu transition.
     * @return
     */
    public TransitionStrategy menuTransition() {
        return factory.forMenuScene();
    }

    /**
     * Game transition.
     * @return
     */
    public TransitionStrategy gameTransition() {
        return factory.forGameScene();
    }

    /**
     * Game over transition.
     * @return
     */
    public TransitionStrategy gameOverTransition() {
        return factory.forGameOverScene();
    }

    /**
     * Win transition.
     * @return
     */
    public TransitionStrategy winTransition() {
        return factory.forWinScene();
    }

    /**
     * Settings transition.
     * @return
     */
    public TransitionStrategy settingsTransition() {
        return factory.forSettingsScene();
    }

    /**
     * Level banner transition.
     * @return
     */
    public TransitionStrategy levelBannerTransition() {
        return factory.forLevelBanner();
    }

    /**
     * Level scene transition.
     * @return
     */
    public TransitionStrategy levelTransition() {
        return factory.forLevelScene(); // gọi hàm mới trong TransitionFactory
    }

    /**
     * Play a transition on the given root node.
     * @param root
     * @param strategy
     * @param onFinished
     */
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

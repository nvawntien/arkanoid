package com.game.arkanoid.view.transition;

import javafx.animation.FadeTransition;
import javafx.scene.Node;
import javafx.util.Duration;

/**
 * Simple opacity fade-in transition used for subtle scene swaps.
 */
public class FadeTransitionStrategy implements TransitionStrategy {

    private final Duration duration;
    private final double fromValue;
    private final double toValue;

    public FadeTransitionStrategy() {
        this(Duration.millis(260), 0.0, 1.0);
    }

    public FadeTransitionStrategy(Duration duration, double fromValue, double toValue) {
        this.duration = duration;
        this.fromValue = fromValue;
        this.toValue = toValue;
    }

    @Override
    public void play(Node root, Runnable onFinished) {
        if (root == null) {
            if (onFinished != null) {
                onFinished.run();
            }
            return;
        }
        root.setOpacity(fromValue);
        FadeTransition ft = new FadeTransition(duration, root);
        ft.setFromValue(fromValue);
        ft.setToValue(toValue);
        ft.setOnFinished(e -> {
            if (onFinished != null) {
                onFinished.run();
            }
        });
        ft.play();
    }
}

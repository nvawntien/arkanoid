package com.game.arkanoid.view.transition;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.Node;
import javafx.scene.effect.GaussianBlur;
import javafx.util.Duration;

/**
 * Adds a transient blur while fading content in, emulating an OLED-style dissolve.
 */
public class BlurFadeStrategy implements TransitionStrategy {

    private final Duration duration;
    private final double maxBlurRadius;

    public BlurFadeStrategy() {
        this(Duration.millis(320), 24.0);
    }

    public BlurFadeStrategy(Duration duration, double maxBlurRadius) {
        this.duration = duration;
        this.maxBlurRadius = maxBlurRadius;
    }

    @Override
    public void play(Node root, Runnable onFinished) {
        if (root == null) {
            if (onFinished != null) {
                onFinished.run();
            }
            return;
        }
        GaussianBlur blur = new GaussianBlur(0);
        root.setEffect(blur);

        FadeTransition fade = new FadeTransition(duration, root);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);
        fade.setInterpolator(Interpolator.EASE_BOTH);

        Timeline blurTimeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(blur.radiusProperty(), maxBlurRadius)),
                new KeyFrame(duration, new KeyValue(blur.radiusProperty(), 0.0, Interpolator.EASE_OUT))
        );

        fade.setOnFinished(e -> {
            root.setEffect(null);
            if (onFinished != null) {
                onFinished.run();
            }
        });

        fade.play();
        blurTimeline.play();
    }
}

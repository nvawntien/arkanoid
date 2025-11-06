package com.game.arkanoid.view.transition;

import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.Node;
import javafx.util.Duration;

/**
 * Slides the root upward while fading in, useful for level banners.
 */
public class SlideFadeStrategy implements TransitionStrategy {

    private final Duration duration;
    private final double translateY;

    public SlideFadeStrategy() {
        this(Duration.millis(280), 40.0);
    }

    public SlideFadeStrategy(Duration duration, double translateY) {
        this.duration = duration;
        this.translateY = translateY;
    }

    @Override
    public void play(Node root, Runnable onFinished) {
        if (root == null) {
            if (onFinished != null) {
                onFinished.run();
            }
            return;
        }
        root.setOpacity(0.0);
        root.setTranslateY(translateY);

        TranslateTransition slide = new TranslateTransition(duration, root);
        slide.setFromY(translateY);
        slide.setToY(0.0);
        slide.setInterpolator(Interpolator.EASE_OUT);

        javafx.animation.FadeTransition fade = new javafx.animation.FadeTransition(duration, root);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);
        fade.setInterpolator(Interpolator.EASE_OUT);

        ParallelTransition parallel = new ParallelTransition(slide, fade);
        parallel.setOnFinished(e -> {
            root.setTranslateY(0.0);
            if (onFinished != null) {
                onFinished.run();
            }
        });
        parallel.play();
    }
}

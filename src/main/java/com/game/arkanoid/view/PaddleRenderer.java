package com.game.arkanoid.view;

import com.game.arkanoid.models.Paddle;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.util.Duration;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.Animation;

import java.util.ArrayList;
import java.util.List;

public final class PaddleRenderer implements Renderer<Paddle> {
    private final ImageView node;
    private final List<Image> introFrames = new ArrayList<>();
    private final List<Image> pulsateFrames = new ArrayList<>();

    private Timeline currentAnimation;
    private boolean introPlayed = false;

    public PaddleRenderer(Pane pane) {
        // Load intro frames
        for (int i = 1; i <= 15; i++) {
            String path = String.format("/com/game/arkanoid/images/paddle_materialize_%d.png", i);
            introFrames.add(new Image(getClass().getResourceAsStream(path)));
        }

        // Load pulsate frames
        for (int i = 1; i <= 4; i++) {
            String path = String.format("/com/game/arkanoid/images/paddle_pulsate_%d.png", i);
            pulsateFrames.add(new Image(getClass().getResourceAsStream(path)));
        }

        node = new ImageView(introFrames.get(0));
        node.setSmooth(true);
        pane.getChildren().add(node);
    }

    @Override
    public void render(Paddle paddle) { 
        node.setTranslateX(paddle.getX());
        node.setTranslateY(paddle.getY());
        node.setFitWidth(paddle.getWidth());
        node.setFitHeight(paddle.getHeight());
    }
    
    @Override
    public ImageView getNode() {
        return node;
    }

    // --- Animation controls ---
    public void playIntro() {
        if (introFrames.isEmpty()) return;
        playFrameSequence(introFrames, 80, false, () -> startPulsate());
    }

    public void startPulsate() {
        if (pulsateFrames.isEmpty()) return;
        playFrameSequence(pulsateFrames, 100, true, null);
    }

    public void stopAnimation() {
        if (currentAnimation != null) currentAnimation.stop();
    }

    private void playFrameSequence(List<Image> frames, double frameDurationMs, boolean loop, Runnable onFinished) {
        if (currentAnimation != null) currentAnimation.stop();

        currentAnimation = new Timeline();
        for (int i = 0; i < frames.size(); i++) {
            Image frame = frames.get(i);
            currentAnimation.getKeyFrames().add(
                new KeyFrame(Duration.millis(i * frameDurationMs), e -> node.setImage(frame))
            );
        }

        if (loop) {
            currentAnimation.setCycleCount(Animation.INDEFINITE);
        } else {
            currentAnimation.setCycleCount(1);
            currentAnimation.setOnFinished(e -> {
                if (onFinished != null) onFinished.run();
            });
        }

        currentAnimation.playFromStart();
    }
}

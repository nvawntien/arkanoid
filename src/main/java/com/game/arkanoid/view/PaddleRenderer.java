package com.game.arkanoid.view;

import com.game.arkanoid.models.Paddle;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.util.Duration;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.Animation;
import javafx.animation.AnimationTimer;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

public final class PaddleRenderer implements Renderer<Paddle> {
    private final ImageView node;
    private final List<Image> introFrames = new ArrayList<>();
    private final List<Image> pulsateFrames = new ArrayList<>();
    private final List<Image> wideFrames = new ArrayList<>();
    private final List<Image> shrinkFrames = new ArrayList<>();
    private final List<Image> widePulsateFrames = new ArrayList<>();
    private final List<Image> laserFrames = new ArrayList<>();
    private final List<Image> laserPulsateFrames = new ArrayList<>();
    private Timeline currentAnimation;
    private AnimationTimer expandTimer, shrinkTimer, laserTimer;
    private double elapsedExpand = 0;
    private double elapsedShrink = 0;
    private double elapsedLaser = 0;
    private boolean isTransforming = false; // đang expand hoặc shrink

    public PaddleRenderer(Pane pane) {
        // Load intro frames
        for (int i = 1; i <= 15; i++) {
            introFrames.add(new Image(getClass().getResourceAsStream(
                String.format("/com/game/arkanoid/images/paddle_materialize_%d.png", i)
            )));
        }

        // Load pulsate frames
        for (int i = 1; i <= 4; i++) {
            pulsateFrames.add(new Image(getClass().getResourceAsStream(
                String.format("/com/game/arkanoid/images/paddle_pulsate_%d.png", i)
            )));
        }

        // Load wide frames
        for (int i = 1; i <= 9; i++) {
            wideFrames.add(new Image(getClass().getResourceAsStream(
                String.format("/com/game/arkanoid/images/paddle_wide_%d.png", i)
            )));
        }

        // Load laser frames
        for (int i = 1; i <= 16; i++) {
            laserFrames.add(new Image(getClass().getResourceAsStream(
                String.format("/com/game/arkanoid/images/paddle_laser_%d.png", i)
            )));
        }

        // Load laser pulsate frames
        for (int i = 1; i <= 4; i++) {
            laserPulsateFrames.add(new Image(getClass().getResourceAsStream(
                String.format("/com/game/arkanoid/images/paddle_laser_pulsate_%d.png", i)
            )));
        }

        // shrink là wide đảo ngược
        shrinkFrames.addAll(wideFrames);
        Collections.reverse(shrinkFrames);

        // Load wide pulsate frames
        for (int i = 1; i <= 4; i++) {
            widePulsateFrames.add(new Image(getClass().getResourceAsStream(
                String.format("/com/game/arkanoid/images/paddle_wide_pulsate_%d.png", i)
            )));
        }

        node = new ImageView(introFrames.get(0));
        node.setSmooth(true);
        pane.getChildren().add(node);
    }

    @Override
    public void render(Paddle paddle) { 
        // 🟢 Chỉ cập nhật vị trí, KHÔNG ép kích thước trong lúc animation đang chạy
        node.setTranslateX(paddle.getX());
        node.setTranslateY(paddle.getY());

        if (!isTransforming) {
            // Chỉ update kích thước khi paddle ở trạng thái tĩnh (normal/pulsate)
            node.setFitWidth(paddle.getWidth());
            node.setFitHeight(paddle.getHeight());
        }
    }
    
    @Override
    public ImageView getNode() {
        return node;
    }

    // --- Animation controls ---
    public void playIntro() {
        if (introFrames.isEmpty()) return;
        playFrameSequence(introFrames, 80, false, this::startPulsate);
    }

    public void startPulsate() {
        if (pulsateFrames.isEmpty()) return;
        isTransforming = false;
        playFrameSequence(pulsateFrames, 100, true, null);
    }

    public void playExpand(Runnable onFinished) {
        stopAnimation();

        final int FRAME_COUNT = wideFrames.size();
        final double FRAME_DURATION = 0.02;
        final double TOTAL_DURATION = FRAME_COUNT * FRAME_DURATION;
        elapsedExpand = 0;
        isTransforming = true;

        expandTimer = new AnimationTimer() {
            private long lastTime = 0;
            @Override
            public void handle(long now) {
                if (lastTime == 0) {
                    lastTime = now;
                    return;
                }

                double delta = (now - lastTime) / 1e9;
                lastTime = now;
                elapsedExpand += delta;

                int currentFrame = Math.min((int)(elapsedExpand / FRAME_DURATION), FRAME_COUNT - 1);
                Image frame = wideFrames.get(currentFrame);
                node.setImage(frame);
                node.setFitWidth(frame.getWidth());
                node.setFitHeight(frame.getHeight());

                if (elapsedExpand >= TOTAL_DURATION) {
                    stop();
                    isTransforming = false;
                    startWidePulsate();
                    if (onFinished != null) onFinished.run();
                }
            }
        };
        expandTimer.start();
    }

    public void startWidePulsate() {
        if (widePulsateFrames.isEmpty()) return;
        isTransforming = false;
        playFrameSequence(widePulsateFrames, 100, true, null);
    }

    public void playShrink(Runnable onFinished) {
        stopAnimation();

        final int FRAME_COUNT = shrinkFrames.size();
        final double FRAME_DURATION = 0.02;
        final double TOTAL_DURATION = FRAME_COUNT * FRAME_DURATION;
        elapsedShrink = 0;
        isTransforming = true;

        shrinkTimer = new AnimationTimer() {
            private long lastTime = 0;
            @Override
            public void handle(long now) {
                if (lastTime == 0) {
                    lastTime = now;
                    return;
                }

                double delta = (now - lastTime) / 1e9;
                lastTime = now;
                elapsedShrink += delta;

                int currentFrame = Math.min((int)(elapsedShrink / FRAME_DURATION), FRAME_COUNT - 1);
                Image frame = shrinkFrames.get(currentFrame);
                node.setImage(frame);
                node.setFitWidth(frame.getWidth());
                node.setFitHeight(frame.getHeight());

                if (elapsedShrink >= TOTAL_DURATION) {
                    stop();
                    isTransforming = false;
                    startPulsate();
                    if (onFinished != null) onFinished.run();
                }
            }
        };
        shrinkTimer.start();
    }

    public void playLaser(Runnable onFinished) {
        stopAnimation();

        final int FRAME_COUNT = laserFrames.size();
        final double FRAME_DURATION = 0.08;
        final double TOTAL_DURATION = FRAME_COUNT * FRAME_DURATION;
        elapsedLaser = 0;
        isTransforming = true;

        laserTimer = new AnimationTimer() {
            private long lastTime = 0;
            @Override
            public void handle(long now) {
                if (lastTime == 0) {
                    lastTime = now;
                    return;
                }

                double delta = (now - lastTime) / 1e9;
                lastTime = now;
                elapsedLaser += delta;

                int currentFrame = Math.min((int)(elapsedLaser / FRAME_DURATION), FRAME_COUNT - 1);
                Image frame = laserFrames.get(currentFrame);
                node.setImage(frame);
                node.setFitWidth(frame.getWidth());
                node.setFitHeight(frame.getHeight());

                if (elapsedLaser >= TOTAL_DURATION) {
                    stop();
                    isTransforming = false;
                    startLaserPulsate();
                    if (onFinished != null) onFinished.run();
                }
            }
        };
        laserTimer.start();
    }

    public void startLaserPulsate() {
        if (laserPulsateFrames.isEmpty()) return;
        isTransforming = false;
        playFrameSequence(laserPulsateFrames, 100, true, null);
    }

    public void stopAnimation() {
        if (currentAnimation != null) currentAnimation.stop();
        if (expandTimer != null) expandTimer.stop();
        if (shrinkTimer != null) shrinkTimer.stop();
        if (laserTimer != null) laserTimer.stop();
    }

    private void playFrameSequence(List<Image> frames, double frameDurationMs, boolean loop, Runnable onFinished) {
        if (currentAnimation != null) currentAnimation.stop();

        currentAnimation = new Timeline();
        for (int i = 0; i < frames.size(); i++) {
            Image frame = frames.get(i);
            currentAnimation.getKeyFrames().add(
                new KeyFrame(Duration.millis(i * frameDurationMs), e -> {
                    node.setImage(frame);
                    node.setFitWidth(frame.getWidth());
                    node.setFitHeight(frame.getHeight());
                })
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

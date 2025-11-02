package com.game.arkanoid.view;

import com.game.arkanoid.models.Paddle;
import com.game.arkanoid.models.PowerUp;
import com.game.arkanoid.utils.Constants;

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
import java.util.Map;
import java.util.Collections;

public final class PaddleRenderer implements Renderer<Paddle> {
    private final ImageView node;
    private final List<Image> introFrames = new ArrayList<>();
    private final List<Image> pulsateFrames = new ArrayList<>();
    private final List<Image> wideFrames = new ArrayList<>();
    private final List<Image> shrinkFrames = new ArrayList<>();
    private final List<Image> widePulsateFrames = new ArrayList<>();
    private Timeline currentAnimation;
    private boolean introPlayed = false;
    private double elapsedExpand = 0;
    private double elapsedShrink = 0;

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

        // Load wide frames
        for (int i = 1; i <= 9; i++) {
            String path = String.format("/com/game/arkanoid/images/paddle_wide_%d.png", i);
            wideFrames.add(new Image(getClass().getResourceAsStream(path)));
        }

        // Load shrink frames
        shrinkFrames.addAll(wideFrames);
        Collections.reverse(shrinkFrames);

        // Load wide pulsate frames
        for (int i = 1; i <= 4; i++) {
            String path = String.format("/com/game/arkanoid/images/paddle_wide_pulsate_%d.png", i);
            widePulsateFrames.add(new Image(getClass().getResourceAsStream(path)));
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

    public void playExpand(Runnable onFinished) {
        stopAnimation();

        final int FRAME_COUNT = wideFrames.size();   // số frame mở rộng
        final double FRAME_DURATION = 0.08;          // thời gian 1 frame (giây)
        final double TOTAL_DURATION = FRAME_COUNT * FRAME_DURATION;
        elapsedExpand = 0;

        AnimationTimer expandTimer = new AnimationTimer() {
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

                // Tính frame hiện tại
                int currentFrame = Math.min((int)(elapsedExpand / FRAME_DURATION), FRAME_COUNT - 1);

                // Cập nhật hình ảnh
                node.setImage(wideFrames.get(currentFrame));

                // Khi đã tới frame cuối cùng
                if (elapsedExpand >= TOTAL_DURATION) {
                    stop();
                    startWidePulsate(); // chuyển sang animation pulsate
                    if (onFinished != null) onFinished.run();
                }
            }
        };

        expandTimer.start();
    }



    public void startWidePulsate() {
        if (widePulsateFrames.isEmpty()) return;
        playFrameSequence(widePulsateFrames, 100, true, null);
    }

    public void playShrink(Runnable onFinished) {
        stopAnimation();

        final int FRAME_COUNT = shrinkFrames.size();   // số frame thu nhỏ
        final double FRAME_DURATION = 0.08;          // thời gian 1 frame (giây)
        final double TOTAL_DURATION = FRAME_COUNT * FRAME_DURATION;
        elapsedShrink = 0;

        AnimationTimer shrinkTimer = new AnimationTimer() {
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

                // Tính frame hiện tại
                int currentFrame = Math.min((int)(elapsedShrink / FRAME_DURATION), FRAME_COUNT - 1);

                // Cập nhật hình ảnh
                node.setImage(shrinkFrames.get(currentFrame));

                // Khi đã tới frame cuối cùng
                if (elapsedShrink >= TOTAL_DURATION) {
                    stop();
                    startPulsate(); // chuyển sang animation pulsate
                    if (onFinished != null) onFinished.run();
                }
            }
        };

        shrinkTimer.start();
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

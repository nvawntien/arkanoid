package com.game.arkanoid.view.renderer;

import com.game.arkanoid.events.GameEventBus;
import com.game.arkanoid.events.paddle.ExplodePaddleEvent;
import com.game.arkanoid.events.paddle.IntroPaddleEvent;
import com.game.arkanoid.events.powerup.PowerUpActivatedEvent;
import com.game.arkanoid.events.powerup.PowerUpExpiredEvent;
import com.game.arkanoid.events.paddle.ExplodePaddleFinishedEvent;
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

/**
 * Renders the player's paddle and manages all paddle animations.
 * <p>
 * Supports intro animation, pulsate, expand/shrink for power-ups, laser mode,
 * and explosion animation when the paddle is destroyed.
 * </p>
 */
public final class PaddleRenderer implements Renderer<Paddle> {
    private final ImageView node;
    private final List<Image> introFrames = new ArrayList<>();
    private final List<Image> pulsateFrames = new ArrayList<>();
    private final List<Image> wideFrames = new ArrayList<>();
    private final List<Image> shrinkFrames = new ArrayList<>();
    private final List<Image> widePulsateFrames = new ArrayList<>();
    private final List<Image> laserFrames = new ArrayList<>();
    private final List<Image> laserPulsateFrames = new ArrayList<>();
    private final List<Image> explodeFrames = new ArrayList<>();
    private final List<GameEventBus.Subscription> subscriptions = new ArrayList<>();

    private Timeline currentAnimation;
    private AnimationTimer expandTimer, shrinkTimer, laserTimer;
    private double elapsedExpand = 0;
    private double elapsedShrink = 0;
    private double elapsedLaser = 0;
    private boolean isTransforming = false; // true if paddle is expanding or shrinking

    /**
     * Constructs a PaddleRenderer and loads all paddle animation frames.
     *
     * @param pane the Pane to which the paddle node will be added
     */
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

        // shrink is reverse of wide frames
        shrinkFrames.addAll(wideFrames);
        Collections.reverse(shrinkFrames);

        // Load wide pulsate frames
        for (int i = 1; i <= 4; i++) {
            widePulsateFrames.add(new Image(getClass().getResourceAsStream(
                String.format("/com/game/arkanoid/images/paddle_wide_pulsate_%d.png", i)
            )));
        }

        // Load explode frames
        for (int i = 1; i <= 8; i++) {
            explodeFrames.add(new Image(getClass().getResourceAsStream(
                String.format("/com/game/arkanoid/images/paddle_explode_%d.png", i)
            )));
        }

        registerEventListeners();
        node = new ImageView(introFrames.get(0));
        node.setSmooth(true);
        pane.getChildren().add(node);
    }

    /**
     * Registers event listeners for paddle intro and explosion events.
     */
    public void registerEventListeners() {
        subscriptions.add(GameEventBus.getInstance().subscribe(IntroPaddleEvent.class, e -> playIntro(this::startPulsate)));
        subscriptions.add(GameEventBus.getInstance().subscribe(ExplodePaddleEvent.class,e -> playExplosion()));
    }

    /**
     * Handles activation of power-ups affecting the paddle.
     *
     * @param event the power-up activation event
     */
    public void onPowerUpActivated(PowerUpActivatedEvent event) {
        switch (event.type()) {
            case EXPAND_PADDLE -> playExpand(null);
            case LASER_PADDLE -> playLaser(null);
            default -> {}
        }
    }

    /**
     * Handles expiration of power-ups affecting the paddle.
     *
     * @param event the power-up expiration event
     */
    public void onPowerUpExpired(PowerUpExpiredEvent event) {
        switch (event.type()) {
            case EXPAND_PADDLE -> playShrink(null);
            case LASER_PADDLE -> startPulsate();
            default -> {}
        }
    }

    /**
     * Updates the paddle position and size based on the Paddle model.
     *
     * @param paddle the current paddle state
     */
    @Override
    public void render(Paddle paddle) { 
        node.setTranslateX(paddle.getX());
        node.setTranslateY(paddle.getY());

        if (!isTransforming) {
            node.setFitWidth(paddle.getWidth());
            node.setFitHeight(paddle.getHeight());
        }
    }
    
    /**
     * Returns the ImageView node representing the paddle.
     *
     * @return the paddle ImageView
     */
    @Override
    public ImageView getNode() {
        return node;
    }

    // --- Animation controls ---

    /**
     * Plays the intro animation for the paddle.
     *
     * @param onFinished a callback to run after the animation completes
     */
    public void playIntro(Runnable onFinished) {
        if (introFrames.isEmpty()) return;
        playFrameSequence(introFrames, 80, false, onFinished);
    }

    /**
     * Plays the paddle explosion animation.
     */
    public void playExplosion() {
        if (explodeFrames.isEmpty()) return;
        playFrameSequence(explodeFrames, 80, false, this::publishExplodeFinishedEvent);
    }

    /**
     * Publishes an event when the paddle explosion animation finishes.
     */
    private void publishExplodeFinishedEvent() {
        GameEventBus.getInstance().publish(new ExplodePaddleFinishedEvent(true));
    }

    /**
     * Starts pulsate animation for normal paddle.
     */
    public void startPulsate() {
        if (pulsateFrames.isEmpty()) return;
        isTransforming = false;
        playFrameSequence(pulsateFrames, 100, true, null);
    }

    /**
     * Plays expand animation when the paddle grows in size.
     *
     * @param onFinished callback executed after animation completes
     */
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

    /**
     * Starts wide paddle pulsate animation.
     */
    public void startWidePulsate() {
        if (widePulsateFrames.isEmpty()) return;
        isTransforming = false;
        playFrameSequence(widePulsateFrames, 100, true, null);
    }

    /**
     * Plays shrink animation when the paddle returns to normal size.
     *
     * @param onFinished callback executed after animation completes
     */
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

    /**
     * Plays laser paddle animation.
     *
     * @param onFinished callback executed after animation completes
     */
    public void playLaser(Runnable onFinished) {
        stopAnimation();

        final int FRAME_COUNT = laserFrames.size();
        final double FRAME_DURATION = 0.06;
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

    /**
     * Starts laser paddle pulsate animation.
     */
    public void startLaserPulsate() {
        if (laserPulsateFrames.isEmpty()) return;
        isTransforming = false;
        playFrameSequence(laserPulsateFrames, 100, true, null);
    }

    /**
     * Stops all current paddle animations.
     */
    public void stopAnimation() {
        if (currentAnimation != null) currentAnimation.stop();
        if (expandTimer != null) expandTimer.stop();
        if (shrinkTimer != null) shrinkTimer.stop();
        if (laserTimer != null) laserTimer.stop();
    }

    /**
     * Plays a sequence of frames for paddle animation.
     *
     * @param frames the list of images representing animation frames
     * @param frameDurationMs duration of each frame in milliseconds
     * @param loop whether to loop the animation indefinitely
     * @param onFinished callback executed when animation finishes (ignored if loop=true)
     */
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

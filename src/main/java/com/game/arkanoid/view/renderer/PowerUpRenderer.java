package com.game.arkanoid.view.renderer;

import com.game.arkanoid.models.PowerUp;
import com.game.arkanoid.models.PowerUpType;
import javafx.animation.AnimationTimer;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Renders falling power-ups on the game pane.
 * <p>
 * Each power-up has 8-frame animation. Uses an AnimationTimer to
 * cycle through the frames for smooth animation.
 * </p>
 */
public final class PowerUpRenderer implements Renderer<List<PowerUp>> {
    private final Pane pane;
    private final Map<PowerUp, ImageView> nodes = new IdentityHashMap<>();

    /** Number of frames per power-up animation */
    private static final int FRAME_COUNT = 8;

    /** Duration of each frame in seconds */
    private static final double FRAME_DURATION = 0.05;

    /** Sprite frames for each type of power-up */
    private final Map<PowerUpType, Image[]> spriteMap = new IdentityHashMap<>();

    private double elapsedTime = 0;

    /**
     * Constructs a PowerUpRenderer and loads sprite images.
     *
     * @param pane the Pane where power-ups will be rendered
     */
    public PowerUpRenderer(Pane pane) {
        this.pane = pane;
        loadSprites();
        startAnimation();
    }

    /** Loads sprite images for all power-up types */
    private void loadSprites() {
        spriteMap.put(PowerUpType.EXPAND_PADDLE, loadFrames("expand"));
        spriteMap.put(PowerUpType.LASER_PADDLE, loadFrames("laser"));
        spriteMap.put(PowerUpType.MULTI_BALL, loadFrames("duplicate"));
        spriteMap.put(PowerUpType.EXTRA_LIFE, loadFrames("life"));
        spriteMap.put(PowerUpType.SLOW_BALL, loadFrames("slow"));
        spriteMap.put(PowerUpType.CATCH_BALL, loadFrames("catch"));
    }

    /**
     * Loads individual frames for a given power-up sprite.
     *
     * @param name the base name of the power-up sprite
     * @return an array of Images representing animation frames
     */
    private Image[] loadFrames(String name) {
        Image[] frames = new Image[FRAME_COUNT];
        for (int i = 1; i <= FRAME_COUNT; i++) {
            frames[i - 1] = new Image(
                getClass().getResource("/com/game/arkanoid/images/powerup_" + name + "_" + i + ".png").toExternalForm()
            );
        }
        return frames;
    }

    /**
     * Renders the given list of power-ups, updating their positions
     * and removing any that no longer exist.
     *
     * @param powerUps the list of active power-ups
     */
    @Override
    public void render(List<PowerUp> powerUps) {
        Iterator<Map.Entry<PowerUp, ImageView>> it = nodes.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<PowerUp, ImageView> entry = it.next();
            if (!powerUps.contains(entry.getKey())) {
                pane.getChildren().remove(entry.getValue());
                it.remove();
            }
        }

        for (PowerUp powerUp : powerUps) {
            ImageView node = nodes.computeIfAbsent(powerUp, this::createNode);
            node.setX(powerUp.getX());
            node.setY(powerUp.getY());
        }
    }

    /**
     * Not applicable for list renderers; returns null.
     *
     * @return null
     */
    @Override
    public ImageView getNode() {
        return null;
    }

    /**
     * Creates a new ImageView node for the given power-up.
     *
     * @param powerUp the power-up model
     * @return the created ImageView
     */
    private ImageView createNode(PowerUp powerUp) {
        ImageView imageView = new ImageView(spriteMap.get(powerUp.getType())[0]);
        imageView.setFitWidth(powerUp.getWidth());
        imageView.setFitHeight(powerUp.getHeight());
        pane.getChildren().add(imageView);
        return imageView;
    }

    /** Starts the animation timer to update frame images continuously */
    private void startAnimation() {
        new AnimationTimer() {
            private long lastTime = 0;

            @Override
            public void handle(long now) {
                if (lastTime == 0) {
                    lastTime = now;
                    return;
                }

                double delta = (now - lastTime) / 1e9; // convert nanoseconds to seconds
                lastTime = now;
                elapsedTime += delta;

                int currentFrame = (int) ((elapsedTime / FRAME_DURATION) % FRAME_COUNT);

                for (Map.Entry<PowerUp, ImageView> entry : nodes.entrySet()) {
                    PowerUp p = entry.getKey();
                    ImageView view = entry.getValue();
                    Image[] frames = spriteMap.get(p.getType());
                    if (frames != null && frames.length > 0) {
                        view.setImage(frames[currentFrame]);
                    }
                }
            }
        }.start();
    }
}

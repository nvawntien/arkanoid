package com.game.arkanoid.view.renderer;

import com.game.arkanoid.events.enemy.ExplosionEvent;
import com.game.arkanoid.events.GameEventBus;
import com.game.arkanoid.models.Enemy;
import com.game.arkanoid.models.EnemyType;
import javafx.animation.AnimationTimer;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

import java.util.*;

/**
 * Renderer for enemies in the game.
 * <p>
 * Manages enemy sprites, positions, and explosion animations using an
 * event-driven system. Supports animated frames for enemies and explosions.
 * </p>
 */
public final class EnemyRenderer implements Renderer<List<Enemy>> {

    private final Pane pane;
    private final Map<Enemy, ImageView> enemyNodes = new IdentityHashMap<>();

    private static final int ENEMY_FRAME_COUNT = 25;
    private static final double ENEMY_FRAME_DURATION = 0.1;

    private static final int EXPLOSION_FRAME_COUNT = 10;
    private static final double EXPLOSION_FRAME_DURATION = 0.05;

    private final Map<EnemyType, Image[]> enemySprites = new IdentityHashMap<>();
    private final Image[] explosionFrames = new Image[EXPLOSION_FRAME_COUNT];

    private double elapsedTime = 0;
    private final List<Explosion> explosions = new ArrayList<>();
    private final GameEventBus eventBus = GameEventBus.getInstance();
    private final List<GameEventBus.Subscription> subscriptions = new ArrayList<>();

    /**
     * Constructs an EnemyRenderer attached to the specified pane.
     *
     * @param pane the Pane where enemies and explosions will be rendered
     */
    public EnemyRenderer(Pane pane) {
        this.pane = pane;
        loadFrames();
        subscribeToExplosionEvent();
        startAnimation();
    }

    /** Loads sprite frames for all enemy types and explosion animations. */
    private void loadFrames() {
        // Load enemy frames
        for (EnemyType type : EnemyType.values()) {
            Image[] frames = new Image[ENEMY_FRAME_COUNT];
            String name = type.name().toLowerCase();
            for (int i = 1; i <= ENEMY_FRAME_COUNT; i++) {
                frames[i - 1] = new Image(
                        getClass().getResource("/com/game/arkanoid/images/enemy_" + name + "_" + i + ".png")
                                .toExternalForm()
                );
            }
            enemySprites.put(type, frames);
        }

        // Load explosion frames
        for (int i = 1; i <= EXPLOSION_FRAME_COUNT; i++) {
            explosionFrames[i - 1] = new Image(
                    getClass().getResource("/com/game/arkanoid/images/enemy_explosion_" + i + ".png").toExternalForm()
            );
        }
    }

    /** Subscribes to ExplosionEvent from the GameEventBus. */
    private void subscribeToExplosionEvent() {
        subscriptions.add(eventBus.subscribe(ExplosionEvent.class, event -> {
            playExplosion(event.x(), event.y(), event.width(), event.height());
        }));
    }

    /**
     * Plays an explosion animation at the specified location.
     *
     * @param x the X coordinate of the explosion
     * @param y the Y coordinate of the explosion
     * @param width the width of the explosion image
     * @param height the height of the explosion image
     */
    public void playExplosion(double x, double y, double width, double height) {
        ImageView view = new ImageView(explosionFrames[0]);
        view.setFitWidth(width);
        view.setFitHeight(height);
        view.setX(x);
        view.setY(y);
        pane.getChildren().add(view);
        explosions.add(new Explosion(view));
    }

    /**
     * Renders the list of enemies, adding new ones and updating positions.
     * Removes nodes for enemies that no longer exist.
     *
     * @param enemies the list of current enemies
     */
    @Override
    public void render(List<Enemy> enemies) {
        // Remove enemy nodes that no longer exist
        Iterator<Map.Entry<Enemy, ImageView>> it = enemyNodes.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Enemy, ImageView> entry = it.next();
            if (!enemies.contains(entry.getKey())) {
                pane.getChildren().remove(entry.getValue());
                it.remove();
            }
        }

        // Add or update enemy nodes
        for (Enemy enemy : enemies) {
            ImageView node = enemyNodes.computeIfAbsent(enemy, this::createEnemyNode);
            node.setX(enemy.getX());
            node.setY(enemy.getY());
        }
    }

    @Override
    public ImageView getNode() {
        return null;
    }

    /** Creates a new ImageView for the given enemy with its first sprite frame. */
    private ImageView createEnemyNode(Enemy enemy) {
        ImageView imageView = new ImageView(enemySprites.get(enemy.getType())[0]);
        imageView.setFitWidth(enemy.getWidth());
        imageView.setFitHeight(enemy.getHeight());
        pane.getChildren().add(imageView);
        return imageView;
    }

    /** Starts the animation timer for enemy sprite updates and explosions. */
    private void startAnimation() {
        new AnimationTimer() {
            private long lastTime = 0;

            @Override
            public void handle(long now) {
                if (lastTime == 0) { lastTime = now; return; }
                double delta = (now - lastTime) / 1e9;
                lastTime = now;
                elapsedTime += delta;

                int currentFrame = (int) (elapsedTime / ENEMY_FRAME_DURATION) % ENEMY_FRAME_COUNT;
                for (Map.Entry<Enemy, ImageView> entry : enemyNodes.entrySet()) {
                    Image[] frames = enemySprites.get(entry.getKey().getType());
                    if (frames != null && frames.length > 0) {
                        entry.getValue().setImage(frames[currentFrame]);
                    }
                }

                // Update explosions
                Iterator<Explosion> it = explosions.iterator();
                while (it.hasNext()) {
                    Explosion exp = it.next();
                    exp.elapsed += delta;
                    int frameIndex = (int) (exp.elapsed / EXPLOSION_FRAME_DURATION);
                    if (frameIndex >= EXPLOSION_FRAME_COUNT) {
                        pane.getChildren().remove(exp.view);
                        it.remove();
                    } else {
                        exp.view.setImage(explosionFrames[frameIndex]);
                    }
                }
            }
        }.start();
    }

    /** Represents a single explosion animation instance. */
    private static class Explosion {
        ImageView view;
        double elapsed = 0;

        Explosion(ImageView view) {
            this.view = view;
        }
    }
}

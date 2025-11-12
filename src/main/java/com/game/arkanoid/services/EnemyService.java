package com.game.arkanoid.services;

import com.game.arkanoid.events.GameEventBus;
import com.game.arkanoid.events.enemy.ExplosionEvent;
import com.game.arkanoid.events.sound.ExplosionSoundEvent;
import com.game.arkanoid.models.*;
import com.game.arkanoid.utils.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Service responsible for handling enemies in the game.
 * <p>
 * Includes spawning enemies, updating positions, handling collisions
 * with walls, bricks, balls, bullets, and the paddle,
 * applying effects, and triggering explosions.
 */
public final class EnemyService {

    private final Random random = new Random();
    private final GameEventBus eventBus = GameEventBus.getInstance();

    /** Maximum number of enemies allowed on screen simultaneously */
    private static final int MAX_ENEMIES = 5;

    public EnemyService() {}

    /**
     * Spawns a new enemy at the specified position, if under the limit.
     *
     * @param state the current game state
     * @param x initial x-coordinate
     * @param y initial y-coordinate
     */
    public void spawnEnemy(GameState state, double x, double y) {
        if (state.enemies.size() >= MAX_ENEMIES) return;

        EnemyType[] types = EnemyType.values();
        EnemyType type = types[random.nextInt(types.length)];
        double initialVx = Constants.ENEMY_SPEED_X * (random.nextBoolean() ? 1 : -1);

        Enemy enemy = new Enemy(type, x, y, Constants.ENEMY_WIDTH, Constants.ENEMY_HEIGHT, initialVx, Constants.ENEMY_SPEED_Y);
        state.enemies.add(enemy);
    }

    /**
     * Updates all enemies: moves them, handles collisions, applies effects, 
     * and removes enemies or bullets if necessary.
     *
     * @param state the current game state
     * @param dt time delta in seconds
     * @param worldW width of the game world
     * @param worldH height of the game world
     */
    public void update(GameState state, double dt, double worldW, double worldH) {
        List<Enemy> toRemove = new ArrayList<>();
        List<Bullet> toRemoveBullets = new ArrayList<>();

        for (Enemy enemy : state.enemies) {
            if (enemy.getY() > Constants.DOOR_TOP_Y + 22);
            enemy.update(dt);

            handleWallCollision(enemy, worldW, worldH);
            handleBrickCollision(enemy, state);

            if (intersects(enemy, state.paddle)) {
                applyEnemyEffectOnPaddle(enemy, state);
                spawnExplosion(enemy);
                toRemove.add(enemy);
                continue;
            }

            for (Ball ball : state.balls) {
                if (intersects(enemy, ball)) {
                    applyEnemyEffectOnBall(enemy, state, ball);
                    spawnExplosion(enemy);
                    toRemove.add(enemy);
                    break;
                }
            }

            for (Bullet bullet : state.bullets) {
                if (intersects(enemy, bullet)) {
                    spawnExplosion(enemy);
                    toRemove.add(enemy);
                    toRemoveBullets.add(bullet);
                    break;
                }
            }

            if (enemy.getY() > worldH) {
                toRemove.add(enemy);
            }
        }

        state.enemies.removeAll(toRemove);
        state.bullets.removeAll(toRemoveBullets);
    }

    /**
     * Applies enemy effects when colliding with the paddle.
     *
     * @param enemy the enemy that collided
     * @param state the game state
     */
    private void applyEnemyEffectOnPaddle(Enemy enemy, GameState state) {
        switch (enemy.getType()) {
            case CONE -> state.decrementLives();
            case CUBE -> { /* Only explosion, no ball lost */ }
            case MOLECULE -> fastAllBalls(state);
            case PYRAMID -> state.decrementScore(50); 
        }
    }

    /**
     * Applies enemy effects when colliding with a ball.
     *
     * @param enemy the enemy that collided
     * @param state the game state
     * @param ball the ball that collided
     */
    private void applyEnemyEffectOnBall(Enemy enemy, GameState state, Ball ball) {
        switch (enemy.getType()) {
            case CONE -> state.decrementLives();
            case CUBE -> state.balls.remove(ball);
            case MOLECULE -> fastAllBalls(state);
            case PYRAMID -> state.decrementScore(50);
        }

        if (enemy.getType() != EnemyType.CUBE) {
            double ballLeft = ball.getX() - ball.getRadius();
            double ballRight = ball.getX() + ball.getRadius();
            double ballTop = ball.getY() - ball.getRadius();
            double ballBottom = ball.getY() + ball.getRadius();

            double enemyLeft = enemy.getX();
            double enemyRight = enemy.getX() + enemy.getWidth();
            double enemyTop = enemy.getY();
            double enemyBottom = enemy.getY() + enemy.getHeight();

            double overlapLeft = ballRight - enemyLeft;
            double overlapRight = enemyRight - ballLeft;
            double overlapTop = ballBottom - enemyTop;
            double overlapBottom = enemyBottom - ballTop;

            double minOverlapX = Math.min(overlapLeft, overlapRight);
            double minOverlapY = Math.min(overlapTop, overlapBottom);

            if (minOverlapX < minOverlapY) {
                ball.setVelocity(-ball.getDx(), ball.getDy());
            } else {
                ball.setVelocity(ball.getDx(), -ball.getDy());
            }
        }
    }

    /**
     * Increases the speed of all balls by 30%.
     *
     * @param state the game state
     */
    private void fastAllBalls(GameState state) {
        for (Ball ball : state.balls) {
            ball.setVelocity(ball.getDx() * 1.3, ball.getDy() * 1.3);
        }
    }

    /**
     * Handles collisions between an enemy and the world bounds (walls).
     *
     * @param enemy the enemy
     * @param worldW world width
     * @param worldH world height
     */
    private void handleWallCollision(Enemy enemy, double worldW, double worldH) {
        double leftBound = 22;
        double rightBound = worldW - 22;
        double topBound = 172;

        if (enemy.getX() < leftBound) {
            enemy.setX(leftBound);
            enemy.setVx(Math.abs(enemy.getVx()));
        } else if (enemy.getX() + enemy.getWidth() > rightBound) {
            enemy.setX(rightBound - enemy.getWidth());
            enemy.setVx(-Math.abs(enemy.getVx()));
        }

        if (enemy.getY() < topBound) {
            enemy.setY(topBound);
            enemy.setVy(Math.abs(enemy.getVy()));
        }
    }

    /**
     * Handles collisions between an enemy and bricks.
     *
     * @param enemy the enemy
     * @param state the game state
     */
    private void handleBrickCollision(Enemy enemy, GameState state) {
        boolean collidedY = false;
        for (GameObject brick : state.bricks) {
            if (intersects(enemy, brick)) {
                collidedY = true;
                if (enemy.getVy() > 0) {
                    enemy.setY(brick.getY() - enemy.getHeight());
                } else if (enemy.getVy() < 0) {
                    enemy.setY(brick.getY() + brick.getHeight());
                }
                enemy.setVy(0);
                break;
            }
        }
        if (!collidedY) {
            enemy.setVy(Constants.ENEMY_SPEED_Y);
        }

        for (GameObject brick : state.bricks) {
            if (intersects(enemy, brick)) {
                if (enemy.getVx() > 0) {
                    enemy.setX(brick.getX() - enemy.getWidth());
                } else if (enemy.getVx() < 0) {
                    enemy.setX(brick.getX() + brick.getWidth());
                }
                enemy.setVx(-enemy.getVx());
                break;
            }
        }
    }

    /**
     * Spawns an explosion effect and plays the sound at the enemy's location.
     *
     * @param enemy the enemy to explode
     */
    private void spawnExplosion(Enemy enemy) {
        eventBus.publish(new ExplosionSoundEvent());
        eventBus.publish(new ExplosionEvent(
                enemy.getX(),
                enemy.getY(),
                enemy.getWidth(),
                enemy.getHeight()
        ));
    }

    /**
     * Checks if two game objects intersect.
     *
     * @param a first object
     * @param b second object
     * @return true if the objects intersect
     */
    private boolean intersects(GameObject a, GameObject b) {
        return a.getX() < b.getX() + b.getWidth() &&
               a.getX() + a.getWidth() > b.getX() &&
               a.getY() < b.getY() + b.getHeight() &&
               a.getY() + a.getHeight() > b.getY();
    }
}

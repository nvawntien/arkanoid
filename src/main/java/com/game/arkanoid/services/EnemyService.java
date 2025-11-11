package com.game.arkanoid.services;

import com.game.arkanoid.events.GameEventBus;
import com.game.arkanoid.events.enemy.ExplosionEvent;
import com.game.arkanoid.events.game.GameOverEvent;
import com.game.arkanoid.events.sound.ExplosionSoundEvent;
import com.game.arkanoid.models.*;

import com.game.arkanoid.utils.Constants;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class EnemyService {

    private final Random random = new Random();
    private final GameEventBus eventBus = GameEventBus.getInstance();

    private static final int MAX_ENEMIES = 5;

    public EnemyService() {}

    public void spawnEnemy(GameState state, double x, double y) {
        if (state.enemies.size() >= MAX_ENEMIES) return;

        EnemyType[] types = EnemyType.values();
        EnemyType type = types[random.nextInt(types.length)];
        double initialVx = Constants.ENEMY_SPEED_X * (random.nextBoolean() ? 1 : -1);

        Enemy enemy = new Enemy(type, x, y, Constants.ENEMY_WIDTH, Constants.ENEMY_HEIGHT, initialVx, Constants.ENEMY_SPEED_Y);
        state.enemies.add(enemy);
    }

    public void update(GameState state, double dt, double worldW, double worldH) {
        List<Enemy> toRemove = new ArrayList<>();
        List<Bullet> toRemoveBullets = new ArrayList<>();

        for (Enemy enemy : state.enemies) {
            if (enemy.getY() > Constants.DOOR_TOP_Y + 22);
            enemy.update(dt);

            handleWallCollision(enemy, worldW, worldH);
            handleBrickCollision(enemy, state);

            // Va chạm với paddle
            if (intersects(enemy, state.paddle)) {
                applyEnemyEffectOnPaddle(enemy, state);
                spawnExplosion(enemy);
                toRemove.add(enemy);
                continue;
            }

            // Va chạm với balls
            for (Ball ball : state.balls) {
                if (intersects(enemy, ball)) {
                    applyEnemyEffectOnBall(enemy, state, ball);
                    spawnExplosion(enemy);
                    toRemove.add(enemy);
                    break;
                }
            }

            // Va chạm với bullets
            for (Bullet bullet : state.bullets) {
                if (intersects(enemy, bullet)) {
                    spawnExplosion(enemy);
                    toRemove.add(enemy);
                    toRemoveBullets.add(bullet);
                    break;
                }
            }

            // Loại bỏ nếu rơi khỏi màn hình
            if (enemy.getY() > worldH) {
                toRemove.add(enemy);
            }
        }

        state.enemies.removeAll(toRemove);
        state.bullets.removeAll(toRemoveBullets);
    }

    private void applyEnemyEffectOnPaddle(Enemy enemy, GameState state) {
        switch (enemy.getType()) {
            case CONE -> state.decrementLives();
            case CUBE -> { /* Chỉ nổ, không mất ball */ }
            case MOLECULE -> fastAllBalls(state);
            case PYRAMID -> state.decrementScore(50); 
        }
    }

    private void applyEnemyEffectOnBall(Enemy enemy, GameState state, Ball ball) {
        switch (enemy.getType()) {
            case CONE -> state.decrementLives();
            case CUBE -> state.balls.remove(ball); // mất ball
            case MOLECULE -> fastAllBalls(state);
            case PYRAMID -> state.decrementScore(50); // giảm điểm
        }
    }

    private void fastAllBalls(GameState state) {
        for (Ball ball : state.balls) {
            ball.setVelocity(ball.getDx() * 1.3, ball.getDy() * 1.3); // tăng tốc 30%
        }
    }

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

    private void handleBrickCollision(Enemy enemy, GameState state) {
        boolean collided = false;
        for (GameObject brick : state.bricks) {
            if (intersects(enemy, brick)) {
                enemy.setVy(0);
                collided = true;
            }
        }

        if (!collided) {
            enemy.setVy(Constants.ENEMY_SPEED_Y);
        }
    }

    private void spawnExplosion(Enemy enemy) {
        eventBus.publish(new ExplosionSoundEvent());
        eventBus.publish(new ExplosionEvent(
                enemy.getX(),
                enemy.getY(),
                enemy.getWidth(),
                enemy.getHeight()
        ));
    }

    private boolean intersects(GameObject a, GameObject b) {
        return a.getX() < b.getX() + b.getWidth() &&
               a.getX() + a.getWidth() > b.getX() &&
               a.getY() < b.getY() + b.getHeight() &&
               a.getY() + a.getHeight() > b.getY();
    }
}

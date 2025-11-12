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

            // Tìm chiều nhỏ nhất
            double minOverlapX = Math.min(overlapLeft, overlapRight);
            double minOverlapY = Math.min(overlapTop, overlapBottom);

            if (minOverlapX < minOverlapY) {
                // Va chạm theo trục X → đảo dx
                ball.setVelocity(-ball.getDx(), ball.getDy());
            } else {
                // Va chạm theo trục Y → đảo dy
                ball.setVelocity(ball.getDx(), -ball.getDy());
            }
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
        // Trục Y trước (rơi lên/xuống)
        boolean collidedY = false;
        for (GameObject brick : state.bricks) {
            if (intersects(enemy, brick)) {
                collidedY = true;
                if (enemy.getVy() > 0) { // rơi xuống
                    enemy.setY(brick.getY() - enemy.getHeight());
                } else if (enemy.getVy() < 0) { // đi lên
                    enemy.setY(brick.getY() + brick.getHeight());
                }
                enemy.setVy(0);
                break;
            }
        }
        if (!collidedY) {
            enemy.setVy(Constants.ENEMY_SPEED_Y);
        }

        // Trục X (nếu muốn enemy đổi hướng khi chạm tường brick)
        boolean collidedX = false;
        for (GameObject brick : state.bricks) {
            if (intersects(enemy, brick)) {
                collidedX = true;
                if (enemy.getVx() > 0) { // đi sang phải
                    enemy.setX(brick.getX() - enemy.getWidth());
                } else if (enemy.getVx() < 0) { // đi sang trái
                    enemy.setX(brick.getX() + brick.getWidth());
                }
                enemy.setVx(-enemy.getVx()); // đổi hướng ngang nhẹ
                break;
            }
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

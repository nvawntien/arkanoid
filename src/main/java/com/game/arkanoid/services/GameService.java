package com.game.arkanoid.services;

import com.game.arkanoid.config.GameSettings;
import com.game.arkanoid.events.BrickDestroyedEvent;
import com.game.arkanoid.events.BrickHitEvent;
import com.game.arkanoid.events.GameEventBus;
import com.game.arkanoid.events.GameOverEvent;
import com.game.arkanoid.events.LevelClearedEvent;
import com.game.arkanoid.events.LifeLostEvent;
import com.game.arkanoid.events.PaddleHitEvent;
import com.game.arkanoid.models.Ball;
import com.game.arkanoid.models.Brick;
import com.game.arkanoid.models.GameState;
import com.game.arkanoid.models.InputState;
import com.game.arkanoid.models.PowerUp;
import com.game.arkanoid.models.PowerUpType;
import com.game.arkanoid.utils.Constants;
import java.util.Iterator;
import java.util.List;

/**
 * Core game orchestration logic. Talks only to other services/models, publishing events for
 * presentation-specific reactions (sound, animations, etc.).
 */
public final class GameService {

    private final BallService ballSvc;
    private final PaddleService paddleSvc;
    private final BricksService bricksSvc;
    private final PowerUpService powerUpSvc;
    private final BulletService bulletSvc;
    private final RoundService roundSvc;
    private GameState boundState;

    public GameService (
            BallService ballSvc,
            PaddleService paddleSvc,
            BricksService bricksSvc,
            PowerUpService powerUpSvc,
            BulletService bulletSvc,
            RoundService roundSvc
    ) {
        this.ballSvc = ballSvc;
        this.paddleSvc = paddleSvc;
        this.bricksSvc = bricksSvc;
        this.powerUpSvc = powerUpSvc;
        this.bulletSvc = bulletSvc;
        this.roundSvc = roundSvc;
    }

    public void bindState(GameState state) {
        this.boundState = state;
    }

    public void update(GameState state, InputState in, double dt, double worldW, double worldH) {
        if (!state.running || state.paused) {
            return;
        }

        double scaledDt = dt * state.timeScale;

        bulletSvc.tickCooldown(state, scaledDt);
        handleInput(state, in, scaledDt, worldW);
        updatePrimaryBall(state, scaledDt, worldW, worldH);
        updateSecondaryBalls(state, scaledDt, worldW, worldH);
        updateBullets(state, scaledDt, worldH);
        powerUpSvc.update(state, scaledDt, worldW, worldH);
        checkLevelCleared(state);
    }

    private void handleInput(GameState state, InputState in, double dt, double worldW) {
        if (in.left) {
            paddleSvc.moveLeft(state.paddle, dt, worldW);
        }
        if (in.right) {
            paddleSvc.moveRight(state.paddle, dt, worldW);
        }

        if (!state.ball.isMoving()) {
            ballSvc.resetOnPaddle(state.ball, state.paddle);
        }
        if (in.launch && !state.ball.isMoving()) {
            ballSvc.launch(state.ball);
        }
        if (in.fire && state.activePowerUps.containsKey(PowerUpType.LASER_PADDLE)) {
            bulletSvc.tryFire(state, state.paddle);
        }
    }

    private void updatePrimaryBall(GameState state, double dt, double worldW, double worldH) {
        Ball ball = state.ball;
        ballSvc.step(ball, dt);
        ballSvc.bounceWorld(ball, worldW, worldH);
        handlePaddleCollision(ball, state);
        handleBrickCollisions(state, ball);

        if (ballSvc.fellBelow(ball, worldH)) {
            if (!state.extraBalls.isEmpty()) {
                Ball replacement = state.extraBalls.remove(0);
                copyBallState(replacement, state.ball);
            } else {
                state.decrementLives();
                GameEventBus.getInstance().publish(new LifeLostEvent(state.lives));
                state.resetForLife();
                state.extraBalls.clear();
                ballSvc.resetOnPaddle(state.ball, state.paddle);
                if (state.lives < 0) {
                    state.running = false;
                    state.gameOver = true;
                    GameEventBus.getInstance().publish(new GameOverEvent());
                }
            }
        }
    }

    private void updateSecondaryBalls(GameState state, double dt, double worldW, double worldH) {
        Iterator<Ball> iterator = state.extraBalls.iterator();
        while (iterator.hasNext()) {
            Ball extra = iterator.next();
            ballSvc.step(extra, dt);
            ballSvc.bounceWorld(extra, worldW, worldH);
            handlePaddleCollision(extra, state);
            handleBrickCollisions(state, extra);
            if (ballSvc.fellBelow(extra, worldH)) {
                iterator.remove();
            }
        }
    }

    private void updateBullets(GameState state, double dt, double worldH) {
        List<BulletService.Impact> impacts = bulletSvc.update(state, state.bricks, dt, worldH);
        for (BulletService.Impact impact : impacts) {
            Brick brick = impact.brick();

            // 1) giống như bóng: để bricksSvc xử lý hit + set destroyed nếu đủ
            boolean destroyed = bricksSvc.handleBrickHit(brick);

            // 2) fire sự kiện hit (trước hay sau handleBrickHit đều ok, giữ consistent với luồng bóng)
            GameEventBus.getInstance().publish(new BrickHitEvent(brick));

            // 3) nếu đã phá
            if (destroyed) {
                processDestroyedBrick(state, brick); // spawn powerup + cộng điểm + re-check clear
            }
        }
    }


    private void handlePaddleCollision(Ball ball, GameState state) {
        if (ballSvc.checkCollision(ball, state.paddle)) {
            ballSvc.bounceOff(ball, state.paddle);
            ball.setCenter(ball.getCenterX(), state.paddle.getY() - ball.getRadius() - Constants.BALL_NUDGE);
            GameEventBus.getInstance().publish(new PaddleHitEvent());
        }
    }

    private void handleBrickCollisions(GameState state, Ball ball) {
        for (Brick brick : state.bricks) {
            if (brick.isDestroyed()) {
                continue;
            }

            if (ballSvc.checkCollision(ball, brick)) {
                ballSvc.bounceOff(ball, brick);
                GameEventBus.getInstance().publish(new BrickHitEvent(brick));
                boolean destroyed = bricksSvc.handleBrickHit(brick);
                if (destroyed) {
                    processDestroyedBrick(state, brick);
                }
                break;
            }
        }
    }

    private int countAliveBricks(GameState state) {
        int alive = 0;
        for (Brick brick : state.bricks) {
            if (!brick.isDestroyed()) {
                alive++;
            }
        }
        return alive;
    }

    private void checkLevelCleared(GameState state) {
        // Nếu đang chờ transition thì bỏ qua
        if (state.levelTransitionPending) {
            return;
        }

        // Kiểm tra xem còn gạch không
        if (bricksSvc.allBricksCleared(state.bricks)) {
            System.out.println("[GameService] All bricks cleared at level " + state.level);
            state.levelTransitionPending = true;
            state.running = false;

            // Phát sự kiện LevelCleared
            GameEventBus.getInstance().publish(new LevelClearedEvent(state.level));
        }
    }


    private void processDestroyedBrick(GameState state, Brick brick) {
        state.score++;
        GameEventBus.getInstance().publish(new BrickDestroyedEvent(brick));
        PowerUp spawned = null;
        if (countAliveBricks(state) > 1) {
            spawned = powerUpSvc.spawnPowerUpIfAny(brick.getX(), brick.getY(), brick.getWidth());
        }
        if (spawned != null) {
            state.powerUps.add(spawned);
        }
        checkLevelCleared(state);
    }

    private void copyBallState(Ball source, Ball target) {
        target.setCenter(source.getCenterX(), source.getCenterY());
        target.setVelocity(source.getDx(), source.getDy());
        target.setMoving(source.isMoving());
    }

    public void restartLevel(GameState state) {
        roundSvc.loadLevel(state, state.level);
        state.running = true;
        state.gameOver = false;
        state.gameCompleted = false;
        state.paused = true;
        state.levelTransitionPending = false;
    }

    public void loadNextLevel(GameState state) {
        roundSvc.loadNextLevel(state);
    }

    public void startNextLevel(GameState state) {
        state.paused = false;
        state.running = true;
        state.levelTransitionPending = false;
    }

    public void loadLevel(GameState state, int levelIndex) {
        roundSvc.loadLevel(state, levelIndex);
    }
}

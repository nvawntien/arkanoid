package com.game.arkanoid.services;

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
import com.game.arkanoid.utils.Constants;
import java.util.Iterator;

/**
 * Core game orchestration logic. Talks only to other services/models, publishing events for
 * presentation-specific reactions (sound, animations, etc.).
 */
public final class GameService {

    private final BallService ballSvc;
    private final PaddleService paddleSvc;
    private final BricksService bricksSvc;
    private final PowerUpService powerUpSvc;
    private final RoundService roundSvc;
    private GameState boundState;

    public GameService (
            BallService ballSvc,
            PaddleService paddleSvc,
            BricksService bricksSvc,
            PowerUpService powerUpSvc,
            RoundService roundSvc
    ) {
        this.ballSvc = ballSvc;
        this.paddleSvc = paddleSvc;
        this.bricksSvc = bricksSvc;
        this.powerUpSvc = powerUpSvc;
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

        handleInput(state, in, scaledDt, worldW);
        updatePrimaryBall(state, scaledDt, worldW, worldH);
        updateSecondaryBalls(state, scaledDt, worldW, worldH);
        powerUpSvc.update(state, scaledDt, worldW, worldH);
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
                    state.score += 100;
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
        if (state.levelTransitionPending) {
            return;
        }
        if (bricksSvc.allBricksCleared(state.bricks)) {
            state.levelTransitionPending = true;
            state.running = false;
            GameEventBus.getInstance().publish(new LevelClearedEvent(state.level));
        }
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

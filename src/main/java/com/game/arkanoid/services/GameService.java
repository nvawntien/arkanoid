package com.game.arkanoid.services;

import com.game.arkanoid.models.Ball;
import com.game.arkanoid.models.Brick;
import com.game.arkanoid.models.GameState;
import com.game.arkanoid.models.InputState;
import com.game.arkanoid.utils.Constants;
import com.game.arkanoid.models.PowerUp;
import java.util.Iterator;

public final class GameService {

    private final BallService ballSvc;
    private final PaddleService paddleSvc;
    private final BricksService bricksSvc;
    private final PowerUpService powerUpSvc;

    public GameService(BallService ballSvc, PaddleService paddleSvc, BricksService bricksSvc, PowerUpService powerUpSvc) {
        this.ballSvc = ballSvc;
        this.paddleSvc = paddleSvc;
        this.bricksSvc = bricksSvc;
        this.powerUpSvc = powerUpSvc;
    }

    public void update(GameState state, InputState in, double dt, double worldW, double worldH) {
        if (!state.running) {
            return;
        }
        if (state.paused) {
            return;
        }

        final double scaledDt = dt * state.timeScale;

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
            ballSvc.dockToPaddle(state.ball, state.paddle);
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
                state.lives--;
                state.resetForLife();
                state.extraBalls.clear();
                ballSvc.resetOnPaddle(state.ball, state.paddle);
                if (state.lives < 0) {
                    state.running = false;
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
        if (ballSvc.intersectsAABB(ball, state.paddle)) {
            ballSvc.bounceOffAABB(ball, state.paddle);
            ball.setCenter(ball.getCenterX(), state.paddle.getY() - ball.getRadius() - Constants.BALL_NUDGE);
        }
    }

    private void handleBrickCollisions(GameState state, Ball ball) {
        for (Brick brick : state.bricks) {
            if (brick.isDestroyed()) {
                continue;
            }
            if (ballSvc.checkCollision(ball, brick)) {
                ballSvc.bounceOffAABB(ball, brick);
                boolean destroyed = bricksSvc.handleBrickHit(brick);
                if (destroyed) {
                    state.score += 100;
                    PowerUp spawned = powerUpSvc.spawnPowerUpIfAny(brick);
                    if (spawned != null) {
                        state.powerUps.add(spawned);
                    }
                }
                break;
            }
        }
    }

    private void copyBallState(Ball source, Ball target) {
        target.setCenter(source.getCenterX(), source.getCenterY());
        target.setVelocity(source.getDx(), source.getDy());
        target.setMoving(source.isMoving());
    }
}

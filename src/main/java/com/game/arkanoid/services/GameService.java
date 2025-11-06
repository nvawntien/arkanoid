package com.game.arkanoid.services;

import com.game.arkanoid.models.Ball;
import com.game.arkanoid.models.Brick;
import com.game.arkanoid.models.GameState;
import com.game.arkanoid.models.InputState;
import com.game.arkanoid.models.PowerUp;
import com.game.arkanoid.utils.Constants;
import com.game.arkanoid.view.renderer.PaddleRenderer;
import com.game.arkanoid.view.sound.SoundService;
import java.util.Iterator;

/**
 * Central gameplay orchestrator tying together domain services and triggering sounds/animations.
 */
public final class GameService {

    private final BallService ballSvc;
    private final PaddleService paddleSvc;
    private final BricksService bricksSvc;
    private final PowerUpService powerUpSvc;
    private final RoundService roundSvc;
    private final SoundService soundSvc;

    public GameService(
            BallService ballSvc,
            PaddleService paddleSvc,
            BricksService bricksSvc,
            PowerUpService powerUpSvc,
            RoundService roundSvc,
            SoundService soundSvc) {
        this.ballSvc = ballSvc;
        this.paddleSvc = paddleSvc;
        this.bricksSvc = bricksSvc;
        this.powerUpSvc = powerUpSvc;
        this.roundSvc = roundSvc;
        this.soundSvc = soundSvc;
    }

    public void bindState(GameState state) {
        // no-op kept for compatibility with previous wiring
    }

    public void update(GameState state, PaddleRenderer paddleRenderer, InputState in, double dt, double worldW, double worldH) {
        if (!state.running) {
            return;
        }
        if (state.paused) {
            return;
        }

        double scaledDt = dt * state.timeScale;

        handleInput(state, in, scaledDt, worldW);
        updatePrimaryBall(state, scaledDt, worldW, worldH, paddleRenderer);
        updateSecondaryBalls(state, scaledDt, worldW, worldH, paddleRenderer);
        powerUpSvc.update(ballSvc, paddleRenderer, state, scaledDt, worldW, worldH);

        if (bricksSvc.allBricksCleared(state.bricks)) {
            soundSvc.playSfx("level_clear");
            roundSvc.loadNextLevel(state);
            if (state.gameCompleted) {
                state.running = false;
            } else {
                state.paused = true; // allow controller to run countdown before next level
            }
        }
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

    private void updatePrimaryBall(GameState state, double dt, double worldW, double worldH, PaddleRenderer paddleRenderer) {
        Ball ball = state.ball;
        ballSvc.step(ball, dt);
        ballSvc.bounceWorld(ball, worldW, worldH);
        handlePaddleCollision(ball, state);
        handleBrickCollisions(state, ball, paddleRenderer);

        if (ballSvc.fellBelow(ball, worldH)) {
            if (!state.extraBalls.isEmpty()) {
                Ball replacement = state.extraBalls.remove(0);
                copyBallState(replacement, state.ball);
            } else {
                state.decrementLives();
                soundSvc.playSfx("life_lost");
                state.resetForLife();
                state.extraBalls.clear();
                ballSvc.resetOnPaddle(state.ball, state.paddle);
                if (state.lives < 0) {
                    state.running = false;
                    state.gameOver = true;
                    soundSvc.playSfx("game_over");
                }
            }
        }
    }

    private void updateSecondaryBalls(GameState state, double dt, double worldW, double worldH, PaddleRenderer paddleRenderer) {
        Iterator<Ball> iterator = state.extraBalls.iterator();
        while (iterator.hasNext()) {
            Ball extra = iterator.next();
            ballSvc.step(extra, dt);
            ballSvc.bounceWorld(extra, worldW, worldH);
            handlePaddleCollision(extra, state);
            handleBrickCollisions(state, extra, paddleRenderer);
            if (ballSvc.fellBelow(extra, worldH)) {
                iterator.remove();
            }
        }
    }

    private void handlePaddleCollision(Ball ball, GameState state) {
        if (ballSvc.checkCollision(ball, state.paddle)) {
            ballSvc.bounceOff(ball, state.paddle);
            ball.setCenter(ball.getCenterX(), state.paddle.getY() - ball.getRadius() - Constants.BALL_NUDGE);
            soundSvc.playSfx("paddle_hit");
        }
    }

    private void handleBrickCollisions(GameState state, Ball ball, PaddleRenderer paddleRenderer) {
        for (Brick brick : state.bricks) {
            if (brick.isDestroyed()) {
                continue;
            }

            if (ballSvc.checkCollision(ball, brick)) {
                ballSvc.bounceOff(ball, brick);
                soundSvc.playSfx("brick_hit");
                boolean destroyed = bricksSvc.handleBrickHit(brick);
                if (destroyed) {
                    state.score += 100;
                    soundSvc.playSfx("brick_break");

                    if (countAliveBricks(state) > 0) {
                        PowerUp spawned = powerUpSvc.spawnPowerUpIfAny(brick);
                        if (spawned != null) {
                            state.powerUps.add(spawned);
                        }
                    }
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
    }

    public void loadNextLevel(GameState state) {
        roundSvc.loadNextLevel(state);
    }

    public void startNextLevel(GameState state) {
        state.paused = false;
        state.running = true;
    }
}

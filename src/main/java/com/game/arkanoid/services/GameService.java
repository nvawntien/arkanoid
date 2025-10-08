// services/GameService.java
package com.game.arkanoid.services;

import java.util.Iterator;

import com.game.arkanoid.models.*;
import com.game.arkanoid.utils.Constants;

/**
 * Game Service.
 * 
 * @author bmngxn
 */
public final class GameService {
     private final BallService ballSvc;
    private final PaddleService paddleSvc;
    private final BricksService bricksSvc;

    public GameService(BallService ballSvc, PaddleService paddleSvc, BricksService bricksSvc) {
        this.ballSvc = ballSvc;
        this.paddleSvc = paddleSvc;
        this.bricksSvc = bricksSvc;
    }
    /**
     * Advance one frame of game logic.
     * @param in     input state for this frame
     * @param dt     delta time in seconds
     * @param worldW world width (e.g., pane width)
     * @param worldH world height (e.g., pane height)
     */
    public void update(GameState gameState, InputState in, double dt, double worldW, double worldH) {
        if (!gameState.running) return;

        if (in.pause) gameState.paused = !gameState.paused;
        if (gameState.paused) return;

        final double scaledDt = dt * gameState.timeScale;

        // Input -> paddle
        if (in.left) {
            paddleSvc.moveLeft(gameState.paddle,  scaledDt, worldW);
        }
        if (in.right) {
            paddleSvc.moveRight(gameState.paddle, scaledDt, worldW);
        }
        //  KEEP BALL DOCKED TO PADDLE WHEN NOT MOVING  ⬇⬇⬇
        if (!gameState.ball.isMoving()) {
            ballSvc.dockToPaddle(gameState.ball, gameState.paddle);
        }
        // (do this BEFORE launch/physics so it rides the paddle cleanly)
        if (in.launch && !gameState.ball.isMoving()) {
            ballSvc.launch(gameState.ball);
        }

        //  Ball physics
        ballSvc.step(gameState.ball, scaledDt);
        ballSvc.bounceWorld(gameState.ball, worldW, worldH);

        // Ball–Paddle collision (basic)
        if (ballSvc.intersectsAABB(gameState.ball, gameState.paddle)) {
            ballSvc.bounceOffAABB(gameState.ball, gameState.paddle);
            gameState.ball.setCenter(gameState.ball.getCenterX(), gameState.paddle.getY() - gameState.ball.getRadius() - Constants.BALL_NUDGE);
        }   

        //  Out of bounds (lose life)
        if (ballSvc.fellBelow(gameState.ball, worldH)) {
            gameState.lives--;
            gameState.resetForLife();
            ballSvc.resetOnPaddle(gameState.ball, gameState.paddle);
            if (gameState.lives < 0) {
                gameState.running = false; // game over
            }
        }

        //   TODO: ball–brick collisions --> update s.score, remove bricks
        //    (use a CollisionService that iterates s.bricks)
        Iterator<Brick> it = gameState.bricks.iterator();
        // 4) Ball–Brick collision
        for (Brick brick : gameState.bricks) {

            if (ballSvc.checkCollision(gameState.ball, brick) && !brick.isDestroyed()) {
                ballSvc.bounceOffAABB(gameState.ball, brick);
                boolean destroyed = bricksSvc.handleBrickHit(brick);
                if (destroyed) gameState.score += 100;
                    break; // chỉ xử lý 1 brick mỗi frame
            }
        }

    }

}



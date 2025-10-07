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
    public void update(GameState s, InputState in, double dt, double worldW, double worldH) {
        if (!s.running) return;

        if (in.pause) s.paused = !s.paused;
        if (s.paused) return;

        final double scaledDt = dt * s.timeScale;

        // 1) Input -> paddle
        if (in.left) {
            paddleSvc.moveLeft(s.paddle,  scaledDt, worldW);
        }
        if (in.right) {
            paddleSvc.moveRight(s.paddle, scaledDt, worldW);
        }
        // 1.5) KEEP BALL DOCKED TO PADDLE WHEN NOT MOVING  ⬇⬇⬇
        if (!s.ball.isMoving()) {
            ballSvc.dockToPaddle(s.ball, s.paddle);
        }
        // (do this BEFORE launch/physics so it rides the paddle cleanly)
        if (in.launch && !s.ball.isMoving()) {
            ballSvc.launch(s.ball);
        }

        // 2) Ball physics
        ballSvc.step(s.ball, scaledDt);
        ballSvc.bounceWorld(s.ball, worldW, worldH);

        // 3) Ball–Paddle collision (basic)
        if (ballSvc.intersectsAABB(s.ball, s.paddle)) {
            ballSvc.bounceOffAABB(s.ball, s.paddle);
            s.ball.setCenter(s.ball.getCenterX(), s.paddle.getY() - s.ball.getRadius() - Constants.BALL_NUDGE);
        }   

        // 4) Out of bounds (lose life)
        if (ballSvc.fellBelow(s.ball, worldH)) {
            s.lives--;
            s.resetForLife();
            ballSvc.resetOnPaddle(s.ball, s.paddle);
            if (s.lives < 0) {
                s.running = false; // game over
            }
        }
        //   TODO: ball–brick collisions --> update s.score, remove bricks
        //    (use a CollisionService that iterates s.bricks)
        Iterator<Brick> it = s.bricks.iterator();
        // 4) Ball–Brick collision
        for (Brick brick : s.bricks) {

            if (ballSvc.checkCollision(s.ball, brick) && !brick.isDestroyed()) {
                ballSvc.bounceOffAABB(s.ball, brick);
                boolean destroyed = bricksSvc.handleBrickHit(brick);
                if (destroyed) s.score += 100;
                    break; // chỉ xử lý 1 brick mỗi frame
            }
        }

    }

}



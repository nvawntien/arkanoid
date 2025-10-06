// services/GameService.java
package com.game.arkanoid.services;

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

    public GameService(BallService ballSvc, PaddleService paddleSvc) {
        this.ballSvc = ballSvc;
        this.paddleSvc = paddleSvc;
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
        if (in.left)  paddleSvc.moveLeft(s.paddle,  scaledDt, worldW);
        if (in.right) paddleSvc.moveRight(s.paddle, scaledDt, worldW);
        if (in.launch && !s.ball.isMoving()) ballSvc.launch(s.ball);

        // 2) Ball physics
        ballSvc.step(s.ball, scaledDt);
        ballSvc.bounceWorld(s.ball, worldW, worldH);

        // 3) Ball–Paddle collision (basic)
        if (s.ball.checkCollision(s.paddle)) {
            ballSvc.bounceOff(s.ball, s.paddle);
            // keep it just above the paddle to avoid sticking
            s.ball.setCenter(s.ball.getCenterX(),
                             s.paddle.getY() - s.ball.getRadius() - Constants.BALL_NUDGE);
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
    }

}

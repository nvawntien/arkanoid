package com.game.arkanoid.services;

import com.game.arkanoid.models.Ball;
import com.game.arkanoid.models.Paddle;
import com.game.arkanoid.utils.Constants;
import javafx.animation.AnimationTimer;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;

import java.util.Set;

public class GameService {
    private BallService ballService;
    private PaddleService paddleService;
    private AnimationTimer gameLoop;

    public GameService(BallService ballService, PaddleService paddleService) {
        this.ballService = ballService;
        this.paddleService  = paddleService;
    }

    public BallService getBallService() {
        return ballService;
    }

    public void setBallService(BallService ballService) {
        this.ballService = ballService;
    }

    public PaddleService getPaddleService() {
        return paddleService;
    }

    public void setPaddleService(PaddleService paddleService) {
        this.paddleService = paddleService;
    }

    public void startGameLoop(Pane gamePane, Set<KeyCode> activeKeys) {
        gameLoop = new AnimationTimer() {
            @Override // chưa đúng đâu xờiii
            public void handle(long now) {

                if (activeKeys.contains(KeyCode.LEFT)) {
                    paddleService.moveLeft();
                }
                if (activeKeys.contains(KeyCode.RIGHT)) {
                    paddleService.moveRight();
                }

                if (!ballService.getBall().isMoving()) {
                    ballService.dockTo(paddleService.getPaddle());
                }

                ballService.update(gamePane.getWidth(), gamePane.getHeight());
            }
        };



        gameLoop.start();
    }

    public void stop() {
        if (gameLoop != null) {
            gameLoop.stop();
        }
    }
}

package com.game.arkanoid.controller;

import com.game.arkanoid.models.Ball;
import com.game.arkanoid.models.Paddle;
import com.game.arkanoid.services.PaddleService;
import com.game.arkanoid.services.BallService;
import com.game.arkanoid.utils.Constants;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import java.util.HashSet;
import java.util.Set;
public class GameControl {

    @FXML
    private Pane gamePane; //id = gamePain

    private BallService ballService; 
    private PaddleService paddleService;

    private final Set<KeyCode> activeKeys = new HashSet<>();
    private AnimationTimer gameLoop;

    public void initialize() {
        //Create paddle
        Paddle paddle = new Paddle(
                Constants.GAME_WIDTH / 2 - Constants.PADDLE_WIDTH / 2, 
                Constants.GAME_HEIGHT - Constants.PADDLE_HEIGHT - 10 );

        paddleService = new PaddleService(paddle);

        //create ball
        Ball ball = new Ball(
                Constants.GAME_WIDTH / 2, 
                Constants.GAME_HEIGHT - Constants.PADDLE_HEIGHT - 5 - Constants.BALL_RADIUS - 5 );

        ballService = new BallService(ball);

        gamePane.getChildren().addAll(paddle.getNode(), ball.getNode());


        gamePane.setFocusTraversable(true);
        gamePane.setOnKeyPressed(event -> activeKeys.add(event.getCode()));
        gamePane.setOnKeyReleased(event -> activeKeys.remove(event.getCode()));

        Platform.runLater(() -> gamePane.requestFocus());

        startGameLoop();
        
    }
    private void startGameLoop() {
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
               
                if (activeKeys.contains(KeyCode.LEFT)) {
                    paddleService.moveLeft();
                }
                if (activeKeys.contains(KeyCode.RIGHT)) {
                    paddleService.moveRight();
                }

                ballService.update(); 
                
            }
        };
        gameLoop.start();
    }
    
    public void stopGame() {
        if (gameLoop != null) {
            gameLoop.stop();
        }
    }
}
package com.game.arkanoid.controller;

import com.game.arkanoid.models.Ball;
import com.game.arkanoid.models.Paddle;
import com.game.arkanoid.services.GameService;
import com.game.arkanoid.services.PaddleService;
import com.game.arkanoid.services.BallService;
import com.game.arkanoid.utils.Constants;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;

import java.net.URL;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;

public class GameController implements Initializable {

    @FXML
    private Pane gamePane;
    private GameService gameService;

    private final Set<KeyCode> activeKeys = new HashSet<>();


    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        gamePane.getChildren().addAll(gameService.getPaddleService().getPaddle().getNode(),
                                      gameService.getBallService().getBall().getNode());


        gamePane.setFocusTraversable(true);
        gamePane.setOnKeyPressed(event -> activeKeys.add(event.getCode()));
        gamePane.setOnKeyReleased(event -> activeKeys.remove(event.getCode()));

        Platform.runLater(() -> gamePane.requestFocus());

        startGameLoop();
    }

    private void startGameLoop() {
        gameService.startGameLoop(gamePane, activeKeys);
    }
    
    public void stopGame() {
        gameService.stop();
    }
}
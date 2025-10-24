package com.game.arkanoid.controller;

import com.game.arkanoid.models.GameState;
import com.game.arkanoid.models.InputState;
import com.game.arkanoid.services.GameService;
import com.game.arkanoid.view.BallRenderer;
import com.game.arkanoid.view.BricksRenderer;
import com.game.arkanoid.view.ExtraBallsRenderer;
import com.game.arkanoid.view.PaddleRenderer;
import com.game.arkanoid.view.PowerUpRenderer;
import com.game.arkanoid.view.SceneNavigator;
import java.util.HashSet;
import java.util.Set;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;

public final class GameController {

    @FXML
    private Pane gamePane;

    private final Set<KeyCode> activeKeys = new HashSet<>();
    private final GameService gameService;
    private final GameState gameState;
    private final SceneNavigator navigator;

    private BallRenderer ballRenderer;
    private PaddleRenderer paddleRenderer;
    private BricksRenderer bricksRenderer;
    private ExtraBallsRenderer extraBallsRenderer;
    private PowerUpRenderer powerUpRenderer;
    private AnimationTimer loop;

    public GameController(GameState gameState, GameService gameService, SceneNavigator navigator) {
        this.gameState = gameState;
        this.gameService = gameService;
        this.navigator = navigator;
    }

    @FXML
    public void initialize() {
        paddleRenderer = new PaddleRenderer(gamePane, gameState.paddle);
        ballRenderer = new BallRenderer(gamePane, gameState.ball);
        bricksRenderer = new BricksRenderer(gamePane, gameState.bricks);
        extraBallsRenderer = new ExtraBallsRenderer(gamePane);
        powerUpRenderer = new PowerUpRenderer(gamePane);

        gamePane.setOnKeyPressed(event -> {
            KeyCode code = event.getCode();
            if (code == KeyCode.ESCAPE) {
                navigator.showMenu();
                return;
            }
            if (code == KeyCode.P) {
                gameState.paused = !gameState.paused;
                return;
            }
            activeKeys.add(code);
        });

        gamePane.setOnKeyReleased(event -> activeKeys.remove(event.getCode()));
        gamePane.setFocusTraversable(true);
        Platform.runLater(gamePane::requestFocus);

        loop = new AnimationTimer() {
            private long last = -1;

            @Override
            public void handle(long now) {
                if (last < 0) {
                    last = now;
                    return;
                }

                double dt = (now - last) / 11_000_000.0;
                last = now;

                InputState in = readInput();
                gameService.update(gameState, in, dt, gamePane.getWidth(), gamePane.getHeight());

                paddleRenderer.render(gameState.paddle);
                ballRenderer.render(gameState.ball);
                extraBallsRenderer.render(gameState.extraBalls);
                powerUpRenderer.render(gameState.powerUps);
                bricksRenderer.render(gameState.bricks);
            }
        };

        loop.start();
    }

    private InputState readInput() {
        InputState in = new InputState();
        in.left = activeKeys.contains(KeyCode.LEFT) || activeKeys.contains(KeyCode.A);
        in.right = activeKeys.contains(KeyCode.RIGHT) || activeKeys.contains(KeyCode.D);
        in.launch = activeKeys.contains(KeyCode.SPACE);
        return in;
    }

    public void stop() {
        if (loop != null) {
            loop.stop();
        }
    }
}

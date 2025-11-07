package com.game.arkanoid.controller;

import com.game.arkanoid.models.GameState;
import com.game.arkanoid.models.InputState;
import com.game.arkanoid.services.GameService;
import com.game.arkanoid.view.*;

import java.util.HashSet;
import java.util.Set;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;

public final class GameController {

    @FXML private Button Pause;
    @FXML private Pane gamePane;
    @FXML private Pane bgGame;

    @FXML private Label livesLabel;
    @FXML private Label scoreLabel;
    @FXML private Label highScoreLabel;

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
        bgGame.setVisible(true);
        setLevelBackground(gameState.level);
        startGameLoop();
    }

    /**
     * 💡 Gán class CSS nền theo level.
     */
    private void setLevelBackground(int level) {
        // Xóa tất cả class cũ để tránh bị chồng style
        bgGame.getStyleClass().removeAll("level-1", "level-2", "level-3", "level-4", "level-5");

        switch (level) {
            case 2 -> bgGame.getStyleClass().add("level-2");
            case 3 -> bgGame.getStyleClass().add("level-3");
            case 4 -> bgGame.getStyleClass().add("level-4");
            case 5 -> bgGame.getStyleClass().add("level-5");
            default -> bgGame.getStyleClass().add("level-1"); // ⚠️ bạn viết nhầm 'bggame' ở bản cũ
        }
    }

    private void startGameLoop() {
        paddleRenderer = new PaddleRenderer(gamePane);
        ballRenderer = new BallRenderer(gamePane, gameState.ball);
        bricksRenderer = new BricksRenderer(gamePane, gameState.bricks);
        extraBallsRenderer = new ExtraBallsRenderer(gamePane);
        powerUpRenderer = new PowerUpRenderer(gamePane);

        // Lắng nghe phím
        gamePane.setOnKeyPressed(event -> {
            KeyCode code = event.getCode();
            if (code == KeyCode.ESCAPE) {
                navigator.showMenu();
                return;
            }
            if (code == KeyCode.P) {
                gameState.paused = !gameState.paused;
                navigator.showSettings();
                return;
            }
            activeKeys.add(code);
        });

        gamePane.setOnKeyReleased(event -> activeKeys.remove(event.getCode()));
        gamePane.setFocusTraversable(true);
        Platform.runLater(gamePane::requestFocus);

        // Chạy animation paddle
        paddleRenderer.playIntro();

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

                // Render từng thành phần
                paddleRenderer.render(gameState.paddle);
                ballRenderer.render(gameState.ball);
                extraBallsRenderer.render(gameState.extraBalls);
                powerUpRenderer.render(gameState.powerUps);
                bricksRenderer.render(gameState.bricks);

                // HUD
                if (livesLabel != null)
                    livesLabel.setText("1UP " + Math.max(0, gameState.lives));
                if (scoreLabel != null)
                    scoreLabel.setText(Integer.toString(gameState.score));
                if (highScoreLabel != null)
                    highScoreLabel.setText("HIGH SCORE 00000");

                // Kiểm tra trạng thái game
                if (gameState.gameOver) {
                    stop();
                    navigator.showGameOver();
                    return;
                }

                if (gameState.gameCompleted) {
                    stop();
                    navigator.showMenu();
                }
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
        if (loop != null) loop.stop();
    }

    @FXML
    private void onPause(ActionEvent event) {
        navigator.showSettings();
    }
}

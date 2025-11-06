package com.game.arkanoid.controller;

import com.game.arkanoid.events.GameEventBus;
import com.game.arkanoid.events.powerup.PowerUpExpiredEvent;
import com.game.arkanoid.events.powerup.PowerUpActivatedEvent;
import com.game.arkanoid.models.GameState;
import com.game.arkanoid.models.InputState;
import com.game.arkanoid.services.GameService;
import com.game.arkanoid.view.BallRenderer;
import com.game.arkanoid.view.BricksRenderer;
import com.game.arkanoid.view.ExtraBallsRenderer;
import com.game.arkanoid.view.PaddleRenderer;
import com.game.arkanoid.view.PowerUpRenderer;
import com.game.arkanoid.view.SceneNavigator;

import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;

public final class GameController {

    @FXML
    private Pane gamePane;

    private final Set<KeyCode> activeKeys = new HashSet<>();
    private final GameService gameService;
    private final GameState gameState;
    private final SceneNavigator navigator;
    private final List<GameEventBus.Subscription> subscriptions = new ArrayList<>();

    private BallRenderer ballRenderer;
    private PaddleRenderer paddleRenderer;
    private BricksRenderer bricksRenderer;
    private ExtraBallsRenderer extraBallsRenderer;
    private PowerUpRenderer powerUpRenderer;
    private AnimationTimer loop;

    @FXML private Label livesLabel;
    @FXML private Label scoreLabel;
    @FXML private Label highScoreLabel;

    public GameController(GameState gameState, GameService gameService, SceneNavigator navigator) {
        this.gameState = gameState;
        this.gameService = gameService;
        this.navigator = navigator;
    }

    @FXML
    public void initialize() {
        setUpRenderers();
        registerEventListeners();
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
        // Play paddle intro animation
        paddleRenderer.playIntro();
        startGameLoop();
    }

    private void registerEventListeners() {
        subscriptions.add(GameEventBus.getInstance().subscribe(PowerUpActivatedEvent.class, paddleRenderer::onPowerUpActivated));
        subscriptions.add(GameEventBus.getInstance().subscribe(PowerUpExpiredEvent.class, paddleRenderer::onPowerUpExpired));
    }

    private void startGameLoop() {
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

                updateHud();

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

    private void updateHud() {
        if (livesLabel != null) livesLabel.setText("1UP " + Math.max(0, gameState.lives));
        if (scoreLabel != null) scoreLabel.setText(Integer.toString(gameState.score));
        if (highScoreLabel != null) highScoreLabel.setText("HIGH SCORE 00000");
    }

    private void setUpRenderers() {
        paddleRenderer = new PaddleRenderer(gamePane);
        ballRenderer = new BallRenderer(gamePane, gameState.ball);
        bricksRenderer = new BricksRenderer(gamePane, gameState.bricks);
        extraBallsRenderer = new ExtraBallsRenderer(gamePane);
        powerUpRenderer = new PowerUpRenderer(gamePane);
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

        subscriptions.forEach(GameEventBus.Subscription::close);
        subscriptions.clear();
    }
}

package com.game.arkanoid.controller;

import com.game.arkanoid.models.*;
import com.game.arkanoid.services.GameService;
import com.game.arkanoid.utils.Constants;
import com.game.arkanoid.view.BallRenderer;
import com.game.arkanoid.view.PaddleRenderer;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;

import java.util.HashSet;
import java.util.Set;

public final class GameController {

    @FXML private Pane gamePane;

    // Input
    private final Set<KeyCode> activeKeys = new HashSet<>();

    // Core (injected)
    private final GameService gameService;
    private final GameState gameState;

    private GameService gameService;
    private GameState gameState;

    // Renderers (own JavaFX nodes)
    private BallRenderer ballRenderer;
    private PaddleRenderer paddleRenderer;
    private BricksRenderer bricksRenderer;
    // Game loop
    private AnimationTimer loop;
    public GameController(GameState gameState, GameService gameService) {
        this.gameState = gameState;
        this.gameService = gameService;
    }

    @FXML
    public void initialize() {
        
        // 2) Create renderers once (Pane is ready here)
        paddleRenderer = new PaddleRenderer(gamePane, gameState.paddle);
        ballRenderer   = new BallRenderer(gamePane, gameState.ball);
        bricksRenderer = new BricksRenderer(gamePane, gameState.bricks);

        //  Input wiring
        gamePane.setOnKeyPressed(e -> activeKeys.add(e.getCode()));
        gamePane.setOnKeyReleased(e -> activeKeys.remove(e.getCode()));
        gamePane.setFocusTraversable(true);

        //  Start the loop on the FX thread
        loop = new AnimationTimer() {
            long last = -1;
            @Override public void handle(long now) {
                if (last < 0) { last = now; return; }
                double dt = (now - last) / Constants.TIME_SCALE;
                last = now;

                InputState in = readInput();

                // Advance game logic (no JavaFX types inside)
                gameService.update(gameState, in, dt, gamePane.getWidth(), gamePane.getHeight());

                // Render: sync model -> nodes
                paddleRenderer.render(gameState.paddle);
                ballRenderer.render(gameState.ball);
                bricksRenderer.render(gameState.bricks);
            }
        };

        loop.start();
    }

    /** First-time layout: bottom-center the paddle. */
    private void layoutInitial() {
        double w = gamePane.getWidth()  > 0 ? gamePane.getWidth()  : gamePane.getPrefWidth();
        double h = gamePane.getHeight() > 0 ? gamePane.getHeight() : gamePane.getPrefHeight();

        double px = (w - gameState.paddle.getWidth()) / 2.0;
        double py = h - gameState.paddle.getHeight() - Constants.PADDLE_MARGIN_BOTTOM;
        gameState.paddle.setPosition(px, py);
    }

    /** On resize: keep paddle glued to bottom and clamp X.*/
    private void layoutOnResize() {
        double newW = gamePane.getWidth();
        double newH = gamePane.getHeight();

        // clamp paddle X in new width
        double maxX = Math.max(0, newW - gameState.paddle.getWidth());
        double clampedX = Math.max(0, Math.min(gameState.paddle.getX(), maxX));
        double py = newH - gameState.paddle.getHeight() - Constants.PADDLE_MARGIN_BOTTOM;

        gameState.paddle.setPosition(clampedX, py);

        // reflect to nodes immediately so it looks correct while resizing
        paddleRenderer.render(gameState.paddle);
        ballRenderer.render(gameState.ball);
    }

    private InputState readInput() {
        InputState in = new InputState();
        in.left   = activeKeys.contains(KeyCode.LEFT)  || activeKeys.contains(KeyCode.A);
        in.right  = activeKeys.contains(KeyCode.RIGHT) || activeKeys.contains(KeyCode.D);
        in.launch = activeKeys.contains(KeyCode.SPACE);
        return in;
    }

    public void stop() {
        if (loop != null) loop.stop();
    }
}

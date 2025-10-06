package com.game.arkanoid.controller;

import com.game.arkanoid.container.Container;
import com.game.arkanoid.models.*;
import com.game.arkanoid.services.GameService;
import com.game.arkanoid.view.*;
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

    // DI / game core
    private Container container;
    private GameService game;
    private GameState state;

    // Renderers (own JavaFX nodes)
    private BallRenderer ballRenderer;
    private PaddleRenderer paddleRenderer;

    // Game loop
    private AnimationTimer loop;
    public GameController(Container container) {
        this.container = container;
        this.game = container.getGameService();
        this.state = container.getGameState();
    }

    @FXML
    public void initialize() {
        // 1) Build composition root (you can inject via setContainer(...) instead if you prefer)
        container = new Container();
        game      = container.getGameService();
        state     = container.getGameState();

        // 2) Create renderers once (Pane is ready here)
        paddleRenderer = new PaddleRenderer(gamePane, state.paddle);
        ballRenderer   = new BallRenderer(gamePane, state.ball);

        // 3) Input wiring
        gamePane.setOnKeyPressed(e -> activeKeys.add(e.getCode()));
        gamePane.setOnKeyReleased(e -> activeKeys.remove(e.getCode()));
        gamePane.setFocusTraversable(true);
        Platform.runLater(gamePane::requestFocus); // ensure pane receives key events

        // 4) Start the loop on the FX thread
        loop = new AnimationTimer() {
            long last = -1;
            @Override public void handle(long now) {
                if (last < 0) { last = now; return; }
                double dt = (now - last) / 1_000_000_000.0;
                last = now;

                // Build per-frame input snapshot
                InputState in = readInput();

                // Advance game logic (no JavaFX types inside)
                game.update(state, in, dt, gamePane.getWidth(), gamePane.getHeight());

                // Render: sync model -> nodes
                paddleRenderer.render(state.paddle);
                ballRenderer.render(state.ball);
            }
        };
        loop.start();
    }

    private InputState readInput() {
        InputState in = new InputState();
        in.left   = activeKeys.contains(KeyCode.LEFT)  || activeKeys.contains(KeyCode.A);
        in.right  = activeKeys.contains(KeyCode.RIGHT) || activeKeys.contains(KeyCode.D);
        in.launch = activeKeys.contains(KeyCode.SPACE);
        // If you add pause in GameService: in.pause = activeKeys.contains(KeyCode.P);
        return in;
    }

    /** Optional: stop the loop when changing scenes/windows. */
    public void stop() {
        if (loop != null) loop.stop();
    }

    /** Optional alternative: inject a prebuilt Container from Main instead of new Container() in initialize(). */
    public void setContainer(Container container) {
        this.container = container;
        this.game = container.getGameService();
        this.state = container.getGameState();
    }
}

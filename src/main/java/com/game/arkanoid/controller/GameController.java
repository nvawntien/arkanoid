package com.game.arkanoid.controller;

import com.game.arkanoid.controller.SceneId;
import com.game.arkanoid.controller.SceneController;
import com.game.arkanoid.models.GameState;
import com.game.arkanoid.models.InputState;
import com.game.arkanoid.services.GameService;
import com.game.arkanoid.view.renderer.BallRenderer;
import com.game.arkanoid.view.renderer.BricksRenderer;
import com.game.arkanoid.view.renderer.ExtraBallsRenderer;
import com.game.arkanoid.view.renderer.PaddleRenderer;
import com.game.arkanoid.view.renderer.PowerUpRenderer;
import com.game.arkanoid.view.sound.SoundRenderer;
import com.game.arkanoid.view.transition.TransitionStrategy;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import javafx.animation.Animation;
import javafx.animation.AnimationTimer;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

public final class GameController {

    private static final double LIFE_ICON_WIDTH = 38.0;

    @FXML private StackPane rootStack;
    @FXML private Pane gamePane;
    @FXML private HBox lifeBox;
    @FXML private StackPane overlayLayer;
    @FXML private StackPane bannerLayer;
    @FXML private Label bannerLabel;
    @FXML private Label livesLabel;
    @FXML private Label scoreLabel;
    @FXML private Label highScoreLabel;

    private final Set<KeyCode> activeKeys = new HashSet<>();
    private final GameService gameService;
    private final GameState gameState;
    private final SceneController navigator;
    private final SoundRenderer soundService = SoundRenderer.getInstance();

    private BallRenderer ballRenderer;
    private PaddleRenderer paddleRenderer;
    private BricksRenderer bricksRenderer;
    private ExtraBallsRenderer extraBallsRenderer;
    private PowerUpRenderer powerUpRenderer;

    private AnimationTimer gameLoop;
    private Parent pauseOverlay;
    private Image lifeIcon;
    private SequentialTransition levelIntroSequence;

    private int lastLifeCount = Integer.MIN_VALUE;
    private int lastLevelObserved = Integer.MIN_VALUE;

    private boolean pauseTransitionRunning = false;

    public GameController(GameState gameState, GameService gameService, SceneController navigator) {
        this.gameState = gameState;
        this.gameService = gameService;
        this.navigator = navigator;
    }

    @FXML
    public void initialize() {
        setupRenderers();
        setupPauseOverlay();
        setupInputHandlers();
        initLifeIcons();
        updateHud();

        soundService.stopBgm("menu_bgm");
        soundService.loopBgm("level_bgm");

        startGameLoop();
        lastLevelObserved = gameState.level;
        startLevelIntro();
    }

    private void setupRenderers() {
        paddleRenderer = new PaddleRenderer(gamePane);
        ballRenderer = new BallRenderer(gamePane, gameState.ball);
        bricksRenderer = new BricksRenderer(gamePane, gameState.bricks);
        extraBallsRenderer = new ExtraBallsRenderer(gamePane);
        powerUpRenderer = new PowerUpRenderer(gamePane);

        gamePane.setFocusTraversable(true);
        Platform.runLater(gamePane::requestFocus);

        // Optional intro anim for paddle
        paddleRenderer.playIntro();
    }

    private void setupPauseOverlay() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/game/arkanoid/fxml/PauseView.fxml"));
            loader.setControllerFactory(cls -> {
                if (cls == PauseController.class) {
                    return new PauseController(this::resumeGame, this::restartLevel, this::exitToMenu);
                }
                throw new IllegalArgumentException("Unsupported pause controller type: " + cls);
            });
            pauseOverlay = loader.load();
            overlayLayer.getChildren().setAll(pauseOverlay);
            overlayLayer.setVisible(false);
            overlayLayer.setManaged(false);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load pause overlay", e);
        }
    }

    private void setupInputHandlers() {
        rootStack.addEventFilter(KeyEvent.KEY_PRESSED, this::handleGlobalKeyPressed);
        gamePane.setOnKeyPressed(this::handleGameKeyPressed);
        gamePane.setOnKeyReleased(event -> activeKeys.remove(event.getCode()));
    }

    private void initLifeIcons() {
        lifeIcon = new Image(getClass().getResourceAsStream("/com/game/arkanoid/images/paddle_life.png"));
        updateLifeIcons(gameState.lives);
        lastLifeCount = gameState.lives;
    }

    private void startGameLoop() {
        gameLoop = new AnimationTimer() {
            private long last = -1;

            @Override
            public void handle(long now) {
                if (last < 0) { last = now; return; }

                // ns -> s
                double dt = (now - last) / 11_000_000.0;
                last = now;

                InputState in = readInput();

                // NOTE: GameService API now requires paddleRenderer
                gameService.update(gameState, paddleRenderer, in, dt, gamePane.getWidth(), gamePane.getHeight());

                // Render current state
                paddleRenderer.render(gameState.paddle);
                ballRenderer.render(gameState.ball);
                extraBallsRenderer.render(gameState.extraBalls);
                powerUpRenderer.render(gameState.powerUps);
                bricksRenderer.render(gameState.bricks);

                // HUD and transitions
                updateHud();
                refreshLifeIcons();
                trackLevelTransition(); // detects level index change and triggers intro

                if (gameState.gameOver) {
                    stopLoopAndNavigate(SceneId.GAME_OVER, navigator.transitions().gameOverTransition());
                    return;
                }
                if (gameState.gameCompleted) {
                    stopLoopAndNavigate(SceneId.MENU, navigator.transitions().menuTransition());
                }
            }
        };
        gameLoop.start();
    }

    private void handleGlobalKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ESCAPE) {
            togglePause();
            event.consume();
        }
    }

    private void handleGameKeyPressed(KeyEvent event) {
        KeyCode code = event.getCode();
        if (code == KeyCode.P) {
            togglePause();
            event.consume();
            return;
        }
        if (gameState.paused) {
            event.consume();
            return;
        }
        activeKeys.add(code);
    }

    private InputState readInput() {
        InputState in = new InputState();
        if (gameState.paused) return in;
        in.left   = activeKeys.contains(KeyCode.LEFT)  || activeKeys.contains(KeyCode.A);
        in.right  = activeKeys.contains(KeyCode.RIGHT) || activeKeys.contains(KeyCode.D);
        in.launch = activeKeys.contains(KeyCode.SPACE);
        return in;
    }

    private void updateHud() {
        if (livesLabel != null)    livesLabel.setText("1UP " + Math.max(0, gameState.lives));
        if (scoreLabel != null)    scoreLabel.setText(Integer.toString(gameState.score));
        if (highScoreLabel != null) highScoreLabel.setText("HIGH SCORE 00000");
    }

    private void refreshLifeIcons() {
        if (gameState.lives != lastLifeCount) {
            updateLifeIcons(gameState.lives);
            lastLifeCount = gameState.lives;
        }
    }

    private void updateLifeIcons(int lives) {
        if (lifeIcon == null || lifeBox == null) return;
        lifeBox.getChildren().clear();
        int displayLives = Math.max(0, lives);
        for (int i = 0; i < displayLives; i++) {
            ImageView icon = new ImageView(lifeIcon);
            icon.setPreserveRatio(true);
            icon.setFitWidth(LIFE_ICON_WIDTH);
            icon.getStyleClass().add("life-icon");
            lifeBox.getChildren().add(icon);
        }
    }

    private void trackLevelTransition() {
        int currentLevel = gameState.level;
        if (currentLevel != lastLevelObserved) {
            lastLevelObserved = currentLevel;
            if (currentLevel > 0) {
                // GameService sets paused=true when advancing levels.
                startLevelIntro();
            }
        }
    }

    private void togglePause() {
        if (isPauseVisible()) resumeGame();
        else showPauseMenu();
    }

    private boolean isPauseVisible() {
        return overlayLayer.isVisible();
    }

    private void showPauseMenu() {
        if (pauseOverlay == null || isPauseVisible() || pauseTransitionRunning) return;

        pauseTransitionRunning = true;
        gameState.paused = true;
        activeKeys.clear();

        overlayLayer.setOpacity(1.0);
        overlayLayer.setVisible(true);
        overlayLayer.setManaged(true);

        pauseOverlay.setOpacity(0.0);
        FadeTransition ft = new FadeTransition(Duration.millis(200), pauseOverlay);
        ft.setFromValue(0.0);
        ft.setToValue(1.0);
        ft.setOnFinished(e -> pauseTransitionRunning = false);
        ft.play();

        soundService.playSfx("pause_on");
        soundService.fade("level_bgm", soundService.effectiveMusicVolume() * 0.35, Duration.millis(250));
        pauseOverlay.requestFocus();
    }

    private void hidePauseMenu() {
        if (!isPauseVisible() || pauseTransitionRunning) return;

        pauseTransitionRunning = true;
        FadeTransition ft = new FadeTransition(Duration.millis(180), pauseOverlay);
        ft.setFromValue(1.0);
        ft.setToValue(0.0);
        ft.setOnFinished(e -> {
            overlayLayer.setVisible(false);
            overlayLayer.setManaged(false);
            pauseTransitionRunning = false;
        });
        ft.play();
    }

    private void resumeGame() {
        hidePauseMenu();
        gameState.paused = false;
        activeKeys.clear(); 
        soundService.playSfx("pause_off");
        soundService.fade("level_bgm", soundService.effectiveMusicVolume(), Duration.millis(220));
        Platform.runLater(gamePane::requestFocus);
    }

    private void restartLevel() {
        hidePauseMenu();
        gameService.restartLevel(gameState);
        lastLevelObserved = gameState.level;
        lastLifeCount = Integer.MIN_VALUE;
        refreshLifeIcons();
        startLevelIntro();
        soundService.playSfx("menu_click");
        Platform.runLater(gamePane::requestFocus);
    }

    private void exitToMenu() {
        hidePauseMenu();
        stop();
        navigator.navigateTo(SceneId.MENU, navigator.transitions().menuTransition());
    }

    private void startLevelIntro() {
        if (levelIntroSequence != null) levelIntroSequence.stop();

        bannerLayer.setVisible(true);
        bannerLayer.setManaged(true);
        gameState.paused = true;

        PauseTransition showLevel = createBannerStep("LEVEL " + gameState.level, 0.8);
        PauseTransition countdown3 = createBannerStep("3", 0.6);
        PauseTransition countdown2 = createBannerStep("2", 0.6);
        PauseTransition countdown1 = createBannerStep("1", 0.6);

        levelIntroSequence = new SequentialTransition(showLevel, countdown3, countdown2, countdown1);
        levelIntroSequence.setOnFinished(e -> {
            bannerLayer.setVisible(false);
            bannerLayer.setManaged(false);
            gameService.startNextLevel(gameState); // unpause + running=true
        });
        levelIntroSequence.playFromStart();
    }

    private PauseTransition createBannerStep(String text, double seconds) {
        PauseTransition pt = new PauseTransition(Duration.seconds(seconds));
        pt.statusProperty().addListener((obs, oldStatus, newStatus) -> {
            if (newStatus == Animation.Status.RUNNING) {
                bannerLabel.setText(text);
            }
        });
        return pt;
    }

    private void stopLoopAndNavigate(SceneId target, TransitionStrategy transition) {
        stop();
        navigator.navigateTo(target, transition);
    }

    public void stop() {
        if (gameLoop != null) gameLoop.stop();
        if (levelIntroSequence != null) levelIntroSequence.stop();
        soundService.stopBgm("level_bgm");
    }
}

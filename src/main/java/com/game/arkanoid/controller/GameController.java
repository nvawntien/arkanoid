package com.game.arkanoid.controller;

import com.game.arkanoid.container.AppContext;
import com.game.arkanoid.events.GameEventBus;
import com.game.arkanoid.events.game.LevelClearedEvent;
import com.game.arkanoid.models.GameState;
import com.game.arkanoid.models.GameStateSnapshot;
import com.game.arkanoid.models.InputState;
import com.game.arkanoid.models.User;
import com.game.arkanoid.services.GameService;
import com.game.arkanoid.view.renderer.BallRenderer;
import com.game.arkanoid.view.renderer.BulletRenderer;
import com.game.arkanoid.view.renderer.BricksRenderer;
import com.game.arkanoid.view.renderer.ExtraBallsRenderer;
import com.game.arkanoid.view.renderer.PaddleRenderer;
import com.game.arkanoid.view.renderer.PowerUpRenderer;
import com.game.arkanoid.view.renderer.LifeRenderer;

import java.util.List;

import com.game.arkanoid.events.powerup.PowerUpActivatedEvent;
import com.game.arkanoid.events.powerup.PowerUpExpiredEvent;
import com.game.arkanoid.events.sound.GameBGMSoundEvent;
import com.game.arkanoid.view.transition.TransitionStrategy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
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

/**
 * Main controller for the in-game scene.
 * Handles game loop, input, rendering, events, transitions, and pause menu.
 */
public final class GameController {

    // --- FXML UI Components ---
    @FXML private StackPane rootStack;
    @FXML private Pane gamePane;
    @FXML private HBox lifeBox;
    @FXML private StackPane overlayLayer;
    @FXML private StackPane bannerLayer;
    @FXML private Label livesLabel;
    @FXML private Label bannerLabel;
    @FXML private Label scoreLabel;
    @FXML private Label highScoreLabel;

    // --- Core Dependencies ---
    private final GameService gameService;
    private final GameState gameState;
    private final SceneController navigator;
    private final Set<KeyCode> activeKeys = new HashSet<>();
    private final List<GameEventBus.Subscription> subscriptions = new ArrayList<>();

    // --- Rendering Components ---
    private BallRenderer ballRenderer;
    private PaddleRenderer paddleRenderer;
    private BricksRenderer bricksRenderer;
    private ExtraBallsRenderer extraBallsRenderer;
    private PowerUpRenderer powerUpRenderer;
    private BulletRenderer bulletRenderer;
    private LifeRenderer lifeRenderer;

    // --- State Tracking ---
    private AnimationTimer loop;
    private SequentialTransition levelIntroSequence;
    private Parent pauseOverlay;
    private int lastLevelObserved = Integer.MIN_VALUE;

    // Constructor ------------------------------------------------------------
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
        updateHud();
        registerEventListeners();

        startGameLoop();
        GameEventBus.getInstance().publish(new GameBGMSoundEvent());
        lastLevelObserved = gameState.level;
        startLevelIntro();
    }

    // ======================================================================
    // region 1. GAME LOOP (Core runtime logic)
    // ======================================================================

    private void startGameLoop() {
        loop = new AnimationTimer() {
            private long last = -1;

            @Override
            public void handle(long now) {
                if (last < 0) { last = now; return; }

                double dt = (now - last) / 11_000_000.0; // ns to ms
                last = now;

                InputState in = readInput();
                gameService.update(gameState, in, dt, gamePane.getWidth(), gamePane.getHeight());
                // Render updated state
                paddleRenderer.render(gameState.paddle);
                ballRenderer.render(gameState.ball);
                extraBallsRenderer.render(gameState.extraBalls);
                powerUpRenderer.render(gameState.powerUps);
                bulletRenderer.render(gameState.bullets);
                bricksRenderer.render(gameState.bricks);
                lifeRenderer.render(gameState.lives);
                // Update hud
                updateHud();
                trackLevelTransition();

                // --- Transition to other scenes ---
                if (gameState.gameOver) {
                    // persist bests and clear in-progress
                    User u = AppContext.getInstance().getCurrentUser();
                    if (u != null) {
                        int bestRound = Math.max(u.getBestRound(), gameState.level);
                        int bestScore = Math.max(u.getBestScore(), gameState.score);
                        AppContext.getInstance().db().updateBest(u.getId(), bestRound, bestScore);
                        AppContext.getInstance().db().clearInProgress(u.getId());
                    }
                    stopLoopAndNavigate(SceneId.GAME_OVER, navigator.transitions().gameOverTransition());
                    return;
                }
                if (gameState.gameCompleted) {
                    stopLoopAndNavigate(SceneId.MENU, navigator.transitions().menuTransition());
                }
            }
        };
        loop.start();
    }

    private InputState readInput() {
        InputState in = new InputState();
        if (gameState.paused) return in;
        in.left   = activeKeys.contains(KeyCode.LEFT)  || activeKeys.contains(KeyCode.A);
        in.right  = activeKeys.contains(KeyCode.RIGHT) || activeKeys.contains(KeyCode.D);
        in.launch = activeKeys.contains(KeyCode.SPACE);
        in.fire   = activeKeys.contains(KeyCode.SPACE);
        return in;
    }

    // endregion


    // ======================================================================
    // region 2. INPUT HANDLING
    // ======================================================================

    private void setupInputHandlers() {
        rootStack.addEventFilter(KeyEvent.KEY_PRESSED, this::handleGlobalKeyPressed);
        gamePane.setOnKeyPressed(this::handleGameKeyPressed);
        gamePane.setOnKeyReleased(event -> activeKeys.remove(event.getCode()));
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

    // endregion


    // ======================================================================
    // region 3. LEVEL TRANSITION + EVENT HANDLING
    // ======================================================================

   // ...existing code...
    private void registerEventListeners() {
        subscriptions.add(GameEventBus.getInstance().subscribe(LevelClearedEvent.class, this::onLevelCleared));

        // chạy trên FX thread, log để debug nếu handler nhận event hay không
        subscriptions.add(GameEventBus.getInstance().subscribe(PowerUpActivatedEvent.class, paddleRenderer::onPowerUpActivated));

        subscriptions.add(GameEventBus.getInstance().subscribe(PowerUpExpiredEvent.class, paddleRenderer::onPowerUpExpired));
    }
// ...existing code...  

    private void onLevelCleared(LevelClearedEvent event) {
        Platform.runLater(() -> {
            if (loop != null) loop.stop();
            gameState.paused = true;

            bannerLayer.setVisible(true);
            bannerLayer.setManaged(true);
            // Apply bigger style for cleared banner; remove countdown style
            bannerLabel.getStyleClass().remove("level-banner-countdown");
            bannerLabel.getStyleClass().remove("level-banner-start");
            if (!bannerLabel.getStyleClass().contains("level-banner-cleared")) {
                bannerLabel.getStyleClass().add("level-banner-cleared");
            }
            bannerLabel.setText("LEVEL " + event.level() + " CLEARED!");

            PauseTransition pause = new PauseTransition(Duration.seconds(1.2));
            pause.setOnFinished(e -> {
                int nextLevel = gameState.level + 1;

                // Ensure nothing persists to next round
                gameState.bullets.clear();
                gameState.powerUps.clear();
                gameState.activePowerUps.clear();
                gameState.extraBalls.clear();
                gameState.laserCooldown = 0.0;

                gameService.loadNextLevel(gameState);
                if (gameState.gameCompleted) {
                    stopLoopAndNavigate(SceneId.MENU, navigator.transitions().menuTransition());
                    return;
                }

                // 🔹 Chuyển qua Round mới bằng SceneController
                navigator.showGameRound(nextLevel);
            });
            pause.play();
        });
    }




    private void trackLevelTransition() {
        int currentLevel = gameState.level;
        if (currentLevel != lastLevelObserved) {
            lastLevelObserved = currentLevel;
            if (currentLevel > 0) startLevelIntro();
        }
    }

    private void startLevelIntro() {
        if (levelIntroSequence != null) {
            levelIntroSequence.stop();
        }

        System.out.println("[GameController] Starting level intro for level " + gameState.level);

        bannerLayer.setVisible(true);
        bannerLayer.setManaged(true);
        gameState.paused = true;

        PauseTransition showLevel = createBannerStep("LEVEL " + gameState.level, 0.8);
        PauseTransition countdown3 = createBannerStep("3", 0.6);
        PauseTransition countdown2 = createBannerStep("2", 0.6);
        PauseTransition countdown1 = createBannerStep("1", 0.6);

        levelIntroSequence = new SequentialTransition(showLevel, countdown3, countdown2, countdown1);
        levelIntroSequence.setOnFinished(e -> {
            System.out.println("[GameController] Countdown finished → startNextLevel()");
            bannerLayer.setVisible(false);
            bannerLayer.setManaged(false);
            gameService.startNextLevel(gameState);
        });
        levelIntroSequence.playFromStart();
    }


    private PauseTransition createBannerStep(String text, double seconds) {
        PauseTransition pt = new PauseTransition(Duration.seconds(seconds));
        pt.statusProperty().addListener((obs, oldStatus, newStatus) -> {
            if (newStatus == Animation.Status.RUNNING) {
                bannerLabel.setText(text);

                // Determine which banner variant to show
                boolean isCountdown = text.chars().allMatch(Character::isDigit);
                boolean isCleared = text.contains("CLEARED!");
                boolean isLevelStart = text.startsWith("LEVEL ") && text.length() > 6 &&
                        text.substring(6).chars().allMatch(Character::isDigit);

                // Reset all variant classes first
                bannerLabel.getStyleClass().remove("level-banner-countdown");
                bannerLabel.getStyleClass().remove("level-banner-cleared");
                bannerLabel.getStyleClass().remove("level-banner-start");

                // Apply appropriate class
                if (isCountdown) {
                    if (!bannerLabel.getStyleClass().contains("level-banner-countdown")) {
                        bannerLabel.getStyleClass().add("level-banner-countdown");
                    }
                } else if (isCleared) {
                    if (!bannerLabel.getStyleClass().contains("level-banner-cleared")) {
                        bannerLabel.getStyleClass().add("level-banner-cleared");
                    }
                } else if (isLevelStart) {
                    if (!bannerLabel.getStyleClass().contains("level-banner-start")) {
                        bannerLabel.getStyleClass().add("level-banner-start");
                    }
                }
            }
        });
        return pt;
    }

    // endregion


    // ======================================================================
    // region 4. PAUSE MENU & NAVIGATION
    // ======================================================================

    private void togglePause() {
        if (isPauseVisible()) resumeGame();
        else showPauseMenu();
    }

    private boolean isPauseVisible() {
        return overlayLayer.isVisible();
    }

    private void showPauseMenu() {
        if (pauseOverlay == null || isPauseVisible()) return;
        gameState.paused = true;
        activeKeys.clear();

        overlayLayer.setOpacity(1.0);
        overlayLayer.setVisible(true);
        overlayLayer.setManaged(true);
        pauseOverlay.setOpacity(0.0);

        FadeTransition ft = new FadeTransition(Duration.millis(200), pauseOverlay);
        ft.setFromValue(0.0);
        ft.setToValue(1.0);
        ft.play();

        // region SOUND - Pause feedback
       // soundService.playSfx("pause_on");
        //soundService.fade("level_bgm", soundService.effectiveMusicVolume() * 0.35, Duration.millis(250));
        // endregion

        pauseOverlay.requestFocus();
    }

    private void hidePauseMenu() {
        if (!isPauseVisible()) return;

        FadeTransition ft = new FadeTransition(Duration.millis(180), pauseOverlay);
        ft.setFromValue(1.0);
        ft.setToValue(0.0);
        ft.setOnFinished(e -> {
            overlayLayer.setVisible(false);
            overlayLayer.setManaged(false);
        });
        ft.play();
    }

    // TODO: CHECK
    private void resumeGame() {
        hidePauseMenu();
        // show 3-2-1 countdown before resuming
        bannerLayer.setVisible(true);
        bannerLayer.setManaged(true);

        PauseTransition three = createBannerStep("3", 0.6);
        PauseTransition two = createBannerStep("2", 0.6);
        PauseTransition one = createBannerStep("1", 0.6);
        SequentialTransition seq = new SequentialTransition(three, two, one);
        seq.setOnFinished(e -> {
            bannerLayer.setVisible(false);
            bannerLayer.setManaged(false);
            gameState.paused = false;
            Platform.runLater(gamePane::requestFocus);
        });
        seq.playFromStart();
    }

    private void restartLevel() {
        hidePauseMenu();
        gameService.restartLevel(gameState);
        lastLevelObserved = gameState.level;
        lifeRenderer.reset();
        startLevelIntro();

        // region SOUND
        //soundService.playSfx("menu_click");
        // endregion

        Platform.runLater(gamePane::requestFocus);
    }

    private void exitToMenu() {
        hidePauseMenu();
        // Save in-progress game state for current user
        User u = AppContext.getInstance().getCurrentUser();
        if (u != null) {
            GameStateSnapshot snap = GameStateSnapshot.from(gameState);
            AppContext.getInstance().db().saveInProgress(u.getId(), snap);
        }
        stop();
        navigator.navigateTo(SceneId.MENU, navigator.transitions().menuTransition());
    }

    // endregion


    // ======================================================================
    // region 5. RENDERING & HUD
    // ======================================================================

    private void setupRenderers() {
        paddleRenderer = new PaddleRenderer(gamePane);
        ballRenderer = new BallRenderer(gamePane, gameState.ball);
        bricksRenderer = new BricksRenderer(gamePane, gameState.bricks);
        extraBallsRenderer = new ExtraBallsRenderer(gamePane);
        powerUpRenderer = new PowerUpRenderer(gamePane);
        bulletRenderer = new BulletRenderer(gamePane);
        lifeRenderer = new LifeRenderer(lifeBox);

        gamePane.setFocusTraversable(true);
        Platform.runLater(gamePane::requestFocus);
        paddleRenderer.playIntro();
    }


    private void updateHud() {
        if (livesLabel != null) livesLabel.setText("1UP " + Math.max(0, gameState.lives));
        if (scoreLabel != null) scoreLabel.setText(Integer.toString(gameState.score));
        if (highScoreLabel != null) highScoreLabel.setText("HIGH SCORE 00000");
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

    // endregion


    // ======================================================================
    // region 6. STOP / CLEANUP
    // ======================================================================

    private void stopLoopAndNavigate(SceneId target, TransitionStrategy transition) {
        stop();
        navigator.navigateTo(target, transition);
    }

    public void stop() {
        if (loop != null) loop.stop();
        if (levelIntroSequence != null) levelIntroSequence.stop();
        //soundService.stopBgm("level_bgm");
        subscriptions.forEach(GameEventBus.Subscription::close);
        subscriptions.clear();
    }

    // endregion

    // ============================================================
    // region 7. CONTINUE SUPPORT (apply snapshot + countdown)
    // ============================================================

    public void applySnapshot(GameStateSnapshot snapshot) {
        // Ensure correct level is loaded before applying: handled by SceneController
        snapshot.applyTo(gameState);
        updateHud();
        lifeRenderer.reset();
    }

    public void resumeWithCountdown() {
        bannerLayer.setVisible(true);
        bannerLayer.setManaged(true);
        PauseTransition three = createBannerStep("3", 0.6);
        PauseTransition two = createBannerStep("2", 0.6);
        PauseTransition one = createBannerStep("1", 0.6);
        SequentialTransition seq = new SequentialTransition(three, two, one);
        seq.setOnFinished(e -> {
            bannerLayer.setVisible(false);
            bannerLayer.setManaged(false);
            gameService.startNextLevel(gameState); // simply resume running
        });
        seq.playFromStart();
    }
    // endregion
}



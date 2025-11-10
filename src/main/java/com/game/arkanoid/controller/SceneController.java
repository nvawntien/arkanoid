package com.game.arkanoid.controller;

import com.game.arkanoid.container.Container;
import com.game.arkanoid.utils.Constants;
import com.game.arkanoid.view.transition.TransitionManager;
import com.game.arkanoid.view.transition.TransitionStrategy;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Central scene navigation helper that also plays animated transitions.
 */
public final class SceneController {

    // --- Core fields ---
    private final Stage stage;
    private final TransitionManager transitionManager = new TransitionManager();
    private GameController activeGameController;

    // --- Constructor ---
    public SceneController(Stage stage) {
        this.stage = Objects.requireNonNull(stage, "stage");
    }

    // ===============================================================
    //  TRANSITION & NAVIGATION HELPERS
    // ===============================================================

    /** Expose transition manager so controllers can request custom strategies. */
    public TransitionManager transitions() {
        return transitionManager;
    }

    /** Navigate to a scene with a custom transition strategy. */
    public void navigateTo(SceneId sceneId, TransitionStrategy transition) {
        switch (sceneId) {
            case LOGIN -> showLogin(transition);
            case SIGNUP -> showSignup(transition);
            case MENU -> showMenu(transition);
            case SETTINGS -> showSettings(transition);
            case GAME -> showGame(transition);
            case GAME_OVER -> showGameOver(transition);
            case RANKINGS -> showRankings(transition);
            default -> throw new IllegalArgumentException("Unhandled scene: " + sceneId);
        }
    }

    /** Set the current scene with a given transition. */
    private void setScene(SceneId sceneId, Parent root, TransitionStrategy transition) {
        Scene scene = new Scene(root, Constants.GAME_WIDTH, Constants.GAME_HEIGHT);
        stage.setScene(scene);
        Platform.runLater(() -> transitionManager.play(scene.getRoot(), transition, null));
    }

    /** Stop any currently active game before changing scenes. */
    private void stopActiveGame() {
        if (activeGameController != null) {
            activeGameController.stop();
            activeGameController = null;
        }
    }

    // ===============================================================
    //  MENU / SETTINGS / RANKINGS / LOGIN / SIGNUP SCENES
    // ===============================================================

    /** Show the main menu with default transition. */
    public void showMenu() {
        showMenu(transitionManager.menuTransition());
    }

    private void showMenu(TransitionStrategy transition) {
        stopActiveGame();
        Parent root = load("/com/game/arkanoid/fxml/MenuView.fxml", loader -> {
            loader.setControllerFactory(cls -> {
                if (cls == MenuController.class) {
                    return new MenuController(this);
                }
                throw buildUnknownController(cls);
            });
        });
        setScene(SceneId.MENU, root, transition);
    }

    /** Show the settings scene with default transition. */
    public void showSettings() {
        showSettings(transitionManager.settingsTransition());
    }

    private void showSettings(TransitionStrategy transition) {
        stopActiveGame();
        Parent root = load("/com/game/arkanoid/fxml/SettingsView.fxml", loader -> {
            loader.setControllerFactory(cls -> {
                if (cls == SettingsController.class) {
                    return new SettingsController(this);
                }
                throw buildUnknownController(cls);
            });
        });
        setScene(SceneId.SETTINGS, root, transition);
    }

    /** Show the rankings scene with default transition. */
    public void showRankings() {
        showRankings(transitionManager.menuTransition());
    }

    private void showRankings(TransitionStrategy transition) {
        stopActiveGame();
        Parent root = load("/com/game/arkanoid/fxml/RankingsView.fxml", loader -> {
            loader.setControllerFactory(cls -> {
                if (cls == RankingsController.class) return new RankingsController(this);
                throw buildUnknownController(cls);
            });
        });
        setScene(SceneId.RANKINGS, root, transition);
    }

    /** Show the login screen. */
    public void showLogin() {
        showLogin(transitionManager.menuTransition());
    }

    private void showLogin(TransitionStrategy transition) {
        stopActiveGame();
        Parent root = load("/com/game/arkanoid/fxml/LoginView.fxml", loader -> {
            loader.setControllerFactory(cls -> {
                if (cls == LoginController.class) return new LoginController(this);
                throw buildUnknownController(cls);
            });
        });
        setScene(SceneId.LOGIN, root, transition);
    }

    /** Show the signup screen. */
    public void showSignup() {
        showSignup(transitionManager.menuTransition());
    }

    private void showSignup(TransitionStrategy transition) {
        stopActiveGame();
        Parent root = load("/com/game/arkanoid/fxml/SignupView.fxml", loader -> {
            loader.setControllerFactory(cls -> {
                if (cls == SignupController.class) return new SignupController(this);
                throw buildUnknownController(cls);
            });
        });
        setScene(SceneId.SIGNUP, root, transition);
    }

    // ===============================================================
    //  GAMEPLAY SCENES (NORMAL, ROUND, SNAPSHOT, GAME OVER)
    // ===============================================================

    /** Show the gameplay scene with default transition. */
    public void showGame() {
        // Start from Round 1 using the new per-round FXMLs
        showGameRound(1);
    }

    private void showGame(TransitionStrategy transition) {
        // Keep compatibility if explicitly called, but prefer showGameRound
        stopActiveGame();
        Container container = Container.getInstance();
        Parent root = load("/com/game/arkanoid/fxml/Round1View.fxml", loader -> {
            loader.setControllerFactory(cls -> {
                if (cls == GameController.class) {
                    return new GameController(container.getGameState(), container.getGameService(), this);
                }
                throw buildUnknownController(cls);
            });
        });
        setScene(SceneId.GAME, root, transition);
    }

    /** Show the given game round (Round1View.fxml, Round2View.fxml, etc.). */
    public void showGameRound(int round) {
        stopActiveGame();
        Container container = Container.getInstance();
        String path = String.format("/com/game/arkanoid/fxml/Round%dView.fxml", round);

        Parent root = load(path, loader -> {
            loader.setControllerFactory(cls -> {
                if (cls == GameController.class) {
                    return new GameController(container.getGameState(), container.getGameService(), this);
                }
                throw buildUnknownController(cls);
            });
        });

        setScene(SceneId.GAME, root, transitionManager.levelBannerTransition());
    }

    /** Show the game over scene with default transition. */
    public void showGameOver() {
        showGameOver(transitionManager.gameOverTransition());
    }

    private void showGameOver(TransitionStrategy transition) {
        stopActiveGame();
        Parent root = load("/com/game/arkanoid/fxml/GameOverView.fxml", loader -> {
            loader.setControllerFactory(cls -> {
                if (cls == GameOverController.class) {
                    return new GameOverController(this);
                }
                throw buildUnknownController(cls);
            });
        });
        setScene(SceneId.GAME_OVER, root, transition);
    }

    /** Continue from paused state (placeholder). */
    public void continueGame() {
        // Implementation can resume an active game session later
    }

    /** Load the requested level and then apply snapshot + resume countdown. */
    public void startGameFromSnapshot(com.game.arkanoid.models.GameStateSnapshot snapshot) {
        int round = Math.max(1, snapshot.currentLevel);
        stopActiveGame();
        Container container = Container.getInstance();
        String path = String.format("/com/game/arkanoid/fxml/Round%dView.fxml", round);

        Parent root = load(path, loader -> {
            loader.setControllerFactory(cls -> {
                if (cls == GameController.class) {
                    return new GameController(container.getGameState(), container.getGameService(), this);
                }
                throw buildUnknownController(cls);
            });
        });

        setScene(SceneId.GAME, root, transitionManager.gameTransition());
        if (activeGameController != null) {
            // apply snapshot then run resume countdown
            activeGameController.applySnapshot(snapshot);
            activeGameController.resumeWithCountdown();
        }
    }

    // ===============================================================
    //  SAVE / EXIT LOGIC
    // ===============================================================

    /** Best-effort save of in-progress game when app closes. */
    public void saveInProgressIfAny() {
        if (activeGameController == null) return;
        if (!activeGameController.isResumable()) return;

        com.game.arkanoid.container.AppContext app = com.game.arkanoid.container.AppContext.getInstance();
        com.game.arkanoid.models.User u = app.getCurrentUser();
        if (u == null) return;

        com.game.arkanoid.models.GameStateSnapshot snap = activeGameController.captureSnapshot();
        try {
            app.db().saveInProgress(u.getId(), snap).join();
        } catch (Exception ignore) {
            // ignore on shutdown
        }
    }

    /** Exit the application. */
    public void exit() {
        stage.close();
    }

    // ===============================================================
    //  FXML LOADING UTILITIES
    // ===============================================================

    /** Generic FXML loader with controller binding. */
    private Parent load(String resource, Consumer<FXMLLoader> configurer) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(resource));
        configurer.accept(loader);
        try {
            Parent parent = loader.load();
            Object controller = loader.getController();
            if (controller instanceof GameController gc) {
                this.activeGameController = gc;
            }
            return parent;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load " + resource, e);
        }
    }

    /** Throw helpful error when unknown controller type requested. */
    private RuntimeException buildUnknownController(Class<?> cls) {
        return new IllegalArgumentException("Unsupported controller request: " + cls.getName());
    }
}

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
 * Central controller responsible for navigating between scenes and
 * playing visual transitions in the Arkanoid game.
 */
public final class SceneController {

    // --- Core fields ---
    /** The primary stage of the application. */
    private final Stage stage;

    /** Manages animated transitions between scenes. */
    private final TransitionManager transitionManager = new TransitionManager();

    /** The currently active game controller instance, if any. */
    private GameController activeGameController;

    // --- Constructor ---
    /**
     * Creates a new SceneController bound to the given stage.
     *
     * @param stage the main application stage (must not be null)
     */
    public SceneController(Stage stage) {
        this.stage = Objects.requireNonNull(stage, "stage");
    }

    // ===============================================================
    //  TRANSITION & NAVIGATION HELPERS
    // ===============================================================

    /**
     * Returns the TransitionManager used by this controller.
     *
     * @return the transition manager
     */
    public TransitionManager transitions() {
        return transitionManager;
    }

    /**
     * Navigates to a specific scene using the given transition strategy.
     *
     * @param sceneId     the target scene identifier
     * @param transition  the transition strategy to apply
     */
    public void navigateTo(SceneId sceneId, TransitionStrategy transition) {
        switch (sceneId) {
            case LOGIN -> showLogin(transition);
            case SIGNUP -> showSignup(transition);
            case MENU -> showMenu(transition);
            case SETTINGS -> showSettings(transition);
            case GAME -> showGame(transition);
            case GAME_OVER -> showGameOver(transition);
            case WIN -> showWin(transition);
            case RANKINGS -> showRankings(transition);
            default -> throw new IllegalArgumentException("Unhandled scene: " + sceneId);
        }
    }

    /**
     * Sets the current scene and applies a transition animation.
     *
     * @param sceneId     the logical identifier of the scene
     * @param root        the root node of the FXML layout
     * @param transition  the transition strategy to apply
     */
    private void setScene(SceneId sceneId, Parent root, TransitionStrategy transition) {
        Scene scene = new Scene(root, Constants.GAME_WIDTH, Constants.GAME_HEIGHT);
        stage.setScene(scene);
        Platform.runLater(() -> transitionManager.play(scene.getRoot(), transition, null));
    }

    /**
     * Stops the currently active game if one exists.
     * Ensures no background updates continue when changing scenes.
     */
    private void stopActiveGame() {
        if (activeGameController != null) {
            activeGameController.stop();
            activeGameController = null;
        }
    }

    // ===============================================================
    //  MENU / SETTINGS / RANKINGS / LOGIN / SIGNUP SCENES
    // ===============================================================

    /** Displays the main menu using the default transition. */
    public void showMenu() {
        showMenu(transitionManager.menuTransition());
    }

    /** Displays the main menu with a specific transition. */
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

    /** Displays the settings scene using the default transition. */
    public void showSettings() {
        showSettings(transitionManager.settingsTransition());
    }

    /** Displays the settings scene with a specific transition. */
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

    /** Displays the rankings scene using the default transition. */
    public void showRankings() {
        showRankings(transitionManager.menuTransition());
    }

    /** Displays the rankings scene with a specific transition. */
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

    /** Displays the login screen using the default transition. */
    public void showLogin() {
        showLogin(transitionManager.menuTransition());
    }

    /** Displays the login screen with a specific transition. */
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

    /** Displays the signup screen using the default transition. */
    public void showSignup() {
        showSignup(transitionManager.menuTransition());
    }

    /** Displays the signup screen with a specific transition. */
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

    /** Displays the default gameplay scene (starting from round 1). */
    public void showGame() {
        showGameRound(1);
    }

    /** Displays the gameplay scene with a specific transition. */
    private void showGame(TransitionStrategy transition) {
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

    /**
     * Displays the specified game round (e.g., Round1View.fxml, Round2View.fxml).
     *
     * @param round the round number to display
     */
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

    /** Displays the Game Over screen using the default transition. */
    public void showGameOver() {
        showGameOver(transitionManager.gameOverTransition());
    }

    /** Displays the Game Over screen with a specific transition. */
    private void showGameOver(TransitionStrategy transition) {
        final int finalScore;

        if (activeGameController != null) {
            finalScore = activeGameController.getScore();
            activeGameController.stop();
            activeGameController = null;
        } else {
            finalScore = 0;
        }

        Parent root = load("/com/game/arkanoid/fxml/GameOverView.fxml", loader -> {
            loader.setControllerFactory(cls -> {
                if (cls == GameOverController.class) {
                    return new GameOverController(this, finalScore);
                }
                throw buildUnknownController(cls);
            });
        });

        setScene(SceneId.GAME_OVER, root, transition);
    }

    /** Displays the victory (Win) screen using the default transition. */
    public void showWin() {
        showWin(transitionManager.winTransition());
    }

    /** Displays the victory (Win) screen with a specific transition. */
    private void showWin(TransitionStrategy transition) {
        final int finalScore;
        if (activeGameController != null) {
            finalScore = activeGameController.getScore();
            activeGameController.stop();
            activeGameController = null;
        } else {
            finalScore = 0;
        }

        Parent root = load("/com/game/arkanoid/fxml/WinView.fxml", loader -> {
            loader.setControllerFactory(cls -> {
                if (cls == WinController.class) {
                    return new WinController(this, finalScore);
                }
                throw buildUnknownController(cls);
            });
        });

        setScene(SceneId.WIN, root, transition);
    }

    /** Resumes the current game (if paused). Placeholder for future use. */
    public void continueGame() {
        // Implementation can resume an active game session later
    }

    /**
     * Loads a saved snapshot and resumes gameplay from that state.
     *
     * @param snapshot the saved game state snapshot
     */
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
            activeGameController.applySnapshot(snapshot);
            activeGameController.resumeWithCountdown();
        }
    }

    // ===============================================================
    //  SAVE / EXIT LOGIC
    // ===============================================================

    /** Attempts to save the current in-progress game before exiting. */
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

    /** Closes the application window. */
    public void exit() {
        stage.close();
    }

    // ===============================================================
    //  FXML LOADING UTILITIES
    // ===============================================================

    /**
     * Loads an FXML file and configures its controller factory.
     *
     * @param resource   the FXML resource path
     * @param configurer consumer to configure the FXMLLoader
     * @return the loaded root node
     */
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

    /**
     * Builds a descriptive exception when an unexpected controller type is requested.
     *
     * @param cls the unknown controller class
     * @return a runtime exception describing the issue
     */
    private RuntimeException buildUnknownController(Class<?> cls) {
        return new IllegalArgumentException("Unsupported controller request: " + cls.getName());
    }
}

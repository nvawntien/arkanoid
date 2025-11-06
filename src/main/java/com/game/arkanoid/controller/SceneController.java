package com.game.arkanoid.controller.infra;

import com.game.arkanoid.container.Container;
import com.game.arkanoid.controller.GameController;
import com.game.arkanoid.controller.GameOverController;
import com.game.arkanoid.controller.MenuController;
import com.game.arkanoid.controller.SettingsController;
import com.game.arkanoid.view.transition.TransitionManager;
import com.game.arkanoid.view.transition.TransitionStrategy;
import com.game.arkanoid.utils.Constants;
import java.io.IOException;
import java.util.Objects;
import java.util.function.Consumer;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Central scene navigation helper that also plays animated transitions.
 */
public final class SceneNavigator {

    private final Stage stage;
    private final TransitionManager transitionManager = new TransitionManager();
    private GameController activeGameController;

    public SceneNavigator(Stage stage) {
        this.stage = Objects.requireNonNull(stage, "stage");
    }

    /** Expose transition manager so controllers can request custom strategies. */
    public TransitionManager transitions() {
        return transitionManager;
    }

    /** Navigate to a scene with a custom transition strategy. */
    public void navigateTo(SceneId sceneId, TransitionStrategy transition) {
        switch (sceneId) {
            case MENU -> showMenu(transition);
            case SETTINGS -> showSettings(transition);
            case GAME -> showGame(transition);
            case GAME_OVER -> showGameOver(transition);
            default -> throw new IllegalArgumentException("Unhandled scene: " + sceneId);
        }
    }

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

    /** Show the gameplay scene with default transition. */
    public void showGame() {
        showGame(transitionManager.gameTransition());
    }

    private void showGame(TransitionStrategy transition) {
        stopActiveGame();
        Container container = new Container();
        Parent root = load("/com/game/arkanoid/fxml/GameView.fxml", loader -> {
            loader.setControllerFactory(cls -> {
                if (cls == GameController.class) {
                    return new GameController(container.getGameState(), container.getGameService(), this);
                }
                throw buildUnknownController(cls);
            });
        });
        setScene(SceneId.GAME, root, transition);
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

    /** Exit the application. */
    public void exit() {
        stage.close();
    }

    private void setScene(SceneId sceneId, Parent root, TransitionStrategy transition) {
        Scene scene = new Scene(root, Constants.GAME_WIDTH, Constants.GAME_HEIGHT);
        stage.setScene(scene);
        transitionManager.play(scene.getRoot(), transition, null);
    }

    private void stopActiveGame() {
        if (activeGameController != null) {
            activeGameController.stop();
            activeGameController = null;
        }
    }

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

    private RuntimeException buildUnknownController(Class<?> cls) {
        return new IllegalArgumentException("Unsupported controller request: " + cls.getName());
    }
}

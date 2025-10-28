package com.game.arkanoid.view;

import com.game.arkanoid.container.Container;
import com.game.arkanoid.controller.GameController;
import com.game.arkanoid.controller.MenuController;
import com.game.arkanoid.controller.SettingsController;
import com.game.arkanoid.utils.Constants;
import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Central scene navigation helper to keep {@link javafx.application.Application} lean.
 */
public final class SceneNavigator {

    private final Stage stage;
    private GameController activeGameController;

    public SceneNavigator(Stage stage) {
        this.stage = stage;
    }

    /**
     * Show the main menu scene.
     */
    public void showMenu() {
        stopActiveGame();
        Parent root = load("/com/game/arkanoid/fxml/MenuView.fxml", loader -> {
            loader.setControllerFactory(cls -> {
                if (cls == MenuController.class) {
                    return new MenuController(this);
                }
                throw buildUnknownController(cls);
            });
        });
        stage.setScene(new Scene(root, Constants.GAME_WIDTH, Constants.GAME_HEIGHT));
    }

    /**
     * Show the settings scene.
     */
    public void showSettings() {
        stopActiveGame();
        Parent root = load("/com/game/arkanoid/fxml/SettingsView.fxml", loader -> {
            loader.setControllerFactory(cls -> {
                if (cls == SettingsController.class) {
                    return new SettingsController(this);
                }
                throw buildUnknownController(cls);
            });
        });
        stage.setScene(new Scene(root, Constants.GAME_WIDTH, Constants.GAME_HEIGHT));
    }

    /**
     * Show the game scene.
     */
    public void showGame() {
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
        Scene scene = new Scene(root, Constants.GAME_WIDTH, Constants.GAME_HEIGHT);
        stage.setScene(scene);
    }

    /**
     * Exit the application.
     */
    public void exit() {
        stage.close();
    }


    /**
     * Stop the active game if any.
     */
    private void stopActiveGame() {
        if (activeGameController != null) {
            activeGameController.stop();
            activeGameController = null;
        }
    }

    /**
     * Load FXML resource with custom controller configuration.
     * @param resource FXML resource path
     * @param configurer controller configurer
     * @return loaded Parent node
     */
    private Parent load(String resource, java.util.function.Consumer<FXMLLoader> configurer) {
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
     * Build exception for unknown controller requests.
     * @param cls requested controller class
     * @return  runtime exception
     */
    private RuntimeException buildUnknownController(Class<?> cls) {
        return new IllegalArgumentException("Unsupported controller request: " + cls.getName());
    }
}

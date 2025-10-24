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

    public void showMenu() {
        stopActiveGame();
        Parent root = load("/com/game/arkanoid/fxml/GameMenu.fxml", loader -> {
            loader.setControllerFactory(cls -> {
                if (cls == MenuController.class) {
                    return new MenuController(this);
                }
                throw buildUnknownController(cls);
            });
        });
        stage.setScene(new Scene(root, Constants.GAME_WIDTH, Constants.GAME_HEIGHT));
    }

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

    public void exit() {
        stage.close();
    }

    private void stopActiveGame() {
        if (activeGameController != null) {
            activeGameController.stop();
            activeGameController = null;
        }
    }

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

    private RuntimeException buildUnknownController(Class<?> cls) {
        return new IllegalArgumentException("Unsupported controller request: " + cls.getName());
    }
}

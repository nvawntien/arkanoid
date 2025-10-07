// Main.java
package com.game.arkanoid.app;

import com.game.arkanoid.container.Container;
import com.game.arkanoid.controller.GameController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws Exception {

        Container container = new Container();

        // 2. Load FXML và inject controller
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/game/arkanoid/fxml/GameView.fxml"));
        loader.setControllerFactory(cls -> {
        if (cls == GameController.class) {
            return new GameController(container.getGameState(), container.getGameService());   // inject Container
        }
        try { return cls.getDeclaredConstructor().newInstance(); }
        catch (Exception e) { throw new RuntimeException(e); }
    });

        Parent root = loader.load();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
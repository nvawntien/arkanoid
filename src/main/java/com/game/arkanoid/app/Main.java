package com.game.arkanoid.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

import com.game.arkanoid.utils.Constants;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/com/game/arkanoid/fxml/GamePlay.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), Constants.GAME_WIDTH, Constants.GAME_HEIGHT);
        stage.setTitle("Arkanoid - game top 1 vn");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

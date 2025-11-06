package com.game.arkanoid.app;

import com.game.arkanoid.controller.SceneController;

import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage stage) {
        stage.setTitle("Arkanoid");      
        stage.setResizable(false);
        SceneController navigator = new SceneController(stage);
        navigator.showMenu();
        stage.show();
    }
    public static void main(String[] args) {
        launch(args);
    }
}
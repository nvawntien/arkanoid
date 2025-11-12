package com.game.arkanoid.app;

import com.game.arkanoid.controller.SceneController;

import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {
    private SceneController navigator;
    @Override
    public void start(Stage stage) {
        stage.setTitle("Arkanoid");      
        stage.setResizable(false);
        navigator = new SceneController(stage);
        stage.setOnCloseRequest(e -> {
            try { navigator.saveInProgressIfAny(); } catch (Exception ignored) {}
        });
        navigator.showLogin();
        stage.show();
    }
    public static void main(String[] args) {
        launch(args);
    }
}

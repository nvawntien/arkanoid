package com.game.arkanoid.app;

import com.game.arkanoid.controller.infra.SceneNavigator;

import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage stage) {
        stage.setTitle("Arkanoid");      
        stage.setResizable(false);
        SceneNavigator navigator = new SceneNavigator(stage);
        navigator.showMenu();
        stage.show();
    }
    public static void main(String[] args) {
        launch(args);
    }
}
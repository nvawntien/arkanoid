package com.game.arkanoid.controller;

import com.game.arkanoid.view.MenuView;
import com.game.arkanoid.view.SceneNavigator;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;

public final class MenuController {

    private final SceneNavigator navigator;

    @FXML private AnchorPane root;
    @FXML private Button optionButton;
    @FXML private Button startButton;
    @FXML private Button exitButton;

    private MenuView animator;

    public MenuController(SceneNavigator navigator) {
        this.navigator = navigator;
    }

    @FXML
    private void initialize() {
        animator = new MenuView(optionButton, startButton, exitButton);

        root.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                setupKeyHandler(newScene);
            }
        });
    }

    private void setupKeyHandler(Scene scene) {
        int count = 0;
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.LEFT) {
                animator.moveLeft();
            } else if (event.getCode() == KeyCode.RIGHT) {
                animator.moveRight();
            } else if (event.getCode() == KeyCode.ENTER) {
                animator.getCenterButton().fire();
            }
        });
    }

    @FXML
    private void onStartGame(ActionEvent event) {
        navigator.showGame();
    }

    @FXML
    private void onOpenSettings(ActionEvent event) {
        navigator.showSettings();
    }

    @FXML
    private void onExit(ActionEvent event) {
        navigator.exit();
    }
}

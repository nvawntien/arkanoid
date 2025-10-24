package com.game.arkanoid.controller;

import com.game.arkanoid.view.SceneNavigator;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

/**
 * Controller for the main menu scene.
 */
public final class MenuController {

    private final SceneNavigator navigator;

    public MenuController(SceneNavigator navigator) {
        this.navigator = navigator;
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

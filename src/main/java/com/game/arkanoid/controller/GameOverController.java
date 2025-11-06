package com.game.arkanoid.controller;

import com.game.arkanoid.controller.SceneController;

import javafx.event.ActionEvent;

/** Simple controller for the game over screen. */
public final class GameOverController {
    private final SceneController navigator;

    public GameOverController(SceneController navigator) {
        this.navigator = navigator;
    }

    public void onPlayAgain(ActionEvent e) {
        navigator.showGame();
    }

    public void onMainMenu(ActionEvent e) {
        navigator.showMenu();
    }
}


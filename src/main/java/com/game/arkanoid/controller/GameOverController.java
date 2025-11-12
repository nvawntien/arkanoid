package com.game.arkanoid.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import com.game.arkanoid.container.Container;

public final class GameOverController {
    private final SceneController navigator;
    private final int finalScore;

    @FXML private Label scoreLabel;

    public GameOverController(SceneController navigator, int finalScore) {
        this.navigator = navigator;
        this.finalScore = finalScore;
    }

    @FXML
    private void initialize() {
        if (scoreLabel != null) {
            scoreLabel.setText(" " + finalScore);
        }
    }

    public void onPlayAgain(ActionEvent e) {
        // Reset game container to start a fresh session
        Container.reset();
        navigator.showGame();
    }

    public void onMainMenu(ActionEvent e) {
        navigator.showMenu();
    }
}

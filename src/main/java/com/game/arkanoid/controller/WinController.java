package com.game.arkanoid.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public final class WinController {
    private final SceneController navigator;
    private final int finalScore;

    @FXML private Label scoreLabel;

    public WinController(SceneController navigator, int finalScore) {
        this.navigator = navigator;
        this.finalScore = finalScore;
    }

    @FXML
    private void initialize() {
        if (scoreLabel != null) {
            scoreLabel.setText(" " + finalScore);
        }
    }

    public void onMainMenu(ActionEvent e) {
        navigator.showMenu();
    }

    public void onShowRankings(ActionEvent e) {
        navigator.showRankings();
    }
}


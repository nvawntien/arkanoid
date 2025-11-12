package com.game.arkanoid.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import com.game.arkanoid.container.Container;
import com.game.arkanoid.events.GameEventBus;
import com.game.arkanoid.events.sound.GameOverSoundEvent;
import com.game.arkanoid.events.sound.ClickSoundEvent;

public final class GameOverController {
    private final SceneController navigator;
    private final int finalScore;

    @FXML private Label scoreLabel;

    /**
     * Constructor.
     * @param navigator
     * @param finalScore
     */
    public GameOverController(SceneController navigator, int finalScore) {
        this.navigator = navigator;
        this.finalScore = finalScore;
    }

    /**
     * FXML initialize method.
     */
    @FXML
    private void initialize() {
        GameEventBus.getInstance().publish(new GameOverSoundEvent());
        if (scoreLabel != null) {
            scoreLabel.setText(" " + finalScore);
        }
    }

    /**
     * Handle "Play Again" button action.
     * @param e
     */
    public void onPlayAgain(ActionEvent e) {
        // Reset game container to start a fresh session
        GameEventBus.getInstance().publish(new ClickSoundEvent());
        Container.reset();
        navigator.showGame();
    }

    /**
     * Handle "Main Menu" button action.
     * @param e
     */
    public void onMainMenu(ActionEvent e) {
        GameEventBus.getInstance().publish(new ClickSoundEvent());
        navigator.showMenu();
    }
}

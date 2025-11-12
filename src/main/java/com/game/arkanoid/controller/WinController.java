package com.game.arkanoid.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * Controller for the "You Win" screen.
 * <p>
 * This class is responsible for displaying the final score after a game victory
 * and providing navigation options such as returning to the main menu or
 * viewing the rankings.
 * </p>
 */
public final class WinController {

    /** Controller responsible for scene navigation (e.g., main menu, rankings). */
    private final SceneController navigator;

    /** The player's final score at the end of the game. */
    private final int finalScore;

    /** Label used to display the player's final score. */
    @FXML private Label scoreLabel;

    /**
     * Constructs a new {@code WinController}.
     *
     * @param navigator  the {@link SceneController} used for scene transitions
     * @param finalScore the final score to display
     */
    public WinController(SceneController navigator, int finalScore) {
        this.navigator = navigator;
        this.finalScore = finalScore;
    }

    /**
     * Initializes the Win screen.
     * <p>
     * Sets the score label to display the player's final score.
     * </p>
     */
    @FXML
    private void initialize() {
        if (scoreLabel != null) {
            scoreLabel.setText(" " + finalScore);
        }
    }

    /**
     * Handles the "Main Menu" button click event.
     *
     * @param e the {@link ActionEvent} triggered by the button
     */
    public void onMainMenu(ActionEvent e) {
        navigator.showMenu();
    }

    /**
     * Handles the "Show Rankings" button click event.
     *
     * @param e the {@link ActionEvent} triggered by the button
     */
    public void onShowRankings(ActionEvent e) {
        navigator.showRankings();
    }
}

package com.game.arkanoid.controller;

import com.game.arkanoid.config.GameSettings;
import com.game.arkanoid.config.GameSettings.Difficulty;
import com.game.arkanoid.view.sound.SoundManager;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Slider;

/**
 * Controller that manages the Settings screen.
 * <p>
 * It handles user input for difficulty and volume settings,
 * synchronizing them with {@link GameSettings} and {@link SoundManager}.
 * </p>
 */
public final class SettingsController {

    /** ComboBox for selecting the game difficulty level. */
    @FXML
    private ComboBox<Difficulty> difficultyCombo;

    /** Navigator used to switch between scenes (e.g., back to menu). */
    private final SceneController navigator;

    /** Singleton instance managing game sounds and volumes. */
    private final SoundManager sound = SoundManager.getInstance();

    /** Slider for controlling overall (master) volume. */
    @FXML
    private Slider masterSlider;

    /** Slider for controlling background music volume. */
    @FXML
    private Slider musicSlider;

    /** Slider for controlling sound effects (SFX) volume. */
    @FXML
    private Slider sfxSlider;

    /** Stores the previous master volume before any change. */
    private double previousMasterVolume = GameSettings.getMasterVolume();

    /**
     * Constructs the controller with a reference to the scene navigator.
     *
     * @param navigator the {@link SceneController} used for scene transitions
     */
    public SettingsController(SceneController navigator) {
        this.navigator = navigator;
    }

    /**
     * Initializes the settings UI components.
     * <p>
     * Populates the difficulty combo box, and binds slider values
     * to corresponding sound properties.
     * </p>
     */
    @FXML
    private void initialize() {
        // Difficulty setup
        difficultyCombo.getItems().setAll(GameSettings.Difficulty.values());
        difficultyCombo.getSelectionModel().select(GameSettings.getDifficulty());

        // Bind sliders to SoundManager properties
        masterSlider.valueProperty().bindBidirectional(sound.masterVolumeProperty());
        musicSlider.valueProperty().bindBidirectional(sound.musicVolumeProperty());
        sfxSlider.valueProperty().bindBidirectional(sound.sfxVolumeProperty());
    }

    /**
     * Called when the user changes the difficulty.
     *
     * @param event the action event triggered by the ComboBox
     */
    @FXML
    private void onDifficultyChanged(ActionEvent event) {
        Difficulty selected = difficultyCombo.getSelectionModel().getSelectedItem();
        if (selected != null) {
            GameSettings.setDifficulty(selected);
        }
    }

    /**
     * Handles the Back button click.
     * Navigates the user back to the main menu.
     *
     * @param event the action event triggered by the button
     */
    @FXML
    private void onBack(ActionEvent event) {
        navigator.showMenu();
    }
}

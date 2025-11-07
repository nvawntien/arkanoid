package com.game.arkanoid.controller;

import com.game.arkanoid.config.GameSettings;
import com.game.arkanoid.config.GameSettings.Difficulty;
import com.game.arkanoid.view.sound.SoundManager;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Slider;

/**
 * Controller that binds the Settings UI to the static configuration store.
 */
public final class SettingsController {

    @FXML
    private CheckBox soundToggle;

    @FXML
    private ComboBox<Difficulty> difficultyCombo;

    private final SceneController navigator;
    private final SoundManager sound = SoundManager.getInstance();

    @FXML
    private Slider masterSlider;

    @FXML
    private Slider musicSlider;

    @FXML
    private Slider sfxSlider;

    private double previousMasterVolume = GameSettings.getMasterVolume();

    public SettingsController(SceneController navigator) {
        this.navigator = navigator;
    }

    @FXML
    private void initialize() {
        difficultyCombo.getItems().setAll(Difficulty.values());
        difficultyCombo.getSelectionModel().select(GameSettings.getDifficulty());
        soundToggle.setSelected(GameSettings.isSoundEnabled());

        masterSlider.valueProperty().bindBidirectional(sound.masterVolumeProperty());
        musicSlider.valueProperty().bindBidirectional(sound.musicVolumeProperty());
        sfxSlider.valueProperty().bindBidirectional(sound.sfxVolumeProperty());
        masterSlider.valueProperty().addListener((obs, oldV, newV) -> {
            if (!masterSlider.isDisabled()) {
                previousMasterVolume = newV.doubleValue();
            }
        });

        boolean enabled = GameSettings.isSoundEnabled();
        masterSlider.setDisable(!enabled);
        musicSlider.setDisable(!enabled);
        sfxSlider.setDisable(!enabled);
        if (enabled) {
            previousMasterVolume = masterSlider.getValue();
        }
    }

    @FXML
    private void onSoundToggled(ActionEvent event) {
        boolean enabled = soundToggle.isSelected();
        GameSettings.setSoundEnabled(enabled);
        if (enabled) {
            double restoreValue = previousMasterVolume > 0 ? previousMasterVolume : 1.0;
            sound.masterVolumeProperty().set(restoreValue);
        } else {
            previousMasterVolume = sound.masterVolumeProperty().get();
            sound.masterVolumeProperty().set(0.0);
        }
        masterSlider.setDisable(!enabled);
        musicSlider.setDisable(!enabled);
        sfxSlider.setDisable(!enabled);
    }

    @FXML
    private void onDifficultyChanged(ActionEvent event) {
        Difficulty selected = difficultyCombo.getSelectionModel().getSelectedItem();
        if (selected != null) {
            GameSettings.setDifficulty(selected);
        }
    }

    @FXML
    private void onBack(ActionEvent event) {
        navigator.showMenu();
    }
}

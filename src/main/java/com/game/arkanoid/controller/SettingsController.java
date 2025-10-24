package com.game.arkanoid.controller;

import com.game.arkanoid.config.GameSettings;
import com.game.arkanoid.config.GameSettings.Difficulty;
import com.game.arkanoid.view.SceneNavigator;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;

/**
 * Controller that binds the Settings UI to the static configuration store.
 */
public final class SettingsController {

    @FXML
    private CheckBox soundToggle;

    @FXML
    private ComboBox<Difficulty> difficultyCombo;

    private final SceneNavigator navigator;

    public SettingsController(SceneNavigator navigator) {
        this.navigator = navigator;
    }

    @FXML
    private void initialize() {
        difficultyCombo.getItems().setAll(Difficulty.values());
        difficultyCombo.getSelectionModel().select(GameSettings.getDifficulty());
        soundToggle.setSelected(GameSettings.isSoundEnabled());
    }

    @FXML
    private void onSoundToggled(ActionEvent event) {
        GameSettings.setSoundEnabled(soundToggle.isSelected());
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

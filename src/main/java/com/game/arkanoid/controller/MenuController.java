package com.game.arkanoid.controller;

import com.game.arkanoid.view.animator.MenuAnimator;
import com.game.arkanoid.view.sound.SoundManager;
import com.game.arkanoid.container.Container;
import com.game.arkanoid.controller.SceneController;
import com.game.arkanoid.events.GameEventBus;
import com.game.arkanoid.events.sound.GameBGMSoundEvent;
import com.game.arkanoid.events.sound.MenuBGMSoundEvent;
import com.game.arkanoid.events.sound.RoundStartSoundEvent;
import com.game.arkanoid.events.sound.StopBGMSoundEvent;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;

public final class MenuController {
    private final SceneController navigator;
    private final GameEventBus eventBus = GameEventBus.getInstance();
    private final SoundManager sound = SoundManager.getInstance();

    @FXML private AnchorPane root;
    @FXML private Button optionButton;
    @FXML private Button startButton;
    @FXML private Button exitButton;

    private MenuAnimator animator;

    public MenuController(SceneController navigator) {
        this.navigator = navigator;
    }

    @FXML
    private void initialize() {
        animator = new MenuAnimator(optionButton, startButton, exitButton);
        //eventBus.publish(new GameBGMSoundEvent());
        //eventBus.publish(new RoundStartSoundEvent());
        eventBus.publish(new MenuBGMSoundEvent());

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
        eventBus.publish(new StopBGMSoundEvent());
        Container.reset();      // 🔹 Reset toàn bộ game state (Container singleton)
        navigator.showGame();
    }

    @FXML
    private void onOpenSettings(ActionEvent event) {
        //sound.playSfx("menu_click");
        navigator.showSettings();
    }

    @FXML
    private void onExit(ActionEvent event) {
        //sound.playSfx("menu_click");
        //sound.stopAll();
        navigator.exit();
    }
}

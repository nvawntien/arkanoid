package com.game.arkanoid.controller;

import com.game.arkanoid.container.Container;
import com.game.arkanoid.events.GameEventBus;
import com.game.arkanoid.events.sound.MenuBGMSoundEvent;
import com.game.arkanoid.events.sound.StopBGMSoundEvent;
import com.game.arkanoid.view.animator.MenuAnimator;
import com.game.arkanoid.view.sound.SoundManager;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class MenuController {

    private final SceneController navigator;
    private final SoundManager sound = SoundManager.getInstance();
    private final GameEventBus eventBus = GameEventBus.getInstance();

    @FXML private AnchorPane root;
    @FXML private Button startButton;
    @FXML private Button optionButton;
    @FXML private Button exitButton;
    @FXML private Button continueButton;
    @FXML private Button rankingButton;

    private MenuAnimator animator;

    public MenuController(SceneController navigator) {
        this.navigator = navigator;
    }

    @FXML
    private void initialize() {
        eventBus.publish(new MenuBGMSoundEvent());

        List<Button> allButtons = List.of(
                continueButton,
                optionButton,
                startButton,
                exitButton,
                rankingButton
        );

        allButtons.forEach(b -> b.setFocusTraversable(false));

        Platform.runLater(() -> {
            // Sắp xếp thật theo vị trí X trong scene
            List<Button> ordered = new ArrayList<>(allButtons);
            ordered.sort(Comparator.comparingDouble(b -> b.localToScene(b.getLayoutX(), 0).getX()));

            animator = new MenuAnimator(ordered);

            // Đặt "Start" làm nút trung tâm
            int tries = 0;
            while (animator.getCenterButton() != startButton && tries < ordered.size()) {
                animator.moveRight();
                tries++;
            }

            root.requestFocus();
        });

        root.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) setupKeyHandler(newScene);
        });
    }

    /** Xử lý phím mũi tên và Enter */
    private void setupKeyHandler(Scene scene) {
        scene.setOnKeyPressed(event -> {
            if (animator == null) return;

            if (event.getCode() == KeyCode.RIGHT) {
                
                animator.moveRight();
            } else if (event.getCode() == KeyCode.LEFT) {
               
                animator.moveLeft();
            } else if (event.getCode() == KeyCode.ENTER) {
                Button selected = animator.getCenterButton();
                if (selected != null) selected.fire();
            }
        });
    }

    // ===== Các hành động của menu =====
    @FXML private void onStartGame(ActionEvent e) {
        eventBus.publish(new StopBGMSoundEvent());
        Container.reset();
        navigator.showGame();
    }

    @FXML private void onOpenSettings(ActionEvent e) {
        
        navigator.showSettings();
    }

    @FXML private void onExit(ActionEvent e) {
        
        navigator.exit();
    }

    @FXML private void onContinueGame(ActionEvent e) {
       
        navigator.continueGame();
    }

    @FXML private void onShowRanking(ActionEvent e) {
       
        navigator.showRanking();
    }
}
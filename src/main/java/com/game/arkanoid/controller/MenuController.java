package com.game.arkanoid.controller;

import java.util.HashSet;
import java.util.Set;

import com.game.arkanoid.models.InputState;
import com.game.arkanoid.services.MenuService;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;

public class MenuController {
    
    @FXML private Pane menuPane;

    // Input
    private final Set<KeyCode> activeKeys = new HashSet<>();
    private MenuService menuService;

    // Game loop
    private AnimationTimer loop;
    public MenuController( MenuService menuService) {
        this.menuService = menuService;
    }

    @FXML
    public void initialize() {
        
        //  Input wiring
        menuPane.setOnKeyPressed(e -> activeKeys.add(e.getCode()));
        menuPane.setOnKeyReleased(e -> activeKeys.remove(e.getCode()));
        menuPane.setFocusTraversable(true);
        Platform.runLater(menuPane::requestFocus); // ensure pane receives key events

        //  Start the loop on the FX thread
        loop = new AnimationTimer() {
            long last = -1;
            @Override public void handle(long now) {
                if (last < 0) { last = now; return; }
                double dt = (now - last) / 11_000_000.0;
                last = now;

                // Build per-frame input snapshot
                InputState in = readInput();

                // Advance game logic (no JavaFX types inside)
                menuService.update();

            }
        };

        loop.start();
    }

    private InputState readInput() {
        InputState in = new InputState();
        in.left   = activeKeys.contains(KeyCode.LEFT)  || activeKeys.contains(KeyCode.A);
        in.right  = activeKeys.contains(KeyCode.RIGHT) || activeKeys.contains(KeyCode.D);
        in.launch = activeKeys.contains(KeyCode.SPACE);
        // If you add pause in GameService: in.pause = activeKeys.contains(KeyCode.P);
        return in;
    }

    /** Optional: stop the loop when changing scenes/windows. */
    public void stop() {
        if (loop != null) loop.stop();
    }
}


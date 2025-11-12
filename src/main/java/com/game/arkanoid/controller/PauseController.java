package com.game.arkanoid.controller;

import java.util.Objects;
import javafx.fxml.FXML;
import com.game.arkanoid.events.GameEventBus;
import com.game.arkanoid.events.sound.ClickSoundEvent;

/**
 * Simple controller for the pause overlay. Delegates actions to callbacks supplied
 * by {@link GameController} to keep this class UI-focused.
 */
public final class PauseController {

    private final Runnable resumeHandler;
    private final Runnable restartHandler;
    private final Runnable exitHandler;
    private final GameEventBus eventBus = GameEventBus.getInstance();

    public PauseController(Runnable resumeHandler, Runnable restartHandler, Runnable exitHandler) {
        this.resumeHandler = Objects.requireNonNull(resumeHandler, "resumeHandler");
        this.restartHandler = Objects.requireNonNull(restartHandler, "restartHandler");
        this.exitHandler = Objects.requireNonNull(exitHandler, "exitHandler");
    }

    /**
     * Handle "Resume" button action.
     */
    @FXML
    private void onResume() {
        eventBus.publish(new ClickSoundEvent());
        resumeHandler.run();
    }

    /**
     * Handle "Restart" button action.
     */
    @FXML
    private void onRestart() {
        eventBus.publish(new ClickSoundEvent());
        restartHandler.run();
    }

    /**
     * Handle "Exit to Menu" button action.
     */
    @FXML
    private void onExitToMenu() {
        eventBus.publish(new ClickSoundEvent());
        exitHandler.run();
    }
}

package com.game.arkanoid.services;

import com.game.arkanoid.models.Brick;
import com.game.arkanoid.models.GameState;

import java.util.List;

/**
 * Service responsible for loading level layouts and managing level progression.
 * <p>
 * Pure logic; no JavaFX types are used to maintain MVC separation.
 * Handles resetting game state when a new level is loaded and advancing levels.
 * </p>
 */
public final class RoundService {

    private final BricksService bricksService;
    private final BallService ballService;
    private final PaddleService paddleService;

    private final String[] levelResources = new String[] {
        "/com/game/arkanoid/levels/level1.txt",
        "/com/game/arkanoid/levels/level2.txt",
        "/com/game/arkanoid/levels/level3.txt",
        "/com/game/arkanoid/levels/level4.txt"
    };

    /**
     * Constructor for RoundService.
     *
     * @param bricksService Service for handling bricks.
     * @param ballService Service for handling balls.
     * @param paddleService Service for handling the paddle.
     */
    public RoundService(BricksService bricksService, BallService ballService, PaddleService paddleService) {
        this.bricksService = bricksService;
        this.ballService = ballService;
        this.paddleService = paddleService;
    }

    /**
     * Loads a specific level into the game state.
     * Clears existing balls, enemies, power-ups, and resets the paddle and time scale.
     * Level index is 1-based; out-of-range indices are clamped to valid levels.
     *
     * @param state Current game state to modify.
     * @param levelIndex 1-based index of the level to load.
     */
    public void loadLevel(GameState state, int levelIndex) {
        int idx = Math.max(1, Math.min(levelResources.length, levelIndex));
        String resource = levelResources[idx - 1];

        List<Brick> bricks = bricksService.createBricksFromResource(resource);

        state.bricks.clear();
        state.bricks.addAll(bricks);
        state.level = idx;
        state.balls.clear();
        state.enemies.clear();
        state.powerUps.clear();
        state.activePowerUps.clear();
        state.levelTransitionPending = false;
        state.paused = true;
        state.running = false;
        state.paddle.setWidthClamped(state.basePaddleWidth);
        state.timeScale = 1.0;
        paddleService.resetPaddlePosition(state.paddle);
        state.balls.add(state.ball);
        // Reset ball position on paddle for the new level
        ballService.resetOnPaddle(state.ball, state.paddle);
    }

    /**
     * Loads the next level if available.
     * If the current level is the last one, marks the game as completed.
     *
     * @param state Current game state to modify.
     */
    public void loadNextLevel(GameState state) {
        int nextIndex = state.level + 1;
        if (nextIndex > levelResources.length) {
            state.gameCompleted = true;
            return;
        }
        loadLevel(state, nextIndex);
    }
}

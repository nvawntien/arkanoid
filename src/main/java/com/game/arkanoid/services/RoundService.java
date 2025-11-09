package com.game.arkanoid.services;

import com.game.arkanoid.models.Brick;
import com.game.arkanoid.models.GameState;

import java.util.List;

/**
 * Loads level layouts from resources and handles level progression.
 * Pure logic; no JavaFX types to keep MVC boundaries.
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

    public RoundService(BricksService bricksService, BallService ballService, PaddleService paddleService) {
        this.bricksService = bricksService;
        this.ballService = ballService;
        this.paddleService = paddleService;
    }

    /** Load the given 1-based level index. */
    public void loadLevel(GameState state, int levelIndex) {
        int idx = Math.max(1, Math.min(levelResources.length, levelIndex));
        String resource = levelResources[idx - 1];

        List<Brick> bricks = bricksService.createBricksFromResource(resource);

        state.bricks.clear();
        state.bricks.addAll(bricks);
        state.level = idx;
        state.balls.clear();
        state.powerUps.clear();
        state.activePowerUps.clear();
        state.levelTransitionPending = false;
        state.paused = true;
        state.running = false;
        state.paddle.setWidthClamped(state.basePaddleWidth);
        state.timeScale = 1.0;
        paddleService.resetPaddlePosition(state.paddle);
        state.balls.add(state.ball);
        // Reset ball on paddle for new level
        ballService.resetOnPaddle(state.ball, state.paddle);
    }

    /**
     * Advance to the next level if any. If already at the last level, mark the game as completed.
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

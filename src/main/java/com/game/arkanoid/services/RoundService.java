package com.game.arkanoid.services;

import com.game.arkanoid.models.Brick;
import com.game.arkanoid.models.GameState;
import java.util.List;

/**
 * Loads level layouts from resources and handles level progression.
 */
public final class RoundService {

    private final BricksService bricksService;
    private final BallService ballService;

    private final String[] levelResources = new String[] {
        "/com/game/arkanoid/levels/level1.txt",
        "/com/game/arkanoid/levels/level2.txt",
        "/com/game/arkanoid/levels/level3.txt",
        "/com/game/arkanoid/levels/level4.txt"
    };

    public RoundService(BricksService bricksService, BallService ballService) {
        this.bricksService = bricksService;
        this.ballService = ballService;
    }

    /** Load the given 1-based level index. */
    public void loadLevel(GameState state, int levelIndex) {
        int idx = Math.max(1, Math.min(levelResources.length, levelIndex));
        String resource = levelResources[idx - 1];

        List<Brick> bricks = bricksService.createBricksFromResource(resource);

        state.bricks.clear();
        state.bricks.addAll(bricks);
        state.level = idx;
        bricksService.recalculateBricksRemaining(state.bricks);
        state.extraBalls.clear();
        state.powerUps.clear();
        state.bullets.clear();
        state.activePowerUps.clear();
        state.laserCooldown = 0.0;
        // Reset ball on paddle for new level
        ballService.resetOnPaddle(state.ball, state.paddle);
    }

    /**
     * Advance to the next level if any. If already at the last level, mark the game as completed.
     */
    public void loadNextLevel(GameState state) {
        System.out.println("[RoundService] Loading next level... current=" + state.level);

        // Nếu vượt quá số level thì kết thúc game
        if (state.level >= levelResources.length - 1) {
            state.gameCompleted = true;
            System.out.println("[RoundService] Game completed!");
            return;
        }

        // Tăng level và nạp level mới
        loadLevel(state, state.level + 1);
        System.out.println("[RoundService] Now at level " + state.level);

        // Reset flags
        state.levelTransitionPending = false;
        state.running = false;  // sẽ được bật ở startNextLevel()
        state.paused = true;

        // Dọn dẹp entity
        state.extraBalls.clear();
        state.powerUps.clear();
        state.bullets.clear();

        // Reset bóng về paddle
        state.ball.setMoving(false);
    }

}

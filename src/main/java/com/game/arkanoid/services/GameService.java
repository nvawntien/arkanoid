package com.game.arkanoid.services;

import com.game.arkanoid.events.GameEventBus;
import com.game.arkanoid.events.game.GameOverEvent;
import com.game.arkanoid.events.game.LevelClearedEvent;
import com.game.arkanoid.events.sound.PaddleHitSoundEvent;
import com.game.arkanoid.events.sound.BrickHitSoundEvent;
import com.game.arkanoid.models.PowerUpType;
import com.game.arkanoid.models.Ball;
import com.game.arkanoid.models.Brick;
import com.game.arkanoid.models.GameState;
import com.game.arkanoid.models.InputState;
import com.game.arkanoid.models.PowerUp;
import com.game.arkanoid.utils.Constants;
import java.util.Iterator;
import java.util.List;

/**
 * Core game orchestration service.
 * Handles update loop, physics, collisions, level transitions, and publishes events
 * for UI/sound layers to react.
 *
 * NOTE:
 * - GameController drives the render/UI.
 * - GameService drives all domain logic & updates GameState.
 * - Only GameService mutates GameState; Controller should read only.
 */
public final class GameService {

    // --- Dependencies (composition of sub-services) ------------------------
    private final BallService ballSvc;
    private final PaddleService paddleSvc;
    private final BricksService bricksSvc;
    private final PowerUpService powerUpSvc;
    private final BulletService bulletSvc;
    private final RoundService roundSvc;
    private GameState boundState;

    // --- Constructor -------------------------------------------------------
    public GameService(
            BallService ballSvc,
            PaddleService paddleSvc,
            BricksService bricksSvc,
            PowerUpService powerUpSvc,
            BulletService bulletSvc,
            RoundService roundSvc
    ) {
        this.ballSvc = ballSvc;
        this.paddleSvc = paddleSvc;
        this.bricksSvc = bricksSvc;
        this.powerUpSvc = powerUpSvc;
        this.bulletSvc = bulletSvc;
        this.roundSvc = roundSvc;
    }

    // ======================================================================
    // region 1. CORE UPDATE LOOP
    // ======================================================================

    /**
     * Main update tick, called every frame.
     * Handles input, movement, collisions, power-ups, bullets, and level clearing.
     */
    public void update(GameState state, InputState in, double dt, double worldW, double worldH) {
        if (!state.running || state.paused || state.levelTransitionPending) return;

        double scaledDt = dt * state.timeScale;

        // 1. Handle input
        handleInput(state, in, scaledDt, worldW);

        // 2. Update projectiles and entities
        bulletSvc.tickCooldown(state, scaledDt);
        updateBalls(state, scaledDt, worldW, worldH);
        updateBullets(state, scaledDt, worldH);

        // 3. Power-ups and game progression
        powerUpSvc.update(state, scaledDt, worldW, worldH);
        checkLevelCleared(state);
    }

    /**
     * Handles keyboard input (movement, launch, fire).
     */
    private void handleInput(GameState state, InputState in, double dt, double worldW) {
        if (in.left)  paddleSvc.moveLeft(state.paddle, dt, worldW);
        if (in.right) paddleSvc.moveRight(state.paddle, dt, worldW);

        // Keep ball attached before launch
        for (Ball ball : state.balls) {
            if (!ball.isMoving()) {
                if (ball.isStuck()) {
                    // Ball was caught: keep it attached to the paddle using the stored offset
                    double paddleX = state.paddle.getX();
                    double paddleW = state.paddle.getWidth();
                    double desiredX = paddleX + ball.getStuckOffsetX();
                    double minX = paddleX + ball.getRadius();
                    double maxX = paddleX + paddleW - ball.getRadius();
                    desiredX = Math.max(minX, Math.min(desiredX, maxX));
                    ball.setCenter(desiredX, state.paddle.getY() - ball.getRadius() - Constants.BALL_NUDGE);
                } else {
                    ballSvc.resetOnPaddle(ball, state.paddle);
                }
            }
        }

        if (in.launch) {
            for (Ball ball : state.balls) {
                if (!ball.isMoving()) {
                    ballSvc.launch(ball);
                    break;
                }
            }
            in.launch = false;
        }

        if (in.fire && state.activePowerUps.containsKey(PowerUpType.LASER_PADDLE)) {
            bulletSvc.tryFire(state, state.paddle);
        }
    }

    // endregion


    // ======================================================================
    // region 2. ENTITY UPDATES (Ball, Extra Balls, Bullets, PowerUps)
    // ======================================================================

  
    /**
     * Updates all extra (duplicated) balls from power-ups.
     */
    private void updateBalls(GameState state, double dt, double worldW, double worldH) {
        Iterator<Ball> iterator = state.balls.iterator();
        while (iterator.hasNext()) {
            Ball ball = iterator.next();
            ballSvc.step(ball, dt);
            ballSvc.bounceWorld(ball, worldW, worldH);
            handlePaddleCollision(ball, state);
            handleBrickCollisions(ball, state);
            if (ballSvc.fellBelow(ball, worldH)) {
                iterator.remove();
                handleBallFall(state);
            }
        }
    }

    /**
     * Updates active bullets from the LASER_PADDLE power-up.
     */
    private void updateBullets(GameState state, double dt, double worldH) {
        if (state.levelTransitionPending) return;

        List<BulletService.Impact> impacts = bulletSvc.update(state, state.bricks, dt, worldH);
        for (BulletService.Impact impact : impacts) {
            Brick brick = impact.brick();

            boolean destroyed = bricksSvc.handleBrickHit(brick);
            if (destroyed) processDestroyedBrick(state, brick);
        }
    }

    // endregion


    // ======================================================================
    // region 3. COLLISION HANDLERS
    // ======================================================================

    private void handlePaddleCollision(Ball ball, GameState state) {
        if (ballSvc.checkCollision(ball, state.paddle)) {
            GameEventBus.getInstance().publish(new PaddleHitSoundEvent());
            ballSvc.bounceOff(ball, state.paddle);
            ball.setCenter(ball.getCenterX(), state.paddle.getY() - ball.getRadius() - Constants.BALL_NUDGE);
        }
    }

    private void handleBrickCollisions(Ball ball, GameState state) {
        for (Brick brick : state.bricks) {
            if (brick.isDestroyed()) continue;

            if (ballSvc.checkCollision(ball, brick)) {
                ballSvc.bounceOff(ball, brick);
                GameEventBus.getInstance().publish(new BrickHitSoundEvent());
                boolean destroyed = bricksSvc.handleBrickHit(brick);
                if (destroyed) {
                    state.score += 100;
                    PowerUp spawned = null;
                    if (countAliveBricks(state) > 0) {
                        spawned = powerUpSvc.spawnPowerUpIfAny(brick.getX(), brick.getY(), brick.getWidth());
                    }
                    if (spawned != null) {
                        state.powerUps.add(spawned);
                    }
                    checkLevelCleared(state);
                }
                break;
            }
        }
    }

    // endregion


    // ======================================================================
    // region 4. GAME PROGRESSION (Lives, Scoring, Level Clear)
    // ======================================================================

    /**
     * Handles ball loss (life decrement, reset, game over check).
     */
    private void handleBallFall(GameState state) {
        if (state.balls.size() > 0) {
            return;
        }

        // No extra balls → lose life
        state.decrementLives();
        state.resetForLife();
        state.balls.add(state.ball);
        ballSvc.resetOnPaddle(state.ball, state.paddle);
        
        if (state.lives < 0) {
            state.running = false;
            state.gameOver = true;
            GameEventBus.getInstance().publish(new GameOverEvent());
        }
    }

    /**
     * Called whenever a brick is destroyed.
     * Updates score, spawns power-up, and re-checks for level completion.
     */
    private void processDestroyedBrick(GameState state, Brick brick) {
        state.score++;

        if (countAliveBricks(state) > 1) {
            PowerUp spawned = powerUpSvc.spawnPowerUpIfAny(brick.getX(), brick.getY(), brick.getWidth());
            if (spawned != null) state.powerUps.add(spawned);
        }
        checkLevelCleared(state);
    }

    /**
     * Checks if all bricks are cleared, marks transition, and fires event.
     */
    private void checkLevelCleared(GameState state) {
        if (state.levelTransitionPending) return;

        if (bricksSvc.allBricksCleared(state.bricks)) {
            System.out.println("[GameService] All bricks cleared at level " + state.level);
            state.levelTransitionPending = true;
            state.running = false;
            GameEventBus.getInstance().publish(new LevelClearedEvent(state.level));
        }
    }

    private int countAliveBricks(GameState state) {
        int alive = 0;
        for (Brick brick : state.bricks) {
            if (!brick.isDestroyed()) alive++;
        }
        return alive;
    }

    // endregion


    // ======================================================================
    // region 5. LEVEL MANAGEMENT
    // ======================================================================

    /**
     * Restarts the current level (same layout, reset state).
     */
    public void restartLevel(GameState state) {
        roundSvc.loadLevel(state, state.level);
        state.running = true;
        state.gameOver = false;
        state.gameCompleted = false;
        state.paused = true;
        state.levelTransitionPending = false;
    }

    /**
     * Loads the next level, keeping state transitions consistent.
     */
    public void loadNextLevel(GameState state) {
        roundSvc.loadNextLevel(state);
    }

    /**
     * Starts the level after intro countdown.
     */
    public void startNextLevel(GameState state) {
        state.paused = false;
        state.running = true;
        state.levelTransitionPending = false;
    }

    /**
     * Loads a specific level index.
     */
    public void loadLevel(GameState state, int levelIndex) {
        roundSvc.loadLevel(state, levelIndex);
    }

    // endregion


    // ======================================================================
    // region 6. MISC / SETUP
    // ======================================================================

    public void bindState(GameState state) {
        this.boundState = state;
    }

    // endregion
}

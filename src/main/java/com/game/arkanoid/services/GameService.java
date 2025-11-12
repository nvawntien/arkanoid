package com.game.arkanoid.services;

import com.game.arkanoid.events.GameEventBus;
import com.game.arkanoid.events.game.GameOverEvent;
import com.game.arkanoid.events.game.LevelClearedEvent;
import com.game.arkanoid.events.paddle.ExplodePaddleEvent;
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
 * Core service orchestrating the Arkanoid game logic.
 * Handles main update loop, entity updates, collision detection,
 * power-ups, level progression, and publishes game events for UI and sound feedback.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Process player input.</li>
 *   <li>Update balls, bullets, power-ups, and enemies.</li>
 *   <li>Handle collisions with paddle, bricks, and enemies.</li>
 *   <li>Manage scoring, lives, and level transitions.</li>
 *   <li>Publish events for sounds, explosions, and game progression.</li>
 * </ul>
 *
 * <p>Note:
 * <ul>
 *   <li>GameController handles rendering and UI.</li>
 *   <li>GameService contains all domain logic and mutates GameState.</li>
 *   <li>Only GameService modifies GameState; UI/controller should read only.</li>
 * </ul>
 */
public final class GameService {

    // --- Dependencies (composition of sub-services) ------------------------
    private final BallService ballSvc;
    private final PaddleService paddleSvc;
    private final BricksService bricksSvc;
    private final PowerUpService powerUpSvc;
    private final BulletService bulletSvc;
    private final EnemyService enemySvc;
    private final RoundService roundSvc;

    public BallService getBallSvc() {
        return ballSvc;
    }

    public PaddleService getPaddleSvc() {
        return paddleSvc;
    }

    public BricksService getBricksSvc() {
        return bricksSvc;
    }

    public PowerUpService getPowerUpSvc() {
        return powerUpSvc;
    }

    public BulletService getBulletSvc() {
        return bulletSvc;
    }

    public EnemyService getEnemySvc() {
        return enemySvc;
    }

    public RoundService getRoundSvc() {
        return roundSvc;
    }

    public GameState getBoundState() {
        return boundState;
    }

    public void setBoundState(GameState boundState) {
        this.boundState = boundState;
    }

    private GameState boundState;

    // --- Constructor -------------------------------------------------------

    /**
     * Constructs the GameService with all sub-services required for game logic.
     *
     * @param ballSvc Ball movement and physics service.
     * @param paddleSvc Paddle movement and collision service.
     * @param bricksSvc Brick collision and state management service.
     * @param powerUpSvc Power-up spawning and effects service.
     * @param bulletSvc Laser bullets update and collision service.
     * @param roundSvc Level loading and round management service.
     * @param enemySvc Enemy spawning, movement, and collision service.
     */
    public GameService(
            BallService ballSvc,
            PaddleService paddleSvc,
            BricksService bricksSvc,
            PowerUpService powerUpSvc,
            BulletService bulletSvc,
            RoundService roundSvc,
            EnemyService enemySvc
    ) {
        this.ballSvc = ballSvc;
        this.paddleSvc = paddleSvc;
        this.bricksSvc = bricksSvc;
        this.powerUpSvc = powerUpSvc;
        this.bulletSvc = bulletSvc;
        this.roundSvc = roundSvc;
        this.enemySvc = enemySvc;
    }

    // ======================================================================
    // region 1. CORE UPDATE LOOP
    // ======================================================================

    /**
     * Main update tick called every frame.
     * Handles input, entity updates, collisions, power-ups, and level progression.
     *
     * @param state Current game state.
     * @param in Player input state.
     * @param dt Delta time since last frame.
     * @param worldW Width of the game world.
     * @param worldH Height of the game world.
     */
    public void update(GameState state, InputState in, double dt, double worldW, double worldH) {
        if (!state.running || state.paused || state.levelTransitionPending) return;

        double scaledDt = dt * state.timeScale;
        handleInput(state, in, scaledDt, worldW);

        enemySvc.update(state, scaledDt, worldW, worldH);
        bulletSvc.tickCooldown(state, scaledDt);
        updateBalls(state, scaledDt, worldW, worldH);
        updateBullets(state, scaledDt, worldH);
        powerUpSvc.update(state, scaledDt, worldW, worldH);
        checkLevelCleared(state);
        handleBallFall(state);
    }

    /**
     * Processes player keyboard input for paddle movement, ball launch, and firing bullets.
     *
     * @param state Current game state.
     * @param in Player input state.
     * @param dt Delta time for movement scaling.
     * @param worldW Width of the game world.
     */
    private void handleInput(GameState state, InputState in, double dt, double worldW) {
        if (in.left)  paddleSvc.moveLeft(state.paddle, dt, worldW);
        if (in.right) paddleSvc.moveRight(state.paddle, dt, worldW);

        // Keep balls attached to paddle if not moving
        for (Ball ball : state.balls) {
            if (!ball.isMoving()) {
                if (ball.isStuck()) {
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

    // ======================================================================
    // region 2. ENTITY UPDATES (Balls, Bullets, PowerUps)
    // ======================================================================

    /**
     * Updates all balls in the game, including extra balls from power-ups.
     * Handles movement, world bouncing, collisions with paddle and bricks,
     * and removes balls that fall below the game world.
     *
     * @param state Current game state.
     * @param dt Delta time for movement scaling.
     * @param worldW Width of the game world.
     * @param worldH Height of the game world.
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
            }
        }
    }

    /**
     * Updates active bullets, handles collisions with bricks, and triggers
     * brick destruction events and power-up spawning.
     *
     * @param state Current game state.
     * @param dt Delta time for bullet movement.
     * @param worldH Height of the game world.
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

    // ======================================================================
    // region 3. COLLISION HANDLERS
    // ======================================================================

    /**
     * Handles collision between a ball and the paddle, including bouncing
     * and playing sound effects.
     *
     * @param ball Ball object.
     * @param state Current game state.
     */
    private void handlePaddleCollision(Ball ball, GameState state) {
        if (ballSvc.checkCollision(ball, state.paddle)) {
            GameEventBus.getInstance().publish(new PaddleHitSoundEvent());
            ballSvc.bounceOff(ball, state.paddle);
            ball.setCenter(ball.getCenterX(), state.paddle.getY() - ball.getRadius() - Constants.BALL_NUDGE);
        }
    }

    /**
     * Handles collision between a ball and bricks. Updates score, spawns power-ups,
     * triggers sound effects, and checks level completion.
     *
     * @param ball Ball object.
     * @param state Current game state.
     */
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

    // ======================================================================
    // region 4. GAME PROGRESSION (Lives, Scoring, Level Clear)
    // ======================================================================

    /**
     * Handles ball falling below the paddle. Decrements lives,
     * resets the ball, and triggers game over events if lives reach zero.
     *
     * @param state Current game state.
     */
    private void handleBallFall(GameState state) {
        if (state.balls.size() > 0) return;

        state.decrementLives();
        state.resetForLife();

        if (state.lives < 0) {
            state.running = false;
            GameEventBus.getInstance().publish(new ExplodePaddleEvent());
            GameEventBus.getInstance().publish(new GameOverEvent());
        } else {
            state.balls.add(state.ball);
            ballSvc.resetOnPaddle(state.ball, state.paddle);
        }
    }

    /**
     * Processes a destroyed brick by updating score, spawning a power-up if applicable,
     * and checking if the level has been cleared.
     *
     * @param state Current game state.
     * @param brick Destroyed brick object.
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
     * Checks whether all bricks have been cleared, marks the level
     * transition, stops the game, and fires the LevelClearedEvent.
     *
     * @param state Current game state.
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

    /**
     * Counts the number of bricks that are not destroyed.
     *
     * @param state Current game state.
     * @return Number of alive bricks.
     */
    private int countAliveBricks(GameState state) {
        int alive = 0;
        for (Brick brick : state.bricks) {
            if (!brick.isDestroyed()) alive++;
        }
        return alive;
    }

    // ======================================================================
    // region 5. LEVEL MANAGEMENT
    // ======================================================================

    /**
     * Restarts the current level by reloading layout and resetting game state.
     *
     * @param state Current game state.
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
     * Loads the next level while maintaining game state transitions.
     *
     * @param state Current game state.
     */
    public void loadNextLevel(GameState state) {
        roundSvc.loadNextLevel(state);
    }

    /**
     * Starts the next level after any intro countdown.
     *
     * @param state Current game state.
     */
    public void startNextLevel(GameState state) {
        state.paused = false;
        state.running = true;
        state.levelTransitionPending = false;
    }

    /**
     * Loads a specific level by index.
     *
     * @param state Current game state.
     * @param levelIndex Index of the level to load.
     */
    public void loadLevel(GameState state, int levelIndex) {
        roundSvc.loadLevel(state, levelIndex);
    }

    // ======================================================================
    // region 6. MISC / SETUP
    // ======================================================================

    /**
     * Binds the GameService to a specific GameState instance.
     *
     * @param state GameState instance to bind.
     */
    public void bindState(GameState state) {
        this.boundState = state;
    }
}

package com.game.arkanoid.services;

import com.game.arkanoid.config.GameSettings;
import com.game.arkanoid.events.GameEventBus;
import com.game.arkanoid.events.powerup.PowerUpActivatedEvent;
import com.game.arkanoid.events.powerup.PowerUpExpiredEvent;
import com.game.arkanoid.events.sound.PowerUpHitSoundEvent;
import com.game.arkanoid.models.Ball;
import com.game.arkanoid.models.GameState;
import com.game.arkanoid.models.Paddle;
import com.game.arkanoid.models.PowerUp;
import com.game.arkanoid.models.PowerUpType;
import com.game.arkanoid.utils.Constants;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Service responsible for managing power-ups in the game.
 * Handles spawning, updating, applying effects, and expiring active power-ups.
 * <p>
 * This service also communicates with renderers and sound layers via events.
 * </p>
 * <p>
 * Typical power-ups include: Expand Paddle, Laser Paddle, Catch Ball, Multi-Ball, Extra Life, Slow Ball.
 * </p>
 */
public final class PowerUpService {

    private final Random random = new Random();
    private final GameEventBus eventBus = GameEventBus.getInstance();

    /**
     * Default constructor.
     */
    public PowerUpService() {
    }

    /**
     * Randomly spawns a power-up at the specified position with a drop chance.
     *
     * @param x X-coordinate of the brick.
     * @param y Y-coordinate of the brick.
     * @param width Width of the brick to center the power-up.
     * @return A new PowerUp instance or null if no power-up is spawned.
     */
    public PowerUp spawnPowerUpIfAny(double x, double y, double width) {
        if (random.nextDouble() > Constants.POWER_UP_DROP_CHANCE) {
            return null;
        }
        PowerUpType[] types = {PowerUpType.CATCH_BALL, PowerUpType.MULTI_BALL};
        PowerUpType type = types[random.nextInt(types.length)];
        double spawnX = x + (width - Constants.POWER_UP_WIDTH) / 2.0;
        double spawnY = y + Constants.BRICK_HEIGHT;
        return new PowerUp(type.MULTI_BALL, spawnX, spawnY, Constants.POWER_UP_WIDTH, Constants.POWER_UP_HEIGHT, Constants.POWER_UP_FALL_SPEED);
    }

    /**
     * Updates all active power-ups and applies their effects when collected.
     * Removes power-ups that fall below the playfield.
     * Updates active timed power-ups and expires them if necessary.
     *
     * @param state Current game state.
     * @param dt Time delta in seconds.
     * @param worldW Width of the game world.
     * @param worldH Height of the game world.
     */
    public void update(GameState state, double dt, double worldW, double worldH) {
        List<PowerUp> toRemove = new ArrayList<>();
        for (PowerUp powerUp : state.powerUps) {
            powerUp.update(dt);
            if (powerUp.getY() > worldH) {
                toRemove.add(powerUp);
                continue;
            }
            if (intersects(powerUp, state.paddle)) {
                GameEventBus.getInstance().publish(new PowerUpHitSoundEvent());
                applyPowerUp(state, powerUp.getType(), worldW);
                toRemove.add(powerUp);
            }
        }
        state.powerUps.removeAll(toRemove);
        tickActiveEffects(state, dt, worldW);
    }

    /**
     * Checks if a power-up intersects with the paddle.
     *
     * @param powerUp The power-up to check.
     * @param paddle The paddle to check collision with.
     * @return True if the power-up intersects the paddle, false otherwise.
     */
    private boolean intersects(PowerUp powerUp, Paddle paddle) {
        double px1 = paddle.getX();
        double py1 = paddle.getY();
        double px2 = px1 + paddle.getWidth();
        double py2 = py1 + paddle.getHeight();

        double ox1 = powerUp.getX();
        double oy1 = powerUp.getY();
        double ox2 = ox1 + powerUp.getWidth();
        double oy2 = oy1 + powerUp.getHeight();

        return ox1 < px2 && ox2 > px1 && oy1 < py2 && oy2 > py1;
    }

    /**
     * Applies the effect of a collected power-up to the game state.
     *
     * @param state Current game state.
     * @param type Type of the collected power-up.
     * @param worldW Width of the game world for clamping the paddle.
     */
    private void applyPowerUp(GameState state, PowerUpType type, double worldW) {
        switch (type) {
            case EXPAND_PADDLE -> {
                if (!state.activePowerUps.containsKey(PowerUpType.EXPAND_PADDLE)) {
                    eventBus.publish(new PowerUpActivatedEvent(PowerUpType.EXPAND_PADDLE));
                }
                state.activePowerUps.remove(PowerUpType.LASER_PADDLE);
                state.paddle.setWidthClamped(state.basePaddleWidth * Constants.POWER_UP_EXPAND_FACTOR);
                state.activePowerUps.put(PowerUpType.EXPAND_PADDLE, Constants.POWER_UP_DURATION);
            }
            case LASER_PADDLE -> {
                if (!state.activePowerUps.containsKey(PowerUpType.LASER_PADDLE)) {
                    eventBus.publish(new PowerUpActivatedEvent(PowerUpType.LASER_PADDLE));
                }
                state.activePowerUps.remove(PowerUpType.EXPAND_PADDLE);
                state.paddle.setWidthClamped(state.basePaddleWidth * Constants.POWER_UP_LASER_FACTOR);
                state.activePowerUps.put(PowerUpType.LASER_PADDLE, Constants.POWER_UP_DURATION);
                state.laserCooldown = 0.0;
            }
            case CATCH_BALL -> {
                for (Ball ball : state.balls) {
                    if (ball.isMoving()) ball.setStuck(true);
                }
            }
            case MULTI_BALL -> spawnAdditionalBalls(state);
            case EXTRA_LIFE -> state.incrementLives();
            case SLOW_BALL -> {
                state.timeScale = Constants.POWER_UP_SLOW_BALL_SCALE;
                state.activePowerUps.put(PowerUpType.SLOW_BALL, Constants.POWER_UP_DURATION);
            }
        }
        clampPaddle(state.paddle, worldW);
    }

    /**
     * Spawns additional balls for the MULTI_BALL power-up.
     * Each existing moving ball spawns two new balls at slight angle offsets.
     *
     * @param state Current game state.
     */
    private void spawnAdditionalBalls(GameState state) {
        List<Ball> newBall = new ArrayList<>();
        for (Ball source : state.balls) {
            if (!source.isMoving()) {
                continue;
            }

            double baseSpeed = Math.hypot(source.getDx(), source.getDy());

            if (baseSpeed < 1e-3) {
                baseSpeed = Constants.BALL_SPEED * GameSettings.getBallSpeedMultiplier();
            }

            double[] angles = {-15.0, 15.0};
            for (double angleOffset : angles) {
                Ball extra = new Ball(source.getCenterX(), source.getCenterY(), source.getRadius());
                double angle = Math.toDegrees(Math.atan2(source.getDy(), source.getDx())) + angleOffset;
                double rad = Math.toRadians(angle);
                extra.setVelocity(baseSpeed * Math.cos(rad), baseSpeed * Math.sin(rad));
                extra.setMoving(true);
                newBall.add(extra);
            }
        }

        state.balls.addAll(newBall);
    }

    /**
     * Clamps the paddle's horizontal position within the world bounds.
     *
     * @param paddle The paddle to clamp.
     * @param worldW Width of the game world.
     */
    private void clampPaddle(Paddle paddle, double worldW) {
        if (paddle.getX() < 22) {
            paddle.setX(22);
        }
        double maxX = worldW - paddle.getWidth() - 22;
        if (paddle.getX() > maxX) {
            paddle.setX(maxX);
        }
    }

    /**
     * Updates the remaining duration of all active power-ups.
     * Expires any power-ups whose duration has ended.
     *
     * @param state Current game state.
     * @param dt Time delta in seconds.
     * @param worldW Width of the game world for clamping the paddle when effects expire.
     */
    private void tickActiveEffects(GameState state, double dt, double worldW) {
        Iterator<PowerUpType> iterator = new ArrayList<>(state.activePowerUps.keySet()).iterator();
        while (iterator.hasNext()) {
            PowerUpType type = iterator.next();
            double remaining = state.activePowerUps.get(type) - dt;
            if (remaining <= 0) {
                state.activePowerUps.remove(type);
                onEffectExpired(state, type, worldW);
            } else {
                state.activePowerUps.put(type, remaining);
            }
        }
    }

    /**
     * Handles expiration of a power-up effect.
     * Resets paddle size, clears bullets for LASER_PADDLE, restores timeScale for SLOW_BALL, and emits expiration events.
     *
     * @param state Current game state.
     * @param type Type of power-up that expired.
     * @param worldW Width of the game world for clamping the paddle.
     */
    private void onEffectExpired(GameState state, PowerUpType type, double worldW) {
        switch (type) {
            case EXPAND_PADDLE, LASER_PADDLE -> {
                state.paddle.setWidthClamped(state.basePaddleWidth);
                clampPaddle(state.paddle, worldW);
                if (type == PowerUpType.LASER_PADDLE) {
                    state.bullets.clear();
                    state.laserCooldown = 0.0;
                }
            }
            case SLOW_BALL -> state.timeScale = 1.0;
            default -> { }
        }

        eventBus.publish(new PowerUpExpiredEvent(type));
    }
}

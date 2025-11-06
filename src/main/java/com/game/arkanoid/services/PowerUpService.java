package com.game.arkanoid.services;

import com.game.arkanoid.config.GameSettings;
import com.game.arkanoid.events.GameEventBus;
import com.game.arkanoid.events.powerup.PowerUpActivatedEvent;
import com.game.arkanoid.events.powerup.PowerUpExpiredEvent;
import com.game.arkanoid.models.Ball;
import com.game.arkanoid.models.Brick;
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
 * Orchestrates the spawning, updating and application of power-ups.
 */
public final class PowerUpService {

    private final Random random = new Random();
    private final GameEventBus eventBus = GameEventBus.getInstance();

    public PowerUp spawnPowerUpIfAny(Brick brick) {
        if (random.nextDouble() > Constants.POWER_UP_DROP_CHANCE) {
            return null;
        }
        PowerUpType[] types = { PowerUpType.EXPAND_PADDLE, PowerUpType.LASER_PADDLE };
        PowerUpType type = types[random.nextInt(types.length)];
        double x = brick.getX() + (brick.getWidth() - Constants.POWER_UP_WIDTH) / 2.0;
        double y = brick.getY() + brick.getHeight();

        PowerUp powerUp = new PowerUp(type, x, y, Constants.POWER_UP_WIDTH, Constants.POWER_UP_HEIGHT, Constants.POWER_UP_FALL_SPEED);
        return powerUp;
    }

    public void update(GameState state, double dt, double worldW, double worldH) {
        List<PowerUp> toRemove = new ArrayList<>();
        for (PowerUp powerUp : state.powerUps) {
            powerUp.update(dt);
            if (powerUp.getY() > worldH) {
                toRemove.add(powerUp);
                continue;
            }
            if (intersects(powerUp, state.paddle)) {
                applyPowerUp(state, powerUp.getType(), worldW);
                toRemove.add(powerUp);
            }
        }  
        state.powerUps.removeAll(toRemove);
        tickActiveEffects(state, dt, worldW);
    }

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
            }
            case CATCH_BALL -> {
                state.ball.setStuck(true);
            }
            case MULTI_BALL -> spawnAdditionalBalls(state);
            case EXTRA_LIFE -> state.lives++;
            case SLOW_BALL -> {
                state.timeScale = Constants.POWER_UP_SLOW_BALL_SCALE;
                state.activePowerUps.put(PowerUpType.SLOW_BALL, Constants.POWER_UP_DURATION);
            }           
        }
        // Ensure paddle stays inside bounds when width changed.
        clampPaddle(state.paddle, worldW);
    }

    private void spawnAdditionalBalls(GameState state) {
        Ball source = state.ball;
        if (!source.isMoving()) {
            return;
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
            state.extraBalls.add(extra);
        }
    }

    private void clampPaddle(Paddle paddle, double worldW) {
        if (paddle.getX() < 22) {
            paddle.setX(22);
        }
        double maxX = worldW - paddle.getWidth() - 22;
        if (paddle.getX() > maxX) {
            paddle.setX(maxX);
        }
    }

    private void tickActiveEffects(GameState state, double dt, double worldW) {
        Iterator<PowerUpType> iterator = new ArrayList<>(state.activePowerUps.keySet()).iterator();
        while (iterator.hasNext()) {
            PowerUpType type = iterator.next();
            double remaining = state.activePowerUps.get(type) - dt;
            if (remaining <= 0) {
                //paddleRenderer.playShrink(null); // No-op onFinished
                state.activePowerUps.remove(type);
                onEffectExpired(state, type, worldW);
            } else {
                state.activePowerUps.put(type, remaining);
            }
        }
    }

    private void onEffectExpired(GameState state, PowerUpType type, double worldW) {
        switch (type) {
            case EXPAND_PADDLE -> {
                state.paddle.setWidthClamped(state.basePaddleWidth);
                clampPaddle(state.paddle, worldW);
            }
            case LASER_PADDLE -> {
                state.paddle.setWidthClamped(state.basePaddleWidth);
                clampPaddle(state.paddle, worldW);
            }
            case SLOW_BALL -> state.timeScale = 1.0;
            default -> { }
        }

        eventBus.publish(new PowerUpExpiredEvent(type));
    }
}

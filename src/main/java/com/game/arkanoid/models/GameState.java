package com.game.arkanoid.models;

import com.game.arkanoid.utils.Constants;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * All runtime state for a single play session.
 */
public final class GameState {

    public final Ball ball;
    public final Paddle paddle;

    public final List<Brick> bricks = new ArrayList<>();
    public final List<Ball> extraBalls = new ArrayList<>();
    public final List<PowerUp> powerUps = new ArrayList<>();
    public final Map<PowerUpType, Double> activePowerUps = new EnumMap<>(PowerUpType.class);

    public int score = Constants.DEFAULT_SCORE;
    public int lives = Constants.DEFAULT_LIVES;
    public int level = Constants.DEFAULT_LEVEL;

    public boolean running = true;
    public boolean paused = false;
    public double timeScale = 1.1;
    public double basePaddleWidth;
    public double basePaddleSpeed;

    public GameState(Ball ball, Paddle paddle) {
        this.ball = ball;
        this.paddle = paddle;
    }

    public void resetForLife() {
        paused = false;
    }

    public void resetForLevel() {
        score = 0;
        lives = 3;
        level = 1;
        running = true;
        paused = false;
        timeScale = 1.0;
    }
}

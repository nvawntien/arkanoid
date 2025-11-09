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
    public final List<Ball> balls = new ArrayList<>();
    public final List<Bullet> bullets = new ArrayList<>();
    public final List<PowerUp> powerUps = new ArrayList<>();
    public final Map<PowerUpType, Double> activePowerUps = new EnumMap<>(PowerUpType.class);

    public int score = Constants.DEFAULT_SCORE;
    public int highScore = 0;
    public int lives = Constants.DEFAULT_LIVES;
    public int level = Constants.DEFAULT_LEVEL;
    public boolean gameCompleted = false;
    public boolean gameOver = false;

    public boolean running = true;
    public boolean paused = false;
    public boolean levelTransitionPending = false;
    public double timeScale = 1.1;
    public double basePaddleWidth;
    public double basePaddleSpeed;
    public double laserCooldown;

    public GameState(Ball ball, Paddle paddle) {
        this.ball = ball;
        this.paddle = paddle;
        this.balls.add(ball);
    }

    public void resetForLife() {
        paused = false;
        bullets.clear();
        laserCooldown = 0.0;
    }

    public void resetForLevel() {
        score = 0;
        lives = Constants.DEFAULT_LIVES;
        level = Constants.DEFAULT_LEVEL;
        running = true;
        paused = false;
        levelTransitionPending = false;
        timeScale = 1.0;
        bullets.clear();
        laserCooldown = 0.0;
    }

    public void decrementLives() {
        lives--;
    }

    public void incrementLives() {
        lives++;
    }
}

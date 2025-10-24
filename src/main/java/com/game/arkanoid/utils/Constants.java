package com.game.arkanoid.utils;

/**
 * Game constants.
 *
 * @author bmngxn
 */
public class Constants {
    // Game scene size
    public static final double GAME_WIDTH  = 600.0;
    public static final double GAME_HEIGHT = 800.0;

    // Ball constants
    public static final double BALL_RADIUS = 5.0;
    public static final double BALL_SPEED  = 7.0;      // px/s launch & nominal speed
    public static final double MIN_BALL_ANGLE = 25.0;    // degrees (right-most)
    public static final double MAX_BALL_ANGLE = 155.0;   // degrees (left-most)
    public static final double BALL_RESTITUTION = 1;  // energy kept on bounce (0..1)
    public static final double BALL_NUDGE = 0.5;         // px to separate after paddle hit
    public static final double BALL_LAUNCH_ANGLE = 135.0; // degrees (straight up)

    // Paddle constants
    public static final double PADDLE_WIDTH  = 150.0;
    public static final double PADDLE_HEIGHT = 20.0;
    public static final double PADDLE_SPEED  = 10.0;     // px per (dt unit); tune as needed
    public static final double MIN_PADDLE_WIDTH = 80.0;
    public static final double MAX_PADDLE_WIDTH = 180.0;
    public static final double PADDLE_MARGIN_BOTTOM = 10.0;
    public static final double BALL_SPAWN_OFFSET = 0.5;  // gap above paddle when docking

    // Brick constants
    public static final double BRICK_WIDTH   = 43.0;
    public static final double BRICK_HEIGHT  = 21.0;
    public static final double BRICK_SPACING = 5.0;
    public static final int BRICK_ROWS = 8;
    public static final int BRICK_COLS = 13;

    // Power-ups
    public static final double POWER_UP_WIDTH = 32.0;
    public static final double POWER_UP_HEIGHT = 16.0;
    public static final double POWER_UP_FALL_SPEED = 8.0;
    public static final double POWER_UP_DROP_CHANCE = 0.25;
    public static final double POWER_UP_DURATION = 12.0;
    public static final double POWER_UP_EXPAND_FACTOR = 1.4;
    public static final double POWER_UP_SHRINK_FACTOR = 0.7;
    public static final double POWER_UP_SLOW_MOTION_SCALE = 0.6;

    public static final int DEFAULT_SCORE = 0;
    public static final int DEFAULT_LIVES = 3;
    public static final int DEFAULT_LEVEL = 1;
}

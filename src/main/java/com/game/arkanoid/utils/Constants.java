package com.game.arkanoid.utils;

/**
 * Game constants.
 * 
 * @author bmngxn
 */
public class Constants {
    // Game scene size
    public static final double GAME_WIDTH = 800.0;
    public static final double GAME_HEIGHT = 600.0;
    
    // Ball constants
    public static final double BALL_RADIUS = 8.0;
    public static final double BALL_SPEED = 300.0;
    public static final double MIN_BALL_ANGLE = 25.0;
    public static final double MAX_BALL_ANGLE = 155.0;
    
    // Paddle constants
    public static final double PADDLE_WIDTH = 120.0;
    public static final double PADDLE_HEIGHT = 16.0;
    public static final double PADDLE_SPEED = 400.0;
    public static final double MIN_PADDLE_WIDTH = 80.0;
    public static final double MAX_PADDLE_WIDTH = 180.0;
    
    // Brick constants
    public static final double BRICK_WIDTH = 80.0;
    public static final double BRICK_HEIGHT = 30.0;
    public static final double BRICK_SPACING = 5.0;
    public static final int BRICK_ROWS = 8;
    public static final int BRICK_COLS = 10;
}

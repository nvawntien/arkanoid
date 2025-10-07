package com.game.arkanoid.models;

import java.util.ArrayList;
import java.util.List;
import com.game.arkanoid.utils.Constants;

/** All runtime state for a play session.  xới
 * 
 * @author bmngxn
 */
public final class GameState {
    // Core entities
    public final Ball ball;
    public final Paddle paddle;

    // World content (fill out as you add features)
    // public final List<Brick> bricks = new ArrayList<>(); // define Brick later

    public int score = Constants.DEFAULT_SCORE;
    public int lives = Constants.DEFAULT_LIVES;
    public int level = Constants.DEFAULT_LEVEL;

    public boolean running = true;  // false --> stop updating
    public boolean paused  = false;
    public double timeScale = 1.0;  // slow-mo / speedup hook

    /**
     * GameState constructor.
     * @param ball ball in this gamestate
     * @param paddle paddle in this gamestate
     */
    public GameState(Ball ball, Paddle paddle) {
        this.ball = ball;
        this.paddle = paddle;
    }

    /** 
     * Convenience: reset per-life without recreating the whole state. 
     */
    public void resetForLife() {
        // keep it as state-only; services will position ball on paddle
        paused = false;
    }

    /** 
     * Convenience: reset per-level (extend as needed). 
     */
    public void resetForLevel() {
        // bricks.clear();
        score = 0;
        lives = 3;
        level = 1;
        running = true;
        paused = false;
        timeScale = 1.0;
    }
}

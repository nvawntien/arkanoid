package com.game.arkanoid.models;

import com.game.arkanoid.utils.Constants;
import java.util.ArrayList;
import java.util.List;

/**
 * A lightweight serializable snapshot of GameState used for persistence.
 * Only contains fields needed to restore gameplay on Continue.
 */
public final class GameStateSnapshot {
    public int currentLevel;
    public int score;
    public int lives;
    public double paddleX;
    public double ballX;
    public double ballY;
    public final List<BrickState> bricks = new ArrayList<>();
    public final List<PowerUpState> fallingPowerUps = new ArrayList<>();

    public static final class BrickState {
        public double x;
        public double y;
        public int health;     // remaining health (1..4); <=0 means destroyed

        public BrickState() {}
        public BrickState(double x, double y, int health) {
            this.x = x; this.y = y; this.health = health;
        }
    }

    public static final class PowerUpState {
        public String type;
        public double x;
        public double y;
        public boolean collected;

        public PowerUpState() {}
        public PowerUpState(String type, double x, double y, boolean collected) {
            this.type = type; this.x = x; this.y = y; this.collected = collected;
        }
    }

    /** Build a snapshot from the current in-memory GameState. */
    public static GameStateSnapshot from(GameState s) {
        GameStateSnapshot snap = new GameStateSnapshot();
        snap.currentLevel = s.level;
        snap.score = s.score;
        snap.lives = s.lives;
        snap.paddleX = s.paddle.getX();
        snap.ballX = s.ball.getCenterX();
        snap.ballY = s.ball.getCenterY();
        for (Brick b : s.bricks) {
            int hp = Math.max(0, b.getHealth());
            snap.bricks.add(new BrickState(b.getX(), b.getY(), hp));
        }
        for (PowerUp p : s.powerUps) {
            snap.fallingPowerUps.add(new PowerUpState(p.getType().name(), p.getX(), p.getY(), p.isCollected()));
        }
        return snap;
    }

    /** Apply snapshot onto a GameState. Level must be preloaded. */
    public void applyTo(GameState s) {
        s.score = this.score;
        s.lives = this.lives;
        s.ball.setCenter(this.ballX, this.ballY);
        s.ball.setMoving(true);
        s.paddle.setX(this.paddleX);

        // Rebuild bricks by mapping states by coordinates (approx). Fallback to level layout sizes.
        if (!bricks.isEmpty()) {
            s.bricks.clear();
            for (BrickState bs : bricks) {
                int health = Math.max(0, bs.health);
                Brick b = new Brick(bs.x, bs.y, Constants.BRICK_WIDTH, Constants.BRICK_HEIGHT, Math.max(1, health));
                if (health <= 0) b.setHealth(0);
                s.bricks.add(b);
            }
        }

        // Falling power-ups
        s.powerUps.clear();
        for (PowerUpState ps : fallingPowerUps) {
            PowerUpType type;
            try { type = PowerUpType.valueOf(ps.type); } catch (Exception ex) { continue; }
            PowerUp pu = new PowerUp(type, ps.x, ps.y, 24, 12, 120);
            if (ps.collected) pu.markCollected();
            s.powerUps.add(pu);
        }
        s.running = false;  // countdown before resume will start running
        s.paused = true;
    }
}


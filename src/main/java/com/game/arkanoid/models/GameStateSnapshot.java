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
    public double ballDx;
    public double ballDy;
    public boolean ballMoving;
    public boolean ballDownward; // direction hint for fallback
    public boolean ballStuck;
    public double ballStuckOffsetX;

    public double paddleWidth;
    public double timeScale;
    public double laserCooldown;

    public final List<ActiveEffect> activeEffects = new ArrayList<>();

    public static final class ActiveEffect {
        public String type;
        public double remaining;
        public ActiveEffect() {}
        public ActiveEffect(String type, double remaining) { this.type = type; this.remaining = remaining; }
    }
    public final List<BrickState> bricks = new ArrayList<>();
    public final List<PowerUpState> fallingPowerUps = new ArrayList<>();
    public final List<ExtraBallState> extraBalls = new ArrayList<>();

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

    public static final class ExtraBallState {
        public double x;
        public double y;
        public double dx;
        public double dy;
        public boolean moving;
        public double radius;

        public ExtraBallState() {}
        public ExtraBallState(double x, double y, double dx, double dy, boolean moving, double radius) {
            this.x = x; this.y = y; this.dx = dx; this.dy = dy; this.moving = moving; this.radius = radius;
        }
    }

    /** Build a snapshot from the current in-memory GameState. */
    public static GameStateSnapshot from(GameState s) {
        GameStateSnapshot snap = new GameStateSnapshot();
        snap.currentLevel = s.level;
        snap.score = s.score;
        snap.lives = s.lives;
        snap.paddleX = s.paddle.getX();
        snap.paddleWidth = s.paddle.getWidth();
        snap.ballX = s.ball.getCenterX();
        snap.ballY = s.ball.getCenterY();
        snap.ballDx = s.ball.getDx();
        snap.ballDy = s.ball.getDy();
        snap.ballMoving = s.ball.isMoving();
        snap.ballDownward = s.ball.getDy() >= 0;
        snap.ballStuck = s.ball.isStuck();
        snap.ballStuckOffsetX = s.ball.getStuckOffsetX();
        snap.timeScale = s.timeScale;
        snap.laserCooldown = s.laserCooldown;

        for (var e : s.activePowerUps.entrySet()) {
            snap.activeEffects.add(new ActiveEffect(e.getKey().name(), e.getValue()));
        }
        for (Brick b : s.bricks) {
            int hp = Math.max(0, b.getHealth());
            snap.bricks.add(new BrickState(b.getX(), b.getY(), hp));
        }
        for (PowerUp p : s.powerUps) {
            snap.fallingPowerUps.add(new PowerUpState(p.getType().name(), p.getX(), p.getY(), p.isCollected()));
        }
        for (Ball b : s.extraBalls) {
            snap.extraBalls.add(new ExtraBallState(
                    b.getCenterX(), b.getCenterY(), b.getDx(), b.getDy(), b.isMoving(), b.getRadius()
            ));
        }
        return snap;
    }

    /** Apply snapshot onto a GameState. Level must be preloaded. */
    public void applyTo(GameState s) {
        s.score = this.score;
        s.lives = this.lives;
        s.ball.setCenter(this.ballX, this.ballY);
        double vx = this.ballDx;
        double vy = this.ballDy;
        boolean moving = this.ballMoving;
        // Fallback for older saves that didn't store velocity
        if (Math.abs(vx) < 1e-6 && Math.abs(vy) < 1e-6) {
            double speed = Constants.BALL_SPEED;
            double t = Math.toRadians(Constants.BALL_LAUNCH_ANGLE);
            vx = speed * Math.cos(t);
            vy = (this.ballDownward ? Math.abs(speed * Math.sin(t)) : -Math.abs(speed * Math.sin(t)));
            moving = true;
        }
        s.ball.setVelocity(vx, vy);
        s.ball.setMoving(moving);
        s.ball.setStuck(this.ballStuck);
        s.ball.setStuckOffsetX(this.ballStuckOffsetX);
        s.paddle.setX(this.paddleX);

        // Restore power-up states / paddle width / timescale / laser cooldown
        s.timeScale = (this.timeScale > 0 ? this.timeScale : 1.0);
        s.laserCooldown = Math.max(0.0, this.laserCooldown);
        s.activePowerUps.clear();
        for (ActiveEffect ef : activeEffects) {
            PowerUpType type;
            try { type = PowerUpType.valueOf(ef.type); } catch (Exception ex) { continue; }
            s.activePowerUps.put(type, Math.max(0.0, ef.remaining));
        }

        // Recompute paddle width from effects if present (prefer effects over stored width)
        double base = s.basePaddleWidth > 0 ? s.basePaddleWidth : s.paddle.getWidth();
        if (s.activePowerUps.containsKey(PowerUpType.EXPAND_PADDLE)) {
            s.paddle.setWidthClamped(base * Constants.POWER_UP_EXPAND_FACTOR);
        } else if (s.activePowerUps.containsKey(PowerUpType.LASER_PADDLE)) {
            s.paddle.setWidthClamped(base * Constants.POWER_UP_LASER_FACTOR);
        } else if (this.paddleWidth > 0) {
            s.paddle.setWidthClamped(this.paddleWidth);
        }

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
            PowerUp pu = new PowerUp(
                    type,
                    ps.x,
                    ps.y,
                    Constants.POWER_UP_WIDTH,
                    Constants.POWER_UP_HEIGHT,
                    Constants.POWER_UP_FALL_SPEED
            );
            if (ps.collected) pu.markCollected();
            s.powerUps.add(pu);
        }

        // Extra balls
        s.extraBalls.clear();
        for (ExtraBallState eb : extraBalls) {
            double r = eb.radius > 0 ? eb.radius : Constants.BALL_RADIUS;
            Ball b = new Ball(eb.x, eb.y, r);
            b.setVelocity(eb.dx, eb.dy);
            b.setMoving(eb.moving);
            s.extraBalls.add(b);
        }
        s.running = false;  // countdown before resume will start running
        s.paused = true;
    }
}

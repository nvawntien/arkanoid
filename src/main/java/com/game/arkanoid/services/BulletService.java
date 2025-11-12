package com.game.arkanoid.services;

import com.game.arkanoid.events.GameEventBus;
import com.game.arkanoid.events.sound.BulletFireSoundEvent;
import com.game.arkanoid.models.Brick;
import com.game.arkanoid.models.Bullet;
import com.game.arkanoid.models.GameState;
import com.game.arkanoid.models.Paddle;
import com.game.arkanoid.utils.Constants;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Handles spawning, updating, and collision detection of paddle bullets
 * when the laser power-up is active.
 */
public final class BulletService {

    /**
     * Represents the result of a bullet impacting a brick.
     *
     * @param brick the brick that was hit
     * @param destroyed true if the brick was destroyed by this hit
     */
    public record Impact(Brick brick, boolean destroyed) { }

    /** Service to manage brick state */
    private final BricksService bricksService;

    /**
     * Constructor for BulletService.
     *
     * @param bricksService service used to handle brick hits
     */
    public BulletService(BricksService bricksService) {
        this.bricksService = bricksService;
    }

    /**
     * Decrements the laser cooldown timer for firing bullets.
     *
     * @param state current game state
     * @param dt time delta in seconds
     */
    public void tickCooldown(GameState state, double dt) {
        if (state.laserCooldown > 0.0) {
            state.laserCooldown = Math.max(0.0, state.laserCooldown - dt);
        }
    }

    /**
     * Attempts to fire bullets from the paddle if the cooldown allows.
     * Adds two bullets from the left and right laser barrels.
     *
     * @param state current game state
     * @param paddle the player's paddle
     * @return true if bullets were fired, false if cooldown prevented firing
     */
    public boolean tryFire(GameState state, Paddle paddle) {
        if (state.laserCooldown > 0.0) {
            return false;
        }

        double width = Constants.BULLET_WIDTH;
        double height = Constants.BULLET_HEIGHT;
        double y = paddle.getY() - height;

        double leftX = paddle.getX() + Constants.LASER_BARREL_INSET - width * 0.5;
        double rightX = paddle.getX() + paddle.getWidth() - Constants.LASER_BARREL_INSET - width * 0.5;

        state.bullets.add(new Bullet(leftX, y, width, height, Constants.BULLET_SPEED));
        state.bullets.add(new Bullet(rightX, y, width, height, Constants.BULLET_SPEED));
        state.laserCooldown = Constants.LASER_FIRE_COOLDOWN;
        GameEventBus.getInstance().publish(new BulletFireSoundEvent());
        return true;
    }

    /**
     * Updates all bullets' positions and checks for collisions with bricks.
     * Removes bullets that leave the game bounds.
     *
     * @param state current game state
     * @param bricks list of all bricks in the level
     * @param dt time delta in seconds
     * @param worldH the height of the game world
     * @return list of impacts representing bullets that hit bricks
     */
    public List<Impact> update(GameState state, List<Brick> bricks, double dt, double worldH) {
        List<Impact> impacts = new ArrayList<>();
        Iterator<Bullet> iterator = state.bullets.iterator();
        while (iterator.hasNext()) {
            Bullet bullet = iterator.next();
            bullet.update(dt);

            if (bullet.bottom() < 172) {
                iterator.remove();
                continue;
            }

            if (bullet.top() > worldH) {
                iterator.remove();
                continue;
            }

            Brick hit = firstHit(bricks, bullet);
            if (hit != null) {
                iterator.remove();
                boolean destroyed = bricksService.handleBrickHit(hit);
                impacts.add(new Impact(hit, destroyed));
            }
        }
        return impacts;
    }

    /**
     * Finds the first brick that a bullet intersects with.
     *
     * @param bricks list of bricks to check
     * @param bullet bullet to test
     * @return the first brick hit by the bullet, or null if none
     */
    private Brick firstHit(List<Brick> bricks, Bullet bullet) {
        for (Brick brick : bricks) {
            if (brick.isDestroyed()) {
                continue;
            }
            if (intersects(bullet, brick)) {
                return brick;
            }
        }
        return null;
    }

    /**
     * Checks whether a bullet intersects a brick.
     *
     * @param bullet the bullet
     * @param brick the brick
     * @return true if the bullet intersects the brick
     */
    private boolean intersects(Bullet bullet, Brick brick) {
        double bL = brick.getX();
        double bT = brick.getY();
        double bR = bL + brick.getWidth();
        double bB = bT + brick.getHeight();

        return bullet.right() > bL
                && bullet.left() < bR
                && bullet.bottom() > bT
                && bullet.top() < bB;
    }
}

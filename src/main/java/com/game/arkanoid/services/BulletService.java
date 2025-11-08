package com.game.arkanoid.services;

import com.game.arkanoid.models.Brick;
import com.game.arkanoid.models.Bullet;
import com.game.arkanoid.models.GameState;
import com.game.arkanoid.models.Paddle;
import com.game.arkanoid.utils.Constants;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Handles spawning and stepping paddle bullets when the laser power-up is active.
 */
public final class BulletService {

    public record Impact(Brick brick, boolean destroyed) { }

    private final BricksService bricksService;

    public BulletService(BricksService bricksService) {
        this.bricksService = bricksService;
    }

    public void tickCooldown(GameState state, double dt) {
        if (state.laserCooldown > 0.0) {
            state.laserCooldown = Math.max(0.0, state.laserCooldown - dt);
        }
    }

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

        return true;
    }

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

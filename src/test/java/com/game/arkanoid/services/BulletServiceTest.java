package com.game.arkanoid.services;

import com.game.arkanoid.models.*;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class BulletServiceTest {

    @Test
    void tryFireSpawnsTwoBulletsAndCooldown() {
        BricksService bricks = new BricksService();
        BulletService svc = new BulletService(bricks);
        GameState state = new GameState(new Ball(100, 200, 8), new Paddle(80, 350, 100, 20, 200));
        Paddle p = state.paddle;
        boolean fired = svc.tryFire(state, p);
        assertTrue(fired);
        assertEquals(2, state.bullets.size());
        assertTrue(state.laserCooldown > 0.0);
    }

    @Test
    void updateRemovesBulletsWhenLeavingBoundsOrHitting() {
        BricksService bricks = new BricksService();
        BulletService svc = new BulletService(bricks);
        GameState state = new GameState(new Ball(100, 200, 8), new Paddle(80, 350, 100, 20, 200));
        // add one bullet near a brick to ensure a hit
        Bullet b = new Bullet(22, 172 + 2, 4, 10, 200);
        state.bullets.add(b);
        List<Brick> bl = new ArrayList<>();
        bl.add(new Brick(22, 172, com.game.arkanoid.utils.Constants.BRICK_WIDTH, com.game.arkanoid.utils.Constants.BRICK_HEIGHT, 1));
        List<BulletService.Impact> impacts = svc.update(state, bl, 0.1, 800);
        assertEquals(1, impacts.size());
        assertTrue(state.bullets.isEmpty());
    }
}


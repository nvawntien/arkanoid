package com.game.arkanoid.models;

import com.game.arkanoid.utils.Constants;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class GameStateSnapshotTest {

    @Test
    void snapshotIncludesAndRestoresEnemiesBallsAndPaddle() {
        // Build a game state with ball, paddle, one brick, one enemy
        Ball ball = new Ball(120, 220, Constants.BALL_RADIUS);
        ball.setVelocity(100, -80);
        ball.setMoving(true);
        Paddle paddle = new Paddle(80, 350, 100, 20, 200);
        GameState s = new GameState(ball, paddle);
        s.bricks.add(new Brick(22, 172, Constants.BRICK_WIDTH, Constants.BRICK_HEIGHT, 1));
        s.enemies.add(new Enemy(EnemyType.CUBE, 140, 200, Constants.ENEMY_WIDTH, Constants.ENEMY_HEIGHT, 40, 60));
        s.score = 42;
        s.lives = 2;
        s.level = 1;

        GameStateSnapshot snap = GameStateSnapshot.from(s);
        assertEquals(1, snap.enemies.size());
        assertEquals(1, snap.balls.size());
        assertEquals(1, snap.bricks.size());

        // Apply to a fresh state
        Ball b2 = new Ball(0, 0, Constants.BALL_RADIUS);
        Paddle p2 = new Paddle(0, 0, 100, 20, 200);
        GameState restored = new GameState(b2, p2);
        snap.applyTo(restored);

        assertEquals(42, restored.score);
        assertEquals(2, restored.lives);
        assertFalse(restored.running);
        assertTrue(restored.paused);
        assertEquals(1, restored.enemies.size());
        assertEquals(1, restored.balls.size());
        assertEquals(1, restored.bricks.size());
    }
}


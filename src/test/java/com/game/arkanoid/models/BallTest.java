package com.game.arkanoid.models;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class BallTest {

    @Test
    void constructAndMove() {
        Ball b = new Ball(100, 200, 8);
        assertEquals(100, b.getCenterX(), 1e-6);
        assertEquals(200, b.getCenterY(), 1e-6);
        assertFalse(b.isMoving());

        b.setVelocity(50, -20);
        b.setMoving(true);
        b.update(1.0);
        assertEquals(150, b.getCenterX(), 1e-6);
        assertEquals(180, b.getCenterY(), 1e-6);
    }
}


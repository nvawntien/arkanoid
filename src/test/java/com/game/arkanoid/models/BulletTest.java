package com.game.arkanoid.models;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class BulletTest {

    @Test
    void movesUpward() {
        Bullet b = new Bullet(100, 200, 4, 12, 150);
        double y0 = b.getY();
        b.update(0.5);
        assertTrue(b.getY() < y0, "Bullet should move upward (y decreasing)");
        assertTrue(b.bottom() > b.top());
        assertTrue(b.right() > b.left());
    }
}


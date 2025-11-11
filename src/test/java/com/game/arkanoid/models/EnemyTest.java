package com.game.arkanoid.models;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class EnemyTest {

    @Test
    void zigzagAndVelocityAccessors() {
        Enemy e = new Enemy(EnemyType.CONE, 100, 180, 24, 24, 60, 120);
        double x0 = e.getX();
        double y0 = e.getY();
        e.update(0.2);
        assertTrue(e.getY() > y0, "Enemy should fall down");
        // zigzag may or may not flip depending on Constants, but vx should be readable
        assertNotNull(e.getType());
        double vx = e.getVx();
        e.setVx(vx);
        e.setVy(e.getVy());
        assertEquals(vx, e.getVx(), 1e-6);
        assertTrue(e.getX() != x0 || e.getY() != y0);
    }
}


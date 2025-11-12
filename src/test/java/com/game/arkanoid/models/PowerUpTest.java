package com.game.arkanoid.models;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PowerUpTest {

    @Test
    void fallAndCollect() {
        PowerUp p = new PowerUp(PowerUpType.EXTRA_LIFE, 50, 60, 20, 12, 80);
        double y0 = p.getY();
        p.update(0.5);
        assertTrue(p.getY() > y0, "Power-up should fall down");
        assertFalse(p.isCollected());
        p.markCollected();
        assertTrue(p.isCollected());
        assertEquals(PowerUpType.EXTRA_LIFE, p.getType());
    }
}


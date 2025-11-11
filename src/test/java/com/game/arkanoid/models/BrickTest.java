package com.game.arkanoid.models;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class BrickTest {

    @Test
    void destroyAndIndestructible() {
        Brick normal = new Brick(0, 0, 40, 20, 1);
        assertFalse(normal.isDestroyed());
        normal.setHealth(0);
        assertTrue(normal.isDestroyed());

        Brick silver = new Brick(0, 0, 40, 20, 9); // indestructible
        assertTrue(silver.isIndestructible());
        int before = silver.getHealth();
        silver.setHealth(0); // should not change
        assertEquals(before, silver.getHealth());
    }
}


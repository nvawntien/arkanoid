package com.game.arkanoid.models;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PaddleTest {

    @Test
    void setWidthClampedWithinBounds() {
        Paddle p = new Paddle(50, 300, 100, 20, 200);
        double original = p.getWidth();
        p.setWidthClamped(original * 10); // will be clamped by max
        assertTrue(p.getWidth() <= p.getMaxWidth());
        p.setWidthClamped(1); // will be clamped by min
        assertTrue(p.getWidth() >= p.getMinWidth());
    }
}


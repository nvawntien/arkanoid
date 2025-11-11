package com.game.arkanoid.models;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class InputStateTest {

    @Test
    void togglesFlags() {
        InputState s = new InputState();
        assertFalse(s.left);
        s.left = true;
        s.right = true;
        s.launch = true;
        s.fire = true;
        assertTrue(s.left && s.right && s.launch && s.fire);
    }
}


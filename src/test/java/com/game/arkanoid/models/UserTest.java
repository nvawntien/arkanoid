package com.game.arkanoid.models;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class UserTest {

    @Test
    void createAndAccess() {
        User u = new User(1, "Alice", "hash", 100, 3, null);
        assertEquals(1, u.getId());
        assertEquals("Alice", u.getName());
        assertEquals("hash", u.getPasswordHash());
        assertEquals(100, u.getBestScore());
        assertEquals(3, u.getBestRound());
    }
}


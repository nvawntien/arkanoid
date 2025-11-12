package com.game.arkanoid.models;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class EnumsTest {

    @Test
    void enemyTypeValuesPresent() {
        assertTrue(EnemyType.values().length > 0);
    }

    @Test
    void powerUpTypeValuesPresent() {
        assertTrue(PowerUpType.values().length > 0);
    }

    @Test
    void doorTypeValuesPresent() {
        assertTrue(DoorType.values().length > 0);
    }
}


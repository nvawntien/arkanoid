package com.game.arkanoid.models;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class RankingEntryTest {

    @Test
    void gettersReturnValues() {
        RankingEntry e = new RankingEntry("Player", 1234, 4);
        assertEquals("Player", e.getName());
        assertEquals(1234, e.getBestScore());
        assertEquals(4, e.getBestRound());
    }
}


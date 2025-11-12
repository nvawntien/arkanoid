package com.game.arkanoid.services;

import static org.junit.jupiter.api.Assertions.*;

import com.game.arkanoid.models.Brick;
import com.game.arkanoid.utils.Constants;
import java.util.List;
import org.junit.jupiter.api.Test;

class BricksServiceTest {

    @Test
    void createBricksFromLayoutTracksRemainingAndDestruction() {
        int[][] layout = new int[Constants.BRICK_ROWS][Constants.BRICK_COLS];
        layout[0][0] = 1;
        layout[0][1] = 2;

        BricksService service = new BricksService();
        List<Brick> bricks = service.createBricksFromLayout(layout);

        assertEquals(2, bricks.size());
        assertEquals(2, service.getBricksRemaining());

        Brick singleHit = bricks.get(0);
        assertTrue(service.handleBrickHit(singleHit));
        assertTrue(singleHit.isDestroyed());
        assertEquals(1, service.getBricksRemaining());

        Brick doubleHit = bricks.get(1);
        assertFalse(service.handleBrickHit(doubleHit));
        assertFalse(doubleHit.isDestroyed());
        assertEquals(1, service.getBricksRemaining());

        assertTrue(service.handleBrickHit(doubleHit));
        assertTrue(doubleHit.isDestroyed());
        assertEquals(0, service.getBricksRemaining());
    }

    @Test
    void createBricksFromResourceIgnoresInvalidSymbolsAndClampsHealth() {
        BricksService service = new BricksService();
        List<Brick> bricks = service.createBricksFromResource("/com/game/arkanoid/levels/test_level.txt");

        assertEquals(3, bricks.size());
        assertEquals(3, service.getBricksRemaining());

        Brick maxHealthBrick = bricks.stream()
                .max((a, b) -> Integer.compare(a.getHealth(), b.getHealth()))
                .orElseThrow();
        assertEquals(4, maxHealthBrick.getHealth());

        bricks.get(0).setHealth(0);
        bricks.get(1).setHealth(0);
        service.recalculateBricksRemaining(bricks);
        assertEquals(1, service.getBricksRemaining());
    }
}

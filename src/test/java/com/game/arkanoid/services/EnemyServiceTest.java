package com.game.arkanoid.services;

import com.game.arkanoid.models.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class EnemyServiceTest {

    @Test
    void spawnAndUpdateMovesEnemies() {
        EnemyService svc = new EnemyService();
        GameState state = new GameState(new Ball(100, 200, 8), new Paddle(80, 350, 100, 20, 200));
        assertEquals(0, state.enemies.size());
        svc.spawnEnemy(state, 120, 180);
        assertFalse(state.enemies.isEmpty());
        double x0 = state.enemies.get(0).getX();
        double y0 = state.enemies.get(0).getY();
        svc.update(state, 0.2, 600, 800);
        assertTrue(state.enemies.get(0).getY() >= y0);
        assertTrue(state.enemies.get(0).getX() != x0 || state.enemies.get(0).getY() != y0);
    }
}


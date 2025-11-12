package com.game.arkanoid.services;

import com.game.arkanoid.models.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PowerUpServiceTest {

    @Test
    void collectingExpandAppliesEffectAndRemovesPowerup() {
        PowerUpService svc = new PowerUpService();
        GameState state = new GameState(new Ball(100, 200, 8), new Paddle(80, 350, 100, 20, 200));
        // place a power-up overlapping the paddle
        PowerUp pu = new PowerUp(PowerUpType.EXPAND_PADDLE, state.paddle.getX(), state.paddle.getY(), 20, 12, 80);
        state.powerUps.add(pu);
        svc.update(state, 0.016, 600, 800);
        assertTrue(state.powerUps.isEmpty(), "Power-up should be removed once collected");
        assertTrue(state.activePowerUps.containsKey(PowerUpType.EXPAND_PADDLE));
        assertTrue(state.paddle.getWidth() >= state.basePaddleWidth, "Paddle width should expand or clamp to base bounds");
    }
}


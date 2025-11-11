package com.game.arkanoid.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for GameState.
 * Tests core logic such as life count, reset behavior, and state flags.
 */
public class GameStateTest {

    private GameState gameState;

    @BeforeEach
    void setUp() {
        //  Ball(centerX, centerY, radius)
        Ball ball = new Ball(100, 200, 8);

        //  Paddle(x, y, width, height, speed)
        Paddle paddle = new Paddle(80, 350, 100, 20, 200);

        //  Create new GameState
        gameState = new GameState(ball, paddle);
    }

    @Test
    void testInitialValues() {
        assertEquals(3, gameState.lives, "Default lives should be 3");
        assertEquals(1, gameState.level, "Default level should be 1");
        assertTrue(gameState.running, "Game should start as running");
        assertFalse(gameState.paused, "Game should not start paused");
        assertEquals(0, gameState.score, "Initial score should be 0");
    }

    @Test
    void testDecrementLives() {
        int before = gameState.lives;
        gameState.decrementLives();
        assertEquals(before - 1, gameState.lives, "Lives should decrease by one");
    }

    @Test
    void testIncrementLives() {
        int before = gameState.lives;
        gameState.incrementLives();
        assertEquals(before + 1, gameState.lives, "Lives should increase by one");
    }

    @Test
    void testResetForLifeClearsBullets() {
        // Add fake bullets and cooldown
        gameState.bullets.add(new Bullet(100, 100, 6, 12, 150));
        gameState.laserCooldown = 3.5;

        gameState.resetForLife();

        assertEquals(0, gameState.bullets.size(), "resetForLife should clear all bullets");
        assertEquals(0.0, gameState.laserCooldown, 0.0001, "laserCooldown should reset to 0");
        assertFalse(gameState.paused, "Game should unpause after life reset");
    }

    @Test
    void testResetForLevelResetsCoreStats() {
        gameState.score = 999;
        gameState.level = 5;
        gameState.lives = 1;
        gameState.paused = true;

        gameState.resetForLevel();

        assertEquals(1, gameState.level, "Level should reset to default");
        assertEquals(3, gameState.lives, "Lives should reset to default");
        assertEquals(0, gameState.score, "Score should reset to 0");
        assertFalse(gameState.paused, "Paused flag should be cleared");
        assertTrue(gameState.running, "Running should be true after reset");
    }

    @Test
    void testDecrementScoreNeverGoesNegative() {
        gameState.score = 5;
        gameState.decrementScore(10);
        assertEquals(0, gameState.score, "Score should never go below zero");
    }
}

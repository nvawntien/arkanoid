package com.game.arkanoid.services;

import com.game.arkanoid.models.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class GameServiceTest {

    @Test
    void startNextLevelSetsFlags() {
        BricksService bricks = new BricksService();
        BallService ball = new BallService();
        PaddleService paddle = new PaddleService();
        PowerUpService power = new PowerUpService();
        EnemyService enemy = new EnemyService();
        BulletService bullet = new BulletService(bricks);
        RoundService round = new RoundService(bricks, ball, paddle);
        GameService game = new GameService(ball, paddle, bricks, power, bullet, round, enemy);

        GameState state = new GameState(new Ball(100, 200, 8), new Paddle(80, 350, 100, 20, 200));
        round.loadLevel(state, 1);
        assertTrue(state.paused);
        game.startNextLevel(state);
        assertFalse(state.paused);
        assertTrue(state.running);
    }
}


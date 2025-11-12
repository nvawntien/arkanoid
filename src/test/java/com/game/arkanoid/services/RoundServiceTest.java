package com.game.arkanoid.services;

import com.game.arkanoid.models.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class RoundServiceTest {

    @Test
    void loadLevelPopulatesBricksAndResetsState() {
        BricksService bricks = new BricksService();
        BallService ball = new BallService();
        PaddleService paddle = new PaddleService();
        RoundService round = new RoundService(bricks, ball, paddle);

        GameState state = new GameState(new Ball(100, 200, 8), new Paddle(80, 350, 100, 20, 200));
        round.loadLevel(state, 1);
        assertFalse(state.bricks.isEmpty());
        assertTrue(state.balls.contains(state.ball));
        assertTrue(state.paused);
        assertFalse(state.running);
    }
}


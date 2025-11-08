package com.game.arkanoid.services;

import static org.junit.jupiter.api.Assertions.*;

import com.game.arkanoid.models.Ball;
import com.game.arkanoid.models.GameState;
import com.game.arkanoid.models.Paddle;
import com.game.arkanoid.utils.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RoundServiceTest {

    private BricksService bricksService;
    private BallService ballService;    
    private PaddleService paddleService;
    private RoundService roundService;
    private GameState state;

    @BeforeEach
    void setUp() {
        bricksService = new BricksService();
        ballService = new BallService();
        paddleService = new PaddleService();
        roundService = new RoundService(bricksService, ballService, paddleService);

        double paddleWidth = Constants.PADDLE_WIDTH;
        Paddle paddle = new Paddle(
                Constants.GAME_WIDTH / 2.0 - paddleWidth / 2.0,
                Constants.GAME_HEIGHT - Constants.PADDLE_HEIGHT - Constants.PADDLE_MARGIN_BOTTOM,
                paddleWidth,
                Constants.PADDLE_HEIGHT,
                Constants.PADDLE_SPEED);
        paddle.setWidthBounds(Constants.MIN_PADDLE_WIDTH, Constants.MAX_PADDLE_WIDTH);

        Ball ball = new Ball(
                Constants.GAME_WIDTH / 2.0,
                paddle.getY() - Constants.BALL_RADIUS - Constants.BALL_SPAWN_OFFSET,
                Constants.BALL_RADIUS);

        state = new GameState(ball, paddle);
        state.lives = Constants.DEFAULT_LIVES;
        roundService.loadLevel(state, 1);
    }

    @Test
    void loadNextLevelAdvancesUntilCompletion() {
        assertEquals(1, state.level);
        assertFalse(state.gameCompleted);

        for (int expected = 2; expected <= 4; expected++) {
            roundService.loadNextLevel(state);
            assertEquals(expected, state.level);
            assertFalse(state.gameCompleted);
        }

        roundService.loadNextLevel(state);
        assertTrue(state.gameCompleted);
        assertEquals(4, state.level);
    }

    @Test
    void loadLevelResetsBallAndClearsState() {
        state.ball.setCenter(0, 0);
        state.ball.setMoving(true);
        state.extraBalls.add(new Ball(10, 10, Constants.BALL_RADIUS));

        roundService.loadLevel(state, 2);

        assertFalse(state.ball.isMoving());
        double expectedX = state.paddle.getX() + state.paddle.getWidth() / 2.0;
        double expectedY = state.paddle.getY() - state.ball.getRadius() - Constants.BALL_SPAWN_OFFSET;
        assertEquals(expectedX, state.ball.getCenterX());
        assertEquals(expectedY, state.ball.getCenterY());
        assertTrue(state.extraBalls.isEmpty());
    }
}

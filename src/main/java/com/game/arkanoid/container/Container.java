package com.game.arkanoid.container;
import java.util.List;
import com.game.arkanoid.models.*;
import com.game.arkanoid.services.*;
import com.game.arkanoid.utils.Constants;

public final class Container {
    private final GameState state;
    private final GameService game;

    public Container() {
        Paddle paddle = new Paddle(
            Constants.GAME_WIDTH / 2.0 - Constants.PADDLE_WIDTH / 2.0,
            Constants.GAME_HEIGHT - Constants.PADDLE_HEIGHT - Constants.PADDLE_MARGIN_BOTTOM,
            Constants.PADDLE_WIDTH, Constants.PADDLE_HEIGHT,
            Constants.PADDLE_SPEED
        );

        Ball ball = new Ball(
            Constants.GAME_WIDTH / 2.0,
            paddle.getY() - Constants.BALL_RADIUS - Constants.BALL_SPAWN_OFFSET,
            Constants.BALL_RADIUS
        );

        this.state = new GameState(ball, paddle);
        this.state.lives = Constants.DEFAULT_LIVES;
        this.state.score = Constants.DEFAULT_SCORE;
        this.state.level = Constants.DEFAULT_LEVEL;

        // 👉 Thêm bricks
        BricksService bricksSvc = new BricksService();
        int[][] layout = {
    {1,2,3,4,1,2,3,4,1,2,3,4,1,2,3,4,1,2,3,4,1,2,3,4,1,2,3,4,1,2,3,4,1,2,3,4,1,2,3,4},
    {2,3,4,1,2,3,4,1,2,3,4,1,2,3,4,1,2,3,4,1,2,3,4,1,2,3,4,1,2,3,4,1,2,3,4,1,2,3,4,1},
    {3,4,1,2,3,4,1,2,3,4,1,2,3,4,1,2,3,4,1,2,3,4,1,2,3,4,1,2,3,4,1,2,3,4,1,2,3,4,1,2},
    {4,1,2,3,4,1,2,3,4,1,2,3,4,1,2,3,4,1,2,3,4,1,2,3,4,1,2,3,4,1,2,3,4,1,2,3,4,1,2,3},
    {1,3,2,4,1,3,2,4,1,3,2,4,1,3,2,4,1,3,2,4,1,3,2,4,1,3,2,4,1,3,2,4,1,3,2,4,1,3,2,4},
    {2,4,3,1,2,4,3,1,2,4,3,1,2,4,3,1,2,4,3,1,2,4,3,1,2,4,3,1,2,4,3,1,2,4,3,1,2,4,3,1},
    {3,1,4,2,3,1,4,2,3,1,4,2,3,1,4,2,3,1,4,2,3,1,4,2,3,1,4,2,3,1,4,2,3,1,4,2,3,1,4,2},
    {4,2,1,3,4,2,1,3,4,2,1,3,4,2,1,3,4,2,1,3,4,2,1,3,4,2,1,3,4,2,1,3,4,2,1,3,4,2,1,3},
    {1,4,3,2,1,4,3,2,1,4,3,2,1,4,3,2,1,4,3,2,1,4,3,2,1,4,3,2,1,4,3,2,1,4,3,2,1,4,3,2},
    {2,3,1,4,2,3,1,4,2,3,1,4,2,3,1,4,2,3,1,4,2,3,1,4,2,3,1,4,2,3,1,4,2,3,1,4,2,3,1,4}
};


        List<Brick> bricks = bricksSvc.createBricksFromLayout(layout);
        this.state.bricks.addAll(bricks);

        // Services
        BallService ballSvc = new BallService();
        PaddleService paddleSvc = new PaddleService();

        // Orchestrator
        this.game = new GameService(ballSvc, paddleSvc, bricksSvc);
    }

    public GameState getGameState() { return state; }
    public GameService getGameService() { return game; }
}

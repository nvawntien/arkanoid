// Container.java
package com.game.arkanoid.container;

import com.game.arkanoid.models.Ball;
import com.game.arkanoid.models.Paddle;
import com.game.arkanoid.services.BallService;
import com.game.arkanoid.services.PaddleService;
import com.game.arkanoid.services.GameService;
import com.game.arkanoid.utils.Constants;

public class Container {

    // Tạo service con
    private final BallService ballService;
    private final PaddleService paddleService;

    // Tạo GameService
    private final GameService gameService;

    public Container() {
        // 1. Tạo model
        Paddle paddle = new Paddle(
                Constants.GAME_WIDTH / 2 - Constants.PADDLE_WIDTH / 2,
                Constants.GAME_HEIGHT - Constants.PADDLE_HEIGHT - 10 );
        Ball ball = new Ball(
                Constants.GAME_WIDTH / 2,
                Constants.GAME_HEIGHT - Constants.PADDLE_HEIGHT - 5 - Constants.BALL_RADIUS - 5, Constants.BALL_RADIUS);

        // 2. Tạo service con
        this.ballService = new BallService(ball);
        this.paddleService = new PaddleService(paddle);

        // 3. Tạo GameService
        this.gameService = new GameService(ballService, paddleService);
    }

    public BallService getBallService() { return ballService; }
    public PaddleService getPaddleService() { return paddleService; }
    public GameService getGameService() { return gameService; }
}

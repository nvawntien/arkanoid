package com.game.arkanoid.container;

import com.game.arkanoid.config.GameSettings;
import com.game.arkanoid.models.*;
import com.game.arkanoid.services.*;
import com.game.arkanoid.utils.Constants;
import com.game.arkanoid.view.sound.SoundManager;

public final class Container {

    private static Container instance; // 🔹 Singleton instance
    private final GameState state;
    private final GameService game;

    /**
     * Private constructor initializes game state and services.
     */
    private Container() { 
        double paddleWidth = Constants.PADDLE_WIDTH * GameSettings.getPaddleWidthMultiplier();
        Paddle paddle = new Paddle(
                Constants.GAME_WIDTH / 2.0 - paddleWidth / 2.0,
                Constants.GAME_HEIGHT - Constants.PADDLE_HEIGHT - Constants.PADDLE_MARGIN_BOTTOM,
                paddleWidth,
                Constants.PADDLE_HEIGHT,
                Constants.PADDLE_SPEED
        );
        paddle.setWidthBounds(Constants.MIN_PADDLE_WIDTH, Constants.MAX_PADDLE_WIDTH);

        Ball ball = new Ball(
                Constants.GAME_WIDTH / 2.0,
                paddle.getY() - Constants.BALL_RADIUS - Constants.BALL_SPAWN_OFFSET,
                Constants.BALL_RADIUS
        );

        this.state = new GameState(ball, paddle);
        this.state.resetForLevel();
        this.state.basePaddleWidth = paddle.getWidth();
        this.state.basePaddleSpeed = paddle.getSpeed();
        this.state.highScore = GameSettings.getHighScore();

        BricksService bricksSvc = new BricksService();
        BallService ballSvc = new BallService();
        PaddleService paddleSvc = new PaddleService();
        SoundManager sound = SoundManager.getInstance();
        PowerUpService powerUpSvc = new PowerUpService();
        EnemyService enemySvc = new EnemyService();
        BulletService bulletSvc = new BulletService(bricksSvc);
        RoundService roundSvc = new RoundService(bricksSvc, ballSvc, paddleSvc);
        roundSvc.loadLevel(this.state, 1);

        this.game = new GameService(ballSvc, paddleSvc, bricksSvc, powerUpSvc, bulletSvc, roundSvc, enemySvc);
        this.game.bindState(this.state);
    }

    /**
     * Get the singleton instance.
     * @return
     */
    public static synchronized Container getInstance() {
        if (instance == null) instance = new Container();
        return instance;
    }

    /**
     * Reset the container singleton (for new game).
     */
    public static synchronized void reset() {
        instance = new Container();
    }

    public GameState getGameState() { return state; }
    public GameService getGameService() { return game; }
}

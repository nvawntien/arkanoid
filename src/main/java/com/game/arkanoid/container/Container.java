package com.game.arkanoid.container;

import com.game.arkanoid.config.GameSettings;
import com.game.arkanoid.models.Ball;
import com.game.arkanoid.models.GameState;
import com.game.arkanoid.models.Paddle;
import com.game.arkanoid.services.BallService;
import com.game.arkanoid.services.BricksService;
import com.game.arkanoid.services.BulletService;
import com.game.arkanoid.services.GameService;
import com.game.arkanoid.services.RoundService;
import com.game.arkanoid.services.PaddleService;
import com.game.arkanoid.services.PowerUpService;
import com.game.arkanoid.view.sound.SoundRenderer;
import com.game.arkanoid.utils.Constants;


public final class Container {

    private final GameState state;
    private final GameService game;

    public Container() {
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
        //add 
        this.state.highScore = GameSettings.getHighScore();

        BricksService bricksSvc = new BricksService();
        BallService ballSvc = new BallService();
        PaddleService paddleSvc = new PaddleService();
        SoundRenderer sound = SoundRenderer.getInstance();
        PowerUpService powerUpSvc = new PowerUpService(sound);
        BulletService bulletSvc = new BulletService(bricksSvc);

        RoundService roundSvc = new RoundService(bricksSvc, ballSvc);
        // Load level 1 from resources directory
        roundSvc.loadLevel(this.state, 1);

        this.game = new GameService(ballSvc, paddleSvc, bricksSvc, powerUpSvc, bulletSvc, roundSvc);
        this.game.bindState(this.state);
    }

    public GameState getGameState() {
        return state;
    }

    public GameService getGameService() {
        return game;
    }
}

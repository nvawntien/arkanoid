package com.game.arkanoid.controller;

import com.game.arkanoid.models.Ball;
import com.game.arkanoid.services.BallService;
import javafx.scene.layout.Pane;

public class BallController {
    private final Ball ball;
    private final BallService svc;
    private final Pane gamePane;

    public BallController(Ball ball, Pane gamePane) {
        this.ball = ball;
        this.gamePane = gamePane;
        this.svc = new BallService(ball);
    }

    public void update() {
        // controller lấy kích thước từ Pane, rồi truyền xuống svc
        svc.update(gamePane.getWidth(), gamePane.getHeight());

        // sau khi logic xong, cập nhật giao diện
        ball.getNode().setCenterX(ball.getX());
        ball.getNode().setCenterY(ball.getY());
    }

    public void start()  { svc.start(); }
    public void stop()   { svc.stop(); }

    public void reset(double x, double y) {
        svc.reset(x, y);
        ball.getNode().setCenterX(x);
        ball.getNode().setCenterY(y);
    }
}

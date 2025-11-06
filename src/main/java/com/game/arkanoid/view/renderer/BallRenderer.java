package com.game.arkanoid.view.renderer;

import com.game.arkanoid.models.Ball;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

/**
 * author: nvawntien.
 */
public final class BallRenderer implements Renderer<Ball> {
    private final ImageView node;

    public BallRenderer(Pane pane, Ball ball) {
        Image image = new Image(getClass().getResourceAsStream("/com/game/arkanoid/images/ball.png"));
        node = new ImageView(image);

        double diameter = ball.getRadius() * 2;
        node.setFitWidth(diameter);
        node.setFitHeight(diameter);

        node.setSmooth(true);

        node.setTranslateX(ball.getX() - ball.getRadius());
        node.setTranslateY(ball.getY() - ball.getRadius());

        pane.getChildren().add(node);
    }

    @Override
    public void render(Ball ball) {
        node.setTranslateX(ball.getX() - ball.getRadius());
        node.setTranslateY(ball.getY() - ball.getRadius());
    }

    @Override
    public ImageView getNode() {
        return node;
    }
}

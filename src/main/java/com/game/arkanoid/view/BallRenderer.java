package com.game.arkanoid.view;

import com.game.arkanoid.models.Ball;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;

/**
 * Ball renderer.
 *
 * @author bmngxn
 */
public final class BallRenderer {
    private final Circle node;

    public BallRenderer(Pane pane, Ball ball) {
        this.node = new Circle(ball.getRadius());
        this.node.getStyleClass().add("ball");
        pane.getChildren().add(this.node);
        render(ball);
    }

    public void render(Ball ball) {
        node.setCenterX(ball.getX());
        node.setCenterY(ball.getY());
        //  node.setRadius(ball.getRadius());            If radius can change via power-ups, add this sync
    }

    public Circle getNode() {
        return node;
    }
}
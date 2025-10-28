package com.game.arkanoid.view;

import com.game.arkanoid.models.Ball;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;

/**
 * Renders secondary balls spawned through the multi-ball power-up.
 */
public final class ExtraBallsRenderer {

    private final Pane pane;
    private final Map<Ball, Circle> nodes = new IdentityHashMap<>();

    public ExtraBallsRenderer(Pane pane) {
        this.pane = pane;
    }

    public void render(List<Ball> balls) {
        Iterator<Map.Entry<Ball, Circle>> it = nodes.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Ball, Circle> entry = it.next();
            if (!balls.contains(entry.getKey())) {
                pane.getChildren().remove(entry.getValue());
                it.remove();
            }
        }

        for (Ball ball : balls) {
            Circle node = nodes.computeIfAbsent(ball, this::createNode);
            node.setCenterX(ball.getX());
            node.setCenterY(ball.getY());
        }
    }

    private Circle createNode(Ball ball) {
        Circle circle   = new Circle(ball.getRadius());
        circle.getStyleClass().add("ball");
        pane.getChildren().add(circle);
        return circle;
    }
}

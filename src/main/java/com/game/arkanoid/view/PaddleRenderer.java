package com.game.arkanoid.view;

import com.game.arkanoid.models.Paddle;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;

/**
 * Paddle renderer.
 *
 * @author bmngxn
 */
public final class PaddleRenderer implements Renderer<Paddle> {
    private final Rectangle node;

    public PaddleRenderer(Pane pane, Paddle p) {
        this.node = new Rectangle(p.getWidth(), p.getHeight());
        this.node.getStyleClass().add("paddle");
        pane.getChildren().add(this.node);
        render(p);                                    // initial sync
    }

    @Override
    public Node getNode() {
        return node;
    }

    @Override
    public void render(Paddle p) {
        // Paddle is AABB with top-left (x, y)
        node.setX(p.getX());
        node.setY(p.getY());
        node.setWidth(p.getWidth());
        node.setHeight(p.getHeight());
    }

    @Override
    public void dispose() {
        if (node.getParent() instanceof Pane parent) {
            parent.getChildren().remove(node);
        }
    }
}

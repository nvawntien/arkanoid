package com.game.arkanoid.view;

import com.game.arkanoid.models.Brick;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class BricksRenderer {
    private final Pane pane;
    private final Map<Brick, Rectangle> brickNodes = new HashMap<>();

    public BricksRenderer(Pane pane, List<Brick> bricks) {
        this.pane = pane;
        for (Brick b : bricks) {
            Rectangle r = new Rectangle(b.getX(), b.getY(), b.getWidth(), b.getHeight());
            r.setArcWidth(6);
            r.setArcHeight(6);

            r.getStyleClass().addAll("brick-style", getBrickClass(b.getHealth()));
            
            pane.getChildren().add(r);
            brickNodes.put(b, r);
        }
    }

    public void render(List<Brick> bricks) {
        for (Brick b : bricks) {
            Rectangle r = brickNodes.get(b);
            if (b.isDestroyed()) {
                r.setVisible(false);
            } else {
                r.setVisible(true);
                r.getStyleClass().removeIf(style -> style.startsWith("brick-"));
                r.getStyleClass().add(getBrickClass(b.getHealth()));
            }
        }
    }

    private String getBrickClass(int health) {
        return switch (health) {
            case 4 -> "brick-4";
            case 3 -> "brick-3";
            case 2 -> "brick-2";
            default -> "brick-1";
        };
    }
}

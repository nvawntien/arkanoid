package com.game.arkanoid.view;

import com.game.arkanoid.models.Brick;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;

/**
 * Renders bricks and stays in sync when the brick list changes (e.g., on level transitions).
 */
public final class BricksRenderer implements Renderer<List<Brick>> {
    private final Pane pane;
    private final Map<Brick, Rectangle> brickNodes = new HashMap<>();

    private final Image brick1Img = new Image(getClass().getResource("/com/game/arkanoid/images/brick_blue.png").toExternalForm());
    private final Image brick2Img = new Image(getClass().getResource("/com/game/arkanoid/images/brick_red.png").toExternalForm());
    private final Image brick3Img = new Image(getClass().getResource("/com/game/arkanoid/images/brick_yellow.png").toExternalForm());
    private final Image brick4Img = new Image(getClass().getResource("/com/game/arkanoid/images/brick_green.png").toExternalForm());

    public BricksRenderer(Pane pane, List<Brick> bricks) {
        this.pane = pane;
        render(bricks);
    }

    @Override
    public void render(List<Brick> bricks) {
        // Remove nodes for bricks that no longer exist
        Iterator<Map.Entry<Brick, Rectangle>> it = brickNodes.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Brick, Rectangle> e = it.next();
            if (!bricks.contains(e.getKey())) {
                pane.getChildren().remove(e.getValue());
                it.remove();
            }
        }

        // Add or update nodes for current bricks
        for (Brick b : bricks) {
            Rectangle r = brickNodes.get(b);
            if (r == null) {
                r = new Rectangle();
                r.setArcWidth(6);
                r.setArcHeight(6);
                r.getStyleClass().add("brick-style");
                pane.getChildren().add(r);
                brickNodes.put(b, r);
            }

            r.setX(b.getX());
            r.setY(b.getY());
            r.setWidth(b.getWidth());
            r.setHeight(b.getHeight());

            if (b.isDestroyed()) {
                r.setVisible(false);
            } else {
                r.setVisible(true);
                r.setFill(getBrickImagePattern(b.getHealth()));
            }
        }
    }

    @Override
    public Rectangle getNode() {
        // Not applicable for list renderer 
        return null;
    }
       
    private ImagePattern getBrickImagePattern(int health) {
        return switch (health) {
            case 4 -> new ImagePattern(brick4Img);
            case 3 -> new ImagePattern(brick3Img);
            case 2 -> new ImagePattern(brick2Img);
            default -> new ImagePattern(brick1Img);
        };
    }
}

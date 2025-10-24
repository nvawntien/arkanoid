package com.game.arkanoid.view;

import com.game.arkanoid.models.PowerUp;
import com.game.arkanoid.models.PowerUpType;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;

/**
 * Draws falling power-ups and keeps the scene graph in sync with the model.
 */
public final class PowerUpRenderer {

    private final Pane pane;
    private final Map<PowerUp, Rectangle> nodes = new IdentityHashMap<>();

    public PowerUpRenderer(Pane pane) {
        this.pane = pane;
    }

    public void render(List<PowerUp> powerUps) {
        Iterator<Map.Entry<PowerUp, Rectangle>> it = nodes.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<PowerUp, Rectangle> entry = it.next();
            if (!powerUps.contains(entry.getKey())) {
                pane.getChildren().remove(entry.getValue());
                it.remove();
            }
        }

        for (PowerUp powerUp : powerUps) {
            Rectangle node = nodes.computeIfAbsent(powerUp, this::createNode);
            node.setX(powerUp.getX());
            node.setY(powerUp.getY());
        }
    }

    private Rectangle createNode(PowerUp powerUp) {
        Rectangle rect = new Rectangle(powerUp.getWidth(), powerUp.getHeight());
        rect.getStyleClass().add("power-up");
        rect.getStyleClass().add(switch (powerUp.getType()) {
            case EXPAND_PADDLE -> "power-up-expand";
            case SHRINK_PADDLE -> "power-up-shrink";
            case MULTI_BALL -> "power-up-multi";
            case EXTRA_LIFE -> "power-up-life";
            case SLOW_MOTION -> "power-up-slow";
        });
        pane.getChildren().add(rect);
        return rect;
    }
}

package com.game.arkanoid.view.renderer;

import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

/**
 * Renders player lives (as small paddle icons) on the HUD.
 */
public class LifeRenderer implements Renderer<Integer> {
    private static final double LIFE_ICON_WIDTH = 38.0;

    private final HBox lifeBox;
    private final Image lifeIcon;
    private int lastLifeCount = Integer.MIN_VALUE;

    public LifeRenderer(HBox lifeBox) {
        this.lifeBox = lifeBox;
        this.lifeIcon = new Image(getClass().getResourceAsStream("/com/game/arkanoid/images/paddle_life.png"));
    }

    public void reset() {
        this.lastLifeCount = Integer.MIN_VALUE;
    }

    @Override
    public void render(Integer lives) {
        if (lives == null || lifeBox == null) return;
        if (lives == lastLifeCount) return; // không cần render lại nếu không đổi

        lifeBox.getChildren().clear();
        int displayLives = Math.max(0, lives);

        for (int i = 0; i < displayLives; i++) {
            ImageView icon = new ImageView(lifeIcon);
            icon.setPreserveRatio(true);
            icon.setFitWidth(LIFE_ICON_WIDTH);
            icon.getStyleClass().add("life-icon");
            lifeBox.getChildren().add(icon);
        }

        lastLifeCount = lives;
    }

    @Override
    public Node getNode() {
        return lifeBox;
    }
}

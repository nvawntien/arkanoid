package com.game.arkanoid.view.renderer;

import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

/**
 * Renders the player's remaining lives on the HUD as small paddle icons.
 * <p>
 * Updates the display only when the number of lives changes.
 * </p>
 */
public class LifeRenderer implements Renderer<Integer> {
    private static final double LIFE_ICON_WIDTH = 38.0;

    private final HBox lifeBox;
    private final Image lifeIcon;
    private int lastLifeCount = Integer.MIN_VALUE;

    /**
     * Constructs a LifeRenderer with the specified HBox container.
     *
     * @param lifeBox the HBox to display life icons
     */
    public LifeRenderer(HBox lifeBox) {
        this.lifeBox = lifeBox;
        this.lifeIcon = new Image(getClass().getResourceAsStream("/com/game/arkanoid/images/paddle_life.png"));
    }

    /**
     * Resets the renderer so that it will re-render on the next update.
     */
    public void reset() {
        this.lastLifeCount = Integer.MIN_VALUE;
    }

    /**
     * Renders the current number of lives as paddle icons.
     * <p>
     * Clears the existing icons and adds a new set based on the current life count.
     * Does nothing if the life count has not changed.
     * </p>
     *
     * @param lives the current number of player lives
     */
    @Override
    public void render(Integer lives) {
        if (lives == null || lifeBox == null) return;
        if (lives == lastLifeCount) return; // No update needed if unchanged

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

    /**
     * Returns the Node representing the life HUD container.
     *
     * @return the HBox node containing life icons
     */
    @Override
    public Node getNode() {
        return lifeBox;
    }
}

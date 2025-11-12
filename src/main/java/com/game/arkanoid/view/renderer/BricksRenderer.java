package com.game.arkanoid.view.renderer;

import com.game.arkanoid.models.Brick;
import java.util.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

/**
 * Renders bricks in the game scene.
 * <p>
 * Maintains a mapping from Brick objects to their ImageView nodes.
 * Automatically updates positions, sizes, and visibility of bricks.
 * Bricks are rendered according to their current health and type.
 * </p>
 */
public final class BricksRenderer implements Renderer<List<Brick>> {

    private final Pane pane;
    private final Map<Brick, ImageView> brickNodes = new HashMap<>();

    // Load brick images once
    private final Image brick1Img = new Image(getClass().getResource("/com/game/arkanoid/images/brick_blue.png").toExternalForm());
    private final Image brick2Img = new Image(getClass().getResource("/com/game/arkanoid/images/brick_red.png").toExternalForm());
    private final Image brick3Img = new Image(getClass().getResource("/com/game/arkanoid/images/brick_yellow.png").toExternalForm());
    private final Image brick4Img = new Image(getClass().getResource("/com/game/arkanoid/images/brick_green.png").toExternalForm());
    private final Image brick5Img = new Image(getClass().getResource("/com/game/arkanoid/images/brick_cyan.png").toExternalForm());
    private final Image brick6Img = new Image(getClass().getResource("/com/game/arkanoid/images/brick_pink.png").toExternalForm());
    private final Image brick7Img = new Image(getClass().getResource("/com/game/arkanoid/images/brick_orange.png").toExternalForm());
    private final Image brick8Img = new Image(getClass().getResource("/com/game/arkanoid/images/brick_gold.png").toExternalForm());
    private final Image brick9Img = new Image(getClass().getResource("/com/game/arkanoid/images/brick_silver.png").toExternalForm());
    
    /**
     * Constructs a BricksRenderer attached to the given Pane.
     *
     * @param pane the Pane on which bricks will be rendered
     */
    public BricksRenderer(Pane pane) {
        this.pane = pane;
    }

    /**
     * Render the given list of bricks.
     * <p>
     * Updates existing brick nodes or creates new ones if needed.
     * Sets position, size, and image according to brick health and type.
     * Hides bricks that are destroyed but still in the list.
     * </p>
     *
     * @param bricks the list of bricks to render
     */
    @Override
    public void render(List<Brick> bricks) {
        // Copy to avoid ConcurrentModificationException
        List<Brick> snapshot = List.copyOf(bricks);

        // Remove bricks that no longer exist (e.g., on level change)
        Iterator<Map.Entry<Brick, ImageView>> it = brickNodes.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Brick, ImageView> entry = it.next();
            if (!snapshot.contains(entry.getKey())) {
                pane.getChildren().remove(entry.getValue());
                it.remove();
            }
        }

        // Render current bricks
        for (Brick b : snapshot) {
            ImageView iv = brickNodes.get(b);
            if (iv == null) {
                // Create new node if not already mapped
                iv = new ImageView();
                iv.setPreserveRatio(false); // avoid distortion when scaling
                pane.getChildren().add(iv);
                brickNodes.put(b, iv);
            }

            // Set image according to health
            iv.setImage(getBrickImage(b));

            // Set position & size
            iv.setX(b.getX());
            iv.setY(b.getY());
            iv.setFitWidth(b.getWidth());
            iv.setFitHeight(b.getHeight());

            // Hide destroyed bricks (still in list)
            iv.setVisible(!b.isDestroyed());
        }
    }

    /**
     * Not applicable for list renderer.
     *
     * @return always returns null
     */
    @Override
    public ImageView getNode() {
        return null;
    }

    /**
     * Returns the image corresponding to a brick based on its health and type.
     * <p>
     * Indestructible bricks use the silver image.
     * Other bricks are selected according to their remaining health.
     * </p>
     *
     * @param b the Brick object
     * @return the Image representing the brick
     */
    private Image getBrickImage(Brick b) {
        // Prioritize indestructible brick (silver)
        if (b.isIndestructible()) {
            return brick9Img; // silver brick
        }

        // Otherwise select image based on remaining health
        return switch (b.getHealth()) {
            case 8 -> brick8Img;
            case 7 -> brick7Img;
            case 6 -> brick6Img;
            case 5 -> brick5Img;
            case 4 -> brick4Img;
            case 3 -> brick3Img;
            case 2 -> brick2Img;
            default -> brick1Img; // case 1 or <= 0 (destroyed) uses default image
        };
    }
}

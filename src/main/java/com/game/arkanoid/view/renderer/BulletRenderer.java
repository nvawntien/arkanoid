package com.game.arkanoid.view.renderer;

import com.game.arkanoid.models.Bullet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

/**
 * Renders bullets fired from the paddle during the laser power-up.
 * <p>
 * Maintains a mapping between Bullet objects and their corresponding ImageView nodes.
 * Automatically updates the position of each bullet and removes ImageView nodes
 * when bullets are no longer active.
 * </p>
 */
public final class BulletRenderer implements Renderer<List<Bullet>> {

    private final Pane pane;
    private final Map<Bullet, ImageView> nodes = new IdentityHashMap<>();
    private final Image bulletImage;

    /**
     * Constructs a BulletRenderer for the specified Pane.
     *
     * @param pane the Pane where bullets will be rendered
     */
    public BulletRenderer(Pane pane) {
        this.pane = pane;
        this.bulletImage = new Image(getClass().getResourceAsStream("/com/game/arkanoid/images/laser_bullet.png"));
    }

    /**
     * Renders the given list of bullets.
     * <p>
     * Updates existing ImageView nodes or creates new ones for new bullets.
     * Removes nodes corresponding to bullets that no longer exist.
     * </p>
     *
     * @param bullets the list of bullets to render
     */
    @Override
    public void render(List<Bullet> bullets) {
        Iterator<Map.Entry<Bullet, ImageView>> it = nodes.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Bullet, ImageView> entry = it.next();
            if (!bullets.contains(entry.getKey())) {
                pane.getChildren().remove(entry.getValue());
                it.remove();
            }
        }

        for (Bullet bullet : bullets) {
            ImageView view = nodes.computeIfAbsent(bullet, this::createNode);
            view.setTranslateX(bullet.getX());
            view.setTranslateY(bullet.getY());
        }
    }

    /**
     * Creates a new ImageView node for the given bullet and adds it to the Pane.
     *
     * @param bullet the Bullet object
     * @return the newly created ImageView node
     */
    private ImageView createNode(Bullet bullet) {
        ImageView view = new ImageView(bulletImage);
        view.setFitWidth(bullet.getWidth());
        view.setFitHeight(bullet.getHeight());
        view.setSmooth(true);
        pane.getChildren().add(view);
        return view;
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
}

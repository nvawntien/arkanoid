package com.game.arkanoid.view.renderer;

import com.game.arkanoid.models.Ball;

import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

/**
 * Renders secondary balls spawned through the multi-ball power-up.
 * <p>
 * Maintains a mapping from Ball objects to their ImageView nodes.
 * Automatically adds new balls to the scene and removes balls that no longer exist.
 * </p>
 */
public final class BallsRenderer implements Renderer<List<Ball>> {
    private final Pane pane;
    private final Map<Ball, ImageView> nodes = new IdentityHashMap<>();
    private final Image ballImage;

    /**
     * Constructs a BallsRenderer attached to the given Pane.
     *
     * @param pane the Pane on which balls will be rendered
     */
    public BallsRenderer(Pane pane) {
        this.pane = pane;
        this.ballImage = new Image(getClass().getResourceAsStream("/com/game/arkanoid/images/ball.png"));
    }

    /**
     * Render the given list of balls.
     * <p>
     * Updates existing ball positions or creates new ImageView nodes for new balls.
     * Removes nodes corresponding to balls that are no longer present.
     * </p>
     *
     * @param balls the list of balls to render
     */
    public void render(List<Ball> balls) {
        // Remove balls that no longer exist
        Iterator<Map.Entry<Ball, ImageView>> it = nodes.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Ball, ImageView> entry = it.next();
            if (!balls.contains(entry.getKey())) {
                pane.getChildren().remove(entry.getValue());
                it.remove();
            }
        }

        // Update or create new ball nodes
        for (Ball ball : balls) {
            ImageView node = nodes.computeIfAbsent(ball, this::createNode);
            node.setTranslateX(ball.getX() - ball.getRadius());
            node.setTranslateY(ball.getY() - ball.getRadius());
        }
    }

    /**
     * Create a new ImageView node for a ball and add it to the pane.
     *
     * @param ball the Ball object
     * @return the created ImageView node
     */
    private ImageView createNode(Ball ball) {
        ImageView view = new ImageView(ballImage);
        double diameter = ball.getRadius() * 2;

        view.setFitWidth(diameter);
        view.setFitHeight(diameter);
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

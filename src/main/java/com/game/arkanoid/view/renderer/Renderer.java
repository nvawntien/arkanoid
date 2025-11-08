package com.game.arkanoid.view.renderer;

import javafx.scene.Node;

/**
 * Interface for rendering a game model onto the JavaFX scene graph.
 * Classes implementing this interface are responsible for creating and
 * updating the visual representation ({@link Node}) of a specific model
 * object in the game.
 *
 * @param <Model> the type of the model this renderer handles
 * @author bmngxn
 */

public interface Renderer<Model> {
    Node getNode();

    /**
     * Sync Node from model. Must be called on the JavaFX thread.
     * @param model game template model
     */
    void render(Model model);

    /**
     * Optional cleanup (remove listeners, animations, etc.).
     */
    default void dispose() {}
}
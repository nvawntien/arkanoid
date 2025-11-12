package com.game.arkanoid.view.renderer;

import javafx.scene.Node;

/**
 * Interface for rendering a game model onto the JavaFX scene graph.
 * <p>
 * Classes implementing this interface are responsible for creating and
 * updating the visual representation ({@link Node}) of a specific model
 * object in the game. This ensures separation between game logic (model)
 * and its visual representation (view).
 * </p>
 *
 * @param <Model> the type of the model this renderer handles
 * @author bmngxn
 */
public interface Renderer<Model> {

    /**
     * Returns the root JavaFX Node associated with this renderer.
     * This node should be added to the scene graph.
     *
     * @return the root Node of the renderer
     */
    Node getNode();

    /**
     * Updates the visual representation based on the provided model.
     * <p>
     * This method must be called on the JavaFX Application Thread to
     * ensure thread-safety with JavaFX scene graph updates.
     * </p>
     *
     * @param model the game model to render
     */
    void render(Model model);

    /**
     * Optional cleanup method for the renderer.
     * <p>
     * Implementers may override this to remove listeners, stop animations,
     * or release other resources when the renderer is no longer needed.
     * </p>
     */
    default void dispose() {}
}

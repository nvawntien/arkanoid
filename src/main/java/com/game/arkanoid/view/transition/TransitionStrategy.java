package com.game.arkanoid.view.transition;

import javafx.scene.Node;

/**
 * Strategy abstraction for playing enter transitions on scene roots.
 */
@FunctionalInterface
public interface TransitionStrategy {

    /**
     * Play the transition on the given root node.
     * @param root       node to animate
     * @param onFinished callback invoked when animation completes
     */
    void play(Node root, Runnable onFinished);
}

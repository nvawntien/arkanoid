package com.game.arkanoid.models;

import javafx.geometry.Bounds;
import javafx.scene.shape.Rectangle;

/**
 * Paddle class.
 * 
 * @author bmngxn
 */

public class Paddle {
    private static final double DEFAULT_WIDTH = com.game.arkanoid.utils.Constants.PADDLE_WIDTH;
    private static final double DEFAULT_HEIGHT = com.game.arkanoid.utils.Constants.PADDLE_HEIGHT;

    private Rectangle node;
    //private double currentWidth (powerups and shi)

    /**
     * Paddle constructor
     * 
     * @param x x axis (Cartesian)
     * @param y y axis (Cartesian)
     */
    public Paddle(double x, double y) {
        node = new Rectangle(x, y, DEFAULT_WIDTH, DEFAULT_HEIGHT);
        node.getStyleClass().add("paddle"); 
        node.setArcWidth(10);
        node.setArcHeight(10);
    }

    // Getters and Setters
    public Rectangle getNode() { return node; }

    public double getWidth()  { return node.getWidth(); }
    public double getHeight() { return node.getHeight(); }

    public void setX(double x) { node.setX(x); }
    public void setY(double y) { node.setY(y); }
    public double getX() { return node.getX(); }
    public double getY() { return node.getY(); }

    public void setPosition(double x, double y) { 
        node.setX(x); 
        node.setY(y); 
    }
}

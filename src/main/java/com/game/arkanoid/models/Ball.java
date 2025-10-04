package com.game.arkanoid.models;

import javafx.scene.shape.Circle;

/**
 * Ball class.
 * 
 * @author bmngxn
 */
public class Ball {
    private static final double DEFAULT_RADIUS = com.game.arkanoid.utils.Constants.BALL_RADIUS;
    private final Circle node;
    private double currentRadius;

    /**
     * Ball constructor 1.
     * 
     * @param x the X coordinate of the ball's center
     * @param y the Y coordinate of the ball's center
     * @param radius the radius of the ball (must be > 0)
     * @throws IllegalArgumentException if radius <= 0
     */
    public Ball(double x, double y, double radius) {
        if (radius <= 0) {
            throw new IllegalArgumentException("Ball radius must be > 0");
        }

        this.currentRadius = radius;
        node = new Circle(x, y, currentRadius);
        node.getStyleClass().add("ball");
    }

    /**
     * Ball constructor 2.
     * 
     * @param x x Cartesian coordinate
     * @param y y Cartesian coordinate 
     */

    public Ball(double x, double y) {
        this.currentRadius = DEFAULT_RADIUS;
        node = new Circle(x, y, currentRadius);
        node.getStyleClass().add("ball");
    }

    //getters and setters
    public Circle getNode() { return node; }

    public double getRadius() { return node.getRadius(); }

    public double getCenterX() { return node.getCenterX(); }
    public double getCenterY() { return node.getCenterY(); }

    public void setCenter(double x, double y) {
        node.setCenterX(x);
        node.setCenterY(y);
    }
}

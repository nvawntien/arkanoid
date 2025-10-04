package com.game.arkanoid.models;

import javafx.scene.shape.Circle;

/**
 * Ball class.
 * 
 * @author bmngxn
 */
// update1 tuan
public class Ball {
    private static final double DEFAULT_RADIUS = com.game.arkanoid.utils.Constants.BALL_RADIUS;
    private final Circle node;
    private double currentRadius;
    private double dx;
    private double dy;

    /**
     * Ball constructor 1.
     * 
     * @param x the X coordinate of the ball's center
     * @param y the Y coordinate of the ball's center
     * @param radius the radius of the ball (must be > 0)
     * @throws IllegalArgumentException if radius <= 0
     */
    public Ball(double x, double y, double radius, double initialDx, double initialDy) {
        if (radius <= 0) {
            throw new IllegalArgumentException("Ball radius must be > 0");
        }

        this.currentRadius = radius;
        this.dx = initialDx;
        this.dy = initialDy;
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
        this(x, 
             y, 
             DEFAULT_RADIUS, 
             com.game.arkanoid.utils.Constants.BALL_INIT_DX, 
             com.game.arkanoid.utils.Constants.BALL_INIT_DY );
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
    public double getDx() { return dx; }
    public void setDx(double dx) { this.dx = dx; }

    public double getDy() { return dy; }
    public void setDy(double dy) { this.dy = dy; }
}
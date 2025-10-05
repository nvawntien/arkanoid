package com.game.arkanoid.models;

import javafx.scene.shape.Circle;

public class Ball {
    private Circle node;
    private double x;
    private double y;
    private double radius;
    private double dx;
    private double dy;
    private boolean isMoving;

    public Ball(double x, double y, double radius) {
        this.x = x;
        this.y = y;
        this.radius = radius;

        // initial flight direction
        this.dx = 3;
        this.dy = -3;
        this.isMoving = false;
        this.node = new Circle(this.x, this.y, this.radius);
        this.node.getStyleClass().add("ball");
    }

    public Circle getNode() {
        return node;
    }

    public void setNode(Circle node) {
        this.node = node;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public double getDx() {
        return dx;
    }

    public void setDx(double dx) {
        this.dx = dx;
    }

    public double getDy() {
        return dy;
    }

    public void setDy(double dy) {
        this.dy = dy;
    }

    public boolean isMoving() {
        return isMoving;
    }

    public void setMoving(boolean moving) {
        isMoving = moving;
    }

    public void setCenter(double x, double y) {
        this.x = x;
        this.y = y;
        node.setCenterX(x);
        node.setCenterY(y);
    }
}

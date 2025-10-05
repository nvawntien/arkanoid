package com.game.arkanoid.models;
import com.game.arkanoid.utils.Constants;
import javafx.scene.shape.Rectangle;
public class Paddle {

    private final Rectangle node;

    private double x;
    private double y;
    private double width;
    private double height;
    private double speed;

    public Paddle(double x, double y) {
        this.x = x;
        this.y = y;
        this.width = Constants.PADDLE_WIDTH ;
        this.height = Constants.PADDLE_HEIGHT;
        this.speed = Constants.PADDLE_SPEED;
        this.node = new Rectangle(x, y, width, height);
        this.node.getStyleClass().add("paddle");
    }

    public Rectangle getNode() {
        return node;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public double getSpeed() {
        return speed;
    }

    public void setX(double x) {
        this.x = x;
        this.node.setX(x);
    }

    public void setY(double y) {
        this.y = y;
        this.node.setY(y);
    }

    public void setWidth(double width) {
        this.width = width;
        this.node.setWidth(width);
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }
}

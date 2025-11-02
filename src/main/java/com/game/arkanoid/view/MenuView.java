package com.game.arkanoid.view;

import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.util.Duration;

public final class MenuView {
    private Button left;
    private Button center;
    private Button right;

    public MenuView(Button left, Button center, Button right) {
        this.left = left;
        this.center = center;
        this.right = right;
        updateHighlight();
    }

    public void moveLeft() {
        // Lưu vị trí cũ
        double leftX = left.getLayoutX(), leftY = left.getLayoutY();
        double centerX = center.getLayoutX(), centerY = center.getLayoutY();
        double rightX = right.getLayoutX(), rightY = right.getLayoutY();

        // Xoay logic
        Button oldLeft = left, oldCenter = center, oldRight = right;
        left = oldCenter;
        center = oldRight;
        right = oldLeft;

        // Animate các nút
        animateMove(oldLeft, rightX, rightY);
        animateMove(oldCenter, leftX, leftY);
        animateMove(oldRight, centerX, centerY, this::updateHighlight);
    }

    public void moveRight() {
        double leftX = left.getLayoutX(), leftY = left.getLayoutY();
        double centerX = center.getLayoutX(), centerY = center.getLayoutY();
        double rightX = right.getLayoutX(), rightY = right.getLayoutY();

        Button oldLeft = left, oldCenter = center, oldRight = right;
        left = oldRight;
        center = oldLeft;
        right = oldCenter;

        animateMove(oldLeft, centerX, centerY);
        animateMove(oldCenter, rightX, rightY);
        animateMove(oldRight, leftX, leftY, this::updateHighlight);
    }

    private void animateMove(Button btn, double newX, double newY) {
        animateMove(btn, newX, newY, null);
    }

    private void animateMove(Button btn, double newX, double newY, Runnable onFinished) {
        TranslateTransition tt = new TranslateTransition(Duration.millis(200), btn);
        tt.setByX(newX - btn.getLayoutX());
        tt.setByY(newY - btn.getLayoutY());
        tt.setOnFinished(e -> {
            btn.setLayoutX(newX);
            btn.setLayoutY(newY);
            btn.setTranslateX(0);
            btn.setTranslateY(0);
            if (onFinished != null) Platform.runLater(onFinished);
        });
        tt.play();
    }

    private void updateHighlight() {
        left.getStyleClass().remove("selected");
        center.getStyleClass().remove("selected");
        right.getStyleClass().remove("selected");

        if (!center.getStyleClass().contains("selected")) {
            center.getStyleClass().add("selected");
        }
    }

    public Button getCenterButton() {
        return center;
    }
}

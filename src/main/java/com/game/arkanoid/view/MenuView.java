package com.game.arkanoid.view;

import javafx.animation.TranslateTransition;
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
    }

    public void moveLeft() {
        // xoay vòng trái
        Button temp = left;
        left = center;
        center = right;
        right = temp;
        moveTo(left, center.getLayoutX(), center.getLayoutY()); // left -> vị trí cũ của center
        moveTo(center, right.getLayoutX(), right.getLayoutY()); // center -> vị trí cũ của right
        moveTo(right, left.getLayoutX(), left.getLayoutY());     // right ->vị trí cũ của left
        }

    public void moveRight() {
        // xoay vòng phải
        Button temp = right;
        right = center;
        center = left;
        left = temp;
      
        moveTo(left, right.getLayoutX(), right.getLayoutY());     // left -> giữ nguyên vị trí cũ của left
        moveTo(center, left.getLayoutX(), left.getLayoutY()); // center -> vị trí cũ của left
        moveTo(right, center.getLayoutX(), center.getLayoutY()); // right -> vị trí cũ của center
    }

    private void moveTo(Button btn, double newX, double newY) {
        TranslateTransition tt = new TranslateTransition(Duration.millis(200), btn);
        tt.setToX(newX - btn.getLayoutX());
        tt.setToY(newY - btn.getLayoutY());
        tt.setOnFinished(e -> {
            btn.setLayoutX(newX);
            btn.setLayoutY(newY);
            btn.setTranslateX(0);
            btn.setTranslateY(0);
        });
        tt.play();
    }

    public Button getCenterButton() {
        return center;
    }
}

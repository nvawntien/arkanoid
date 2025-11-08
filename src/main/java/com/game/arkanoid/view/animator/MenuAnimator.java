package com.game.arkanoid.view.animator;

import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Animator tổng quát cho 5 nút.
 * Ẩn 2 nút ngoài cùng, chỉ hiển thị 3 nút giữa.
 */
public final class MenuAnimator {

    private final List<Button> buttons;
    private final List<Double> originalX = new ArrayList<>();
    private final List<Double> originalY = new ArrayList<>();

    public MenuAnimator(List<Button> orderedButtons) {
        if (orderedButtons == null || orderedButtons.size() < 3)
            throw new IllegalArgumentException("MenuAnimator requires at least 3 buttons");

        this.buttons = new ArrayList<>(orderedButtons);

        for (Button b : this.buttons) {
            originalX.add(b.getLayoutX());
            originalY.add(b.getLayoutY());
        }

        updateHighlight();
        updateVisibility();
    }

    /** Xoay sang trái (menu dịch sang phải) */
    public void moveLeft() {
        List<Button> oldOrder = new ArrayList<>(buttons);
        Button first = buttons.remove(0);
        buttons.add(first);
        animateAll(oldOrder);
    }

    /** Xoay sang phải (menu dịch sang trái) */
    public void moveRight() {
        List<Button> oldOrder = new ArrayList<>(buttons);
        Button last = buttons.remove(buttons.size() - 1);
        buttons.add(0, last);
        animateAll(oldOrder);
    }

    /** Animation di chuyển tất cả các nút */
    private void animateAll(List<Button> oldOrder) {
        int n = buttons.size();

        for (int i = 0; i < n; i++) {
            Button btn = oldOrder.get(i);
            Button newButton = buttons.get(i);
            double targetX = newButton.getLayoutX();
            double targetY = newButton.getLayoutY();
            boolean isLast = (i == n - 1);
            animateMove(btn, targetX, targetY, isLast ? this::postMoveUpdate : null);
        }
    }

    /** Animation di chuyển 1 nút đến vị trí mới */
    private void animateMove(Button btn, double newX, double newY, Runnable onFinished) {
        double deltaX = newX - btn.getLayoutX();
        double deltaY = newY - btn.getLayoutY();

        TranslateTransition tt = new TranslateTransition(Duration.millis(180), btn);
        tt.setByX(deltaX);
        tt.setByY(deltaY);
        tt.setOnFinished(e -> {
            btn.setLayoutX(newX);
            btn.setLayoutY(newY);
            btn.setTranslateX(0);
            btn.setTranslateY(0);
            if (onFinished != null) Platform.runLater(onFinished);
        });
        tt.play();
    }

    /** Cập nhật sau khi xoay */
    private void postMoveUpdate() {
        updateHighlight();
        updateVisibility();
    }

    /** Cập nhật highlight cho nút trung tâm (nút có x lớn thứ 3) */
    private void updateHighlight() {
        for (Button b : buttons) b.getStyleClass().remove("selected");
        Button center = getCenterButton();
        if (center != null && !center.getStyleClass().contains("selected")) {
            center.getStyleClass().add("selected");
        }
    }

    /** Ẩn 2 nút ngoài cùng, chỉ hiển thị 3 nút giữa */
    private void updateVisibility() {
        if (buttons.size() < 3) return;

        // Sắp xếp tạm theo layoutX (trái → phải)
        List<Button> sorted = new ArrayList<>(buttons);
        sorted.sort(Comparator.comparingDouble(Button::getLayoutX));

        int centerIndex = sorted.size() / 2;
        for (int i = 0; i < sorted.size(); i++) {
            Button b = sorted.get(i);
            int distance = Math.abs(i - centerIndex);
            b.setVisible(distance < 2); // chỉ hiển thị 3 nút giữa
        }
    }

    /** Lấy nút trung tâm (nút có layoutX lớn thứ 3) */
    public Button getCenterButton() {
        if (buttons.size() < 3) return null;

        List<Button> sorted = new ArrayList<>(buttons);
        sorted.sort(Comparator.comparingDouble(Button::getLayoutX));

        int centerIndex = sorted.size() / 2;
        return sorted.get(centerIndex);
    }
}

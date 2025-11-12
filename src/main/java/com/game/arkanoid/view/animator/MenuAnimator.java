package com.game.arkanoid.view.animator;

import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * General animator for a horizontal menu of buttons.
 * <p>
 * Designed for 5 buttons: hides the outer 2 buttons and only shows the middle 3.
 * Allows smooth left/right rotation of buttons with animation.
 * </p>
 */
public final class MenuAnimator {

    private final List<Button> buttons;
    private final List<Double> originalX = new ArrayList<>();
    private final List<Double> originalY = new ArrayList<>();

    /**
     * Constructs a MenuAnimator with an ordered list of buttons.
     *
     * @param orderedButtons the buttons in initial order (at least 3 required)
     * @throws IllegalArgumentException if less than 3 buttons are provided
     */
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

    /** Rotate the menu to the left (buttons appear to move right). */
    public void moveLeft() {
        List<Button> oldOrder = new ArrayList<>(buttons);
        Button first = buttons.remove(0);
        buttons.add(first);
        animateAll(oldOrder);
    }

    /** Rotate the menu to the right (buttons appear to move left). */
    public void moveRight() {
        List<Button> oldOrder = new ArrayList<>(buttons);
        Button last = buttons.remove(buttons.size() - 1);
        buttons.add(0, last);
        animateAll(oldOrder);
    }

    /**
     * Animate all buttons to their new positions after a rotation.
     *
     * @param oldOrder the previous order of buttons before rotation
     */
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

    /**
     * Animate a single button to a new layout position.
     *
     * @param btn        the button to move
     * @param newX       target X position
     * @param newY       target Y position
     * @param onFinished optional callback after animation completes
     */
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

    /** Update state after menu rotation (highlight and visibility). */
    private void postMoveUpdate() {
        updateHighlight();
        updateVisibility();
    }

    /** Update the "selected" style for the center button. */
    private void updateHighlight() {
        for (Button b : buttons) b.getStyleClass().remove("selected");
        Button center = getCenterButton();
        if (center != null && !center.getStyleClass().contains("selected")) {
            center.getStyleClass().add("selected");
        }
    }

    /** Update visibility: hide outer buttons, show only the 3 middle ones. */
    private void updateVisibility() {
        if (buttons.size() < 3) return;

        List<Button> sorted = new ArrayList<>(buttons);
        sorted.sort(Comparator.comparingDouble(Button::getLayoutX));

        int centerIndex = sorted.size() / 2;
        for (int i = 0; i < sorted.size(); i++) {
            Button b = sorted.get(i);
            int distance = Math.abs(i - centerIndex);
            b.setVisible(distance < 2); // only show 3 center buttons
        }
    }

    /**
     * Get the center button of the menu (middle X position).
     *
     * @return the center button, or null if not enough buttons
     */
    public Button getCenterButton() {
        if (buttons.size() < 3) return null;

        List<Button> sorted = new ArrayList<>(buttons);
        sorted.sort(Comparator.comparingDouble(Button::getLayoutX));

        int centerIndex = sorted.size() / 2;
        return sorted.get(centerIndex);
    }
}

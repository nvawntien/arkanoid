package com.game.arkanoid.view.renderer;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.game.arkanoid.events.GameEventBus;
import com.game.arkanoid.events.game.OpenDoorTopLeftEvent;
import com.game.arkanoid.events.game.OpenDoorTopRightEvent;
import com.game.arkanoid.events.game.CloseDoorTopLeftEvent;
import com.game.arkanoid.events.game.CloseDoorTopRightEvent;
import com.game.arkanoid.events.game.DoorOpenedEvent;

/**
 * Renderer for the top doors in the game.
 * <p>
 * Handles opening and closing animations for left and right doors using
 * a sequence of image frames. Publishes events when doors are fully opened.
 * Supports callback functions after animation completion.
 * </p>
 */
public final class DoorTopRenderer implements Renderer<Void> {

    private final Pane pane;

    private final ImageView leftDoor;
    private final ImageView rightDoor;
    private final ImageView leftReverseDoor;
    private final ImageView rightReverseDoor;
    private final List<Image> leftFrames = new ArrayList<>();
    private final List<Image> rightFrames = new ArrayList<>();
    private final List<Image> leftReverseFrames = new ArrayList<>();
    private final List<Image> rightReverseFrames = new ArrayList<>();
    private final GameEventBus eventBus = GameEventBus.getInstance();
    private final List<GameEventBus.Subscription> subscriptions = new ArrayList<>();

    /**
     * Constructs a DoorTopRenderer for the specified pane and edge image.
     *
     * @param pane the parent pane where doors will be rendered
     * @param edge_top the reference ImageView for door layout and sizing
     */
    public DoorTopRenderer(Pane pane, ImageView edge_top) {
        this.pane = pane;

        // Load frames
        for (int i = 1; i <= 7; i++) {
            leftFrames.add(new Image(getClass().getResourceAsStream(
                    String.format("/com/game/arkanoid/images/door_top_left_%d.png", i)
            )));
            rightFrames.add(new Image(getClass().getResourceAsStream(
                    String.format("/com/game/arkanoid/images/door_top_right_%d.png", i)
            )));
        }

        leftReverseFrames.addAll(leftFrames);
        rightReverseFrames.addAll(rightFrames);
        Collections.reverse(leftReverseFrames);
        Collections.reverse(rightReverseFrames);

        leftDoor = createImageView(edge_top, leftFrames.get(0));
        leftReverseDoor = createImageView(edge_top, leftReverseFrames.get(0));
        rightDoor = createImageView(edge_top, rightFrames.get(0));
        rightReverseDoor = createImageView(edge_top, rightReverseFrames.get(0));

        pane.getChildren().addAll(leftDoor, rightDoor);
        pane.getChildren().addAll(leftReverseDoor, rightReverseDoor);
        leftReverseDoor.setVisible(false);
        rightReverseDoor.setVisible(false);
        registerEventListeners();
    }

    /**
     * Creates a new ImageView for a door frame based on the reference edge.
     *
     * @param edge_top the reference ImageView for layout and sizing
     * @param img the initial image for the door
     * @return the created ImageView
     */
    private ImageView createImageView(ImageView edge_top, Image img) {
        ImageView iv = new ImageView(img);
        iv.setLayoutX(edge_top.getLayoutX());
        iv.setLayoutY(edge_top.getLayoutY());
        iv.setFitWidth(edge_top.getFitWidth());
        iv.setFitHeight(edge_top.getFitHeight());
        iv.setSmooth(true);
        return iv;
    }

    /**
     * Registers event listeners for door open/close events.
     */
    private void registerEventListeners() {
        subscriptions.add(eventBus.subscribe(OpenDoorTopLeftEvent.class, e -> playLeftDoor(null)));
        subscriptions.add(eventBus.subscribe(CloseDoorTopLeftEvent.class, e -> playLeftReverseDoor(null)));
        subscriptions.add(eventBus.subscribe(OpenDoorTopRightEvent.class, e -> playRightDoor(null)));
        subscriptions.add(eventBus.subscribe(CloseDoorTopRightEvent.class, e -> playRightReverseDoor(null)));
    }

    /**
     * Plays the left door opening animation.
     *
     * @param onFinished callback to run after animation completes (can be null)
     */
    public void playLeftDoor(Runnable onFinished) {
        leftDoor.setVisible(true);
        leftReverseDoor.setVisible(false);
        rightReverseDoor.setVisible(false);
        rightDoor.setVisible(false);

        Timeline timeline = new Timeline();
        for (int i = 0; i < leftFrames.size(); i++) {
            final Image frame = leftFrames.get(i);
            timeline.getKeyFrames().add(
                new KeyFrame(Duration.millis(i * 160), e -> leftDoor.setImage(frame))
            );
        }
        timeline.setCycleCount(1);
        timeline.setOnFinished(e -> {
            if (onFinished != null) onFinished.run();
            eventBus.publish(new DoorOpenedEvent(true));
        });
        timeline.play();
    }

    /**
     * Plays the left door closing (reverse) animation.
     *
     * @param onFinished callback to run after animation completes (can be null)
     */
    public void playLeftReverseDoor(Runnable onFinished) {
        leftReverseDoor.setVisible(true);
        leftDoor.setVisible(false);
        rightDoor.setVisible(false);
        rightReverseDoor.setVisible(false);
        Timeline timeline = new Timeline();
        for (int i = 0; i < leftReverseFrames.size(); i++) {
            final Image frame = leftReverseFrames.get(i);
            timeline.getKeyFrames().add(
                new KeyFrame(Duration.millis(i * 160), e -> leftReverseDoor.setImage(frame))
            );
        }
        timeline.setCycleCount(1);
        timeline.setOnFinished(e -> {
            if (onFinished != null) onFinished.run();
        });
        timeline.play();
    }

    /**
     * Plays the right door opening animation.
     *
     * @param onFinished callback to run after animation completes (can be null)
     */
    public void playRightDoor(Runnable onFinished) {
        rightDoor.setVisible(true);
        rightReverseDoor.setVisible(false);
        leftReverseDoor.setVisible(false);
        leftDoor.setVisible(false);
        Timeline timeline = new Timeline();
        for (int i = 0; i < rightFrames.size(); i++) {
            final Image frame = rightFrames.get(i);
            timeline.getKeyFrames().add(
                new KeyFrame(Duration.millis(i * 160), e -> rightDoor.setImage(frame))
            );
        }
        timeline.setCycleCount(1);
        timeline.setOnFinished(e -> {
            if (onFinished != null) onFinished.run();
            eventBus.publish(new DoorOpenedEvent(false));
        });
        timeline.play();
    }

    /**
     * Plays the right door closing (reverse) animation.
     *
     * @param onFinished callback to run after animation completes (can be null)
     */
    public void playRightReverseDoor(Runnable onFinished) {
        rightReverseDoor.setVisible(true);
        rightDoor.setVisible(false);
        leftDoor.setVisible(false);
        leftReverseDoor.setVisible(false);
        Timeline timeline = new Timeline();
        for (int i = 0; i < rightReverseFrames.size(); i++) {
            final Image frame = rightReverseFrames.get(i);
            timeline.getKeyFrames().add(
                new KeyFrame(Duration.millis(i * 160), e -> rightReverseDoor.setImage(frame))
            );
        }
        timeline.setCycleCount(1);
        timeline.setOnFinished(e -> {
            if (onFinished != null) onFinished.run();
        });
        timeline.play();
    }

    /**
     * Rendering is not needed for doors as their positions are static.
     *
     * @param v ignored
     */
    @Override
    public void render(Void v) {
        // No continuous update needed
    }

    /**
     * Returns the parent pane containing the door ImageViews.
     *
     * @return the Pane node
     */
    @Override
    public Pane getNode() {
        return pane;
    }
}

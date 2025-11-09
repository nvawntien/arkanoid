package com.game.arkanoid.view.renderer;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

import com.game.arkanoid.events.GameEventBus;
import com.game.arkanoid.events.game.OpenDoorTopLeftEvent;
import com.game.arkanoid.events.game.OpenDoorTopRightEvent;

public final class DoorTopRenderer implements Renderer<Void> {
    private final Pane pane;

    private final ImageView leftDoor;
    private final ImageView rightDoor;

    private final List<Image> leftFrames = new ArrayList<>();
    private final List<Image> rightFrames = new ArrayList<>();
    private final GameEventBus eventBus = GameEventBus.getInstance();
    private final List<GameEventBus.Subscription> subscriptions = new ArrayList<>();


    public DoorTopRenderer(Pane pane, ImageView edge_top) {
        this.pane = pane;

        // Load frames: mỗi frame là trạng thái mở rộng của thanh
        for (int i = 1; i <= 7; i++) {
            leftFrames.add(new Image(getClass().getResourceAsStream(
                    String.format("/com/game/arkanoid/images/door_top_left_%d.png", i)
            )));
            rightFrames.add(new Image(getClass().getResourceAsStream(
                    String.format("/com/game/arkanoid/images/door_top_right_%d.png", i)
            )));
        }

        leftDoor = new ImageView(leftFrames.get(0));
        leftDoor.setLayoutX(edge_top.getLayoutX());
        leftDoor.setLayoutY(edge_top.getLayoutY());
        leftDoor.setFitWidth(edge_top.getFitWidth());
        leftDoor.setFitHeight(edge_top.getFitHeight());

        rightDoor = new ImageView(rightFrames.get(0));
        rightDoor.setLayoutX(edge_top.getLayoutX());
        rightDoor.setLayoutY(edge_top.getLayoutY());
        rightDoor.setFitWidth(edge_top.getFitWidth());
        rightDoor.setFitHeight(edge_top.getFitHeight());

        leftDoor.setSmooth(true);
        rightDoor.setSmooth(true);

        pane.getChildren().addAll(leftDoor, rightDoor);

        registerEventListeners();

    }

     private void registerEventListeners() {
        subscriptions.add(eventBus.subscribe(OpenDoorTopLeftEvent.class, e -> playLeftDoor()));
        subscriptions.add(eventBus.subscribe(OpenDoorTopRightEvent.class, e -> playRightDoor()));
    }

    // --- Animation ---
    public void playLeftDoor() {
        Timeline timeline = new Timeline();
        for (int i = 0; i < leftFrames.size(); i++) {
            Image frame = leftFrames.get(i);
            timeline.getKeyFrames().add(
                    new KeyFrame(Duration.millis(i * 80), e -> leftDoor.setImage(frame))
            );
        }
        timeline.setCycleCount(1); // chạy 1 lần
        timeline.play();
    }

    public void playRightDoor() {
        Timeline timeline = new Timeline();
        for (int i = 0; i < rightFrames.size(); i++) {
            Image frame = rightFrames.get(i);
            timeline.getKeyFrames().add(
                    new KeyFrame(Duration.millis(i * 80), e -> rightDoor.setImage(frame))
            );
        }
        timeline.setCycleCount(1); // chạy 1 lần
        timeline.play();
    }

    @Override
    public void render(Void v) {
        // Chỉ animate, không cần update vị trí liên tục
    }

    @Override
    public Pane getNode() {
        return pane;
    }
}

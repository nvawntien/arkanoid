package com.game.arkanoid.view;

import com.game.arkanoid.models.Ball;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

/**
 * Renders secondary balls spawned through the multi-ball power-up.
 */
public final class ExtraBallsRenderer {

    private final Pane pane;
    private final Map<Ball, ImageView> nodes = new IdentityHashMap<>();
    private final Image ballImage;

    public ExtraBallsRenderer(Pane pane) {
        this.pane = pane;
        this.ballImage = new Image(getClass().getResourceAsStream("/com/game/arkanoid/images/ball.png"));
    }

    public void render(List<Ball> balls) {
        // Xóa bóng không còn tồn tại
        Iterator<Map.Entry<Ball, ImageView>> it = nodes.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Ball, ImageView> entry = it.next();
            if (!balls.contains(entry.getKey())) {
                pane.getChildren().remove(entry.getValue());
                it.remove();
            }
        }

        // Cập nhật (hoặc tạo mới) vị trí bóng
        for (Ball ball : balls) {
            ImageView node = nodes.computeIfAbsent(ball, this::createNode);
            node.setTranslateX(ball.getX() - ball.getRadius());
            node.setTranslateY(ball.getY() - ball.getRadius());
        }
    }

    private ImageView createNode(Ball ball) {
        ImageView view = new ImageView(ballImage);
        double diameter = ball.getRadius() * 2;

        view.setFitWidth(diameter);
        view.setFitHeight(diameter);
        view.setSmooth(true);

        pane.getChildren().add(view);
        return view;
    }
}

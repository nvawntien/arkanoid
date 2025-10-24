package com.game.arkanoid.view;

import com.game.arkanoid.models.Brick;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.ImagePattern;
import javafx.scene.image.Image;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class BricksRenderer {
    private final Pane pane;
    private final Map<Brick, Rectangle> brickNodes = new HashMap<>();

    // Nạp sẵn hình ảnh để tái sử dụng, tránh load lại mỗi frame
    private final Image brick1Img = new Image(getClass().getResource("/com/game/arkanoid/images/brick_blue.png").toExternalForm());
    private final Image brick2Img = new Image(getClass().getResource("/com/game/arkanoid/images/brick_red.png").toExternalForm());
    private final Image brick3Img = new Image(getClass().getResource("/com/game/arkanoid/images/brick_yellow.png").toExternalForm());
    private final Image brick4Img = new Image(getClass().getResource("/com/game/arkanoid/images/brick_green.png").toExternalForm());

    public BricksRenderer(Pane pane, List<Brick> bricks) {
        this.pane = pane;

        for (Brick b : bricks) {
            Rectangle r = new Rectangle(b.getX(), b.getY(), b.getWidth(), b.getHeight());
            r.setArcWidth(6);
            r.setArcHeight(6);

            // Gán ảnh tương ứng theo máu
            r.setFill(getBrickImagePattern(b.getHealth()));

            // Vẫn giữ CSS để thêm viền hoặc hiệu ứng
            r.getStyleClass().add("brick-style");

            pane.getChildren().add(r);
            brickNodes.put(b, r);
        }
    }

    public void render(List<Brick> bricks) {
        for (Brick b : bricks) {
            Rectangle r = brickNodes.get(b);
            if (r == null) continue;

            if (b.isDestroyed()) {
                r.setVisible(false);
            } else {
                r.setVisible(true);
                // Cập nhật lại ảnh nếu health thay đổi
                r.setFill(getBrickImagePattern(b.getHealth()));
            }
        }
    }

    // Chọn ảnh tương ứng theo máu
    private ImagePattern getBrickImagePattern(int health) {
        return switch (health) {
            case 4 -> new ImagePattern(brick4Img);
            case 3 -> new ImagePattern(brick3Img);
            case 2 -> new ImagePattern(brick2Img);
            default -> new ImagePattern(brick1Img);
        };
    }
}

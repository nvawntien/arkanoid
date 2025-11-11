package com.game.arkanoid.view.renderer;

import com.game.arkanoid.models.Brick;
import java.util.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

public final class BricksRenderer implements Renderer<List<Brick>> {

    private final Pane pane;
    private final Map<Brick, ImageView> brickNodes = new HashMap<>();

    // Tải ảnh 1 lần duy nhất
    private final Image brick1Img = new Image(getClass().getResource("/com/game/arkanoid/images/brick_blue.png").toExternalForm());
    private final Image brick2Img = new Image(getClass().getResource("/com/game/arkanoid/images/brick_red.png").toExternalForm());
    private final Image brick3Img = new Image(getClass().getResource("/com/game/arkanoid/images/brick_yellow.png").toExternalForm());
    private final Image brick4Img = new Image(getClass().getResource("/com/game/arkanoid/images/brick_green.png").toExternalForm());
    private final Image brick5Img = new Image(getClass().getResource("/com/game/arkanoid/images/brick_cyan.png").toExternalForm());
    private final Image brick6Img = new Image(getClass().getResource("/com/game/arkanoid/images/brick_pink.png").toExternalForm());
    private final Image brick7Img = new Image(getClass().getResource("/com/game/arkanoid/images/brick_orange.png").toExternalForm());
    private final Image brick8Img = new Image(getClass().getResource("/com/game/arkanoid/images/brick_gold.png").toExternalForm());
    private final Image brick9Img = new Image(getClass().getResource("/com/game/arkanoid/images/brick_silver.png").toExternalForm());
    

    public BricksRenderer(Pane pane) {
        this.pane = pane;
    }

    @Override
    public void render(List<Brick> bricks) {
        // copy để tránh ConcurrentModificationException
        List<Brick> snapshot = List.copyOf(bricks);

        // Xóa brick không còn tồn tại (ví dụ: khi chuyển level)
        Iterator<Map.Entry<Brick, ImageView>> it = brickNodes.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Brick, ImageView> entry = it.next();
            if (!snapshot.contains(entry.getKey())) {
                pane.getChildren().remove(entry.getValue());
                it.remove();
            }
        }

        // Vẽ brick hiện tại
        for (Brick b : snapshot) {
            ImageView iv = brickNodes.get(b);
            if (iv == null) {
                // Tạo mới nếu chưa có trong map
                iv = new ImageView();
                iv.setPreserveRatio(false); // để không méo khi scale
                pane.getChildren().add(iv);
                brickNodes.put(b, iv);
            }

            // set ảnh theo health (ĐÃ SỬA LỖI Ở DÒNG NÀY)
            iv.setImage(getBrickImage(b)); 

            // set vị trí & kích thước
            iv.setX(b.getX());
            iv.setY(b.getY());
            iv.setFitWidth(b.getWidth());
            iv.setFitHeight(b.getHeight());

            // Ẩn gạch nếu đã bị phá hủy (nhưng chưa bị xóa khỏi list)
            iv.setVisible(!b.isDestroyed());
        }
    }
    @Override
    public ImageView getNode() {
        // Not applicable for list renderer 
        return null;
    }

    /**
     * Lấy ảnh tương ứng với loại gạch (Brick).
     * Hàm này nhận vào Brick (thay vì int) để kiểm tra isIndestructible()
     */
    private Image getBrickImage(Brick b) {
        // Ưu tiên kiểm tra gạch bạc (bất tử) trước
        // Dựa trên logic của class Brick (isIndestructible() là health == 9)
        if (b.isIndestructible()) {
            return brick9Img; // silver brick
        }

        // Nếu không phải gạch bạc, chọn ảnh theo số máu còn lại
        return switch (b.getHealth()) {
            case 8 -> brick8Img;
            case 7 -> brick7Img;
            case 6 -> brick6Img;
            case 5 -> brick5Img;
            case 4 -> brick4Img;
            case 3 -> brick3Img;
            case 2 -> brick2Img;
            // case 1 và case <= 0 (đã vỡ) sẽ dùng ảnh default
            default -> brick1Img; 
        };
    }

}
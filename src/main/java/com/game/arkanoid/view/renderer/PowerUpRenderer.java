package com.game.arkanoid.view.renderer;

import com.game.arkanoid.models.PowerUp;
import com.game.arkanoid.models.PowerUpType;
import javafx.animation.AnimationTimer;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Renders falling power-ups with animation (8-frame sprite per power-up).
 */
public final class PowerUpRenderer implements Renderer<List<PowerUp>> {
    private final Pane pane;
    private final Map<PowerUp, ImageView> nodes = new IdentityHashMap<>();

    // Mỗi loại powerup có 8 frame
    private static final int FRAME_COUNT = 8;
    private static final double FRAME_DURATION = 0.05; // giây / frame

    // Lưu trữ sprite sheet cho từng loại powerup
    private final Map<PowerUpType, Image[]> spriteMap = new IdentityHashMap<>();

    private double elapsedTime = 0;

    public PowerUpRenderer(Pane pane) {
        this.pane = pane;
        loadSprites();
        startAnimation();
    }

    private void loadSprites() {
        // Giả sử file sprite được chia sẵn thành 8 ảnh riêng biệt
        // (hoặc bạn có thể load từ sprite sheet)
        spriteMap.put(PowerUpType.EXPAND_PADDLE, loadFrames("expand"));
        spriteMap.put(PowerUpType.LASER_PADDLE, loadFrames("laser"));
        spriteMap.put(PowerUpType.MULTI_BALL, loadFrames("duplicate"));
        spriteMap.put(PowerUpType.EXTRA_LIFE, loadFrames("life"));
        spriteMap.put(PowerUpType.SLOW_BALL, loadFrames("slow"));
        spriteMap.put(PowerUpType.CATCH_BALL, loadFrames("catch"));
    }

    private Image[] loadFrames(String name) {
        Image[] frames = new Image[FRAME_COUNT];
        for (int i = 1; i <= FRAME_COUNT; i++) {
            frames[i-1] = new Image(
                getClass().getResource("/com/game/arkanoid/images/powerup_" + name + "_" + i + ".png").toExternalForm()
            );
        }
        return frames;
    }

    @Override
    public void render(List<PowerUp> powerUps) {
        Iterator<Map.Entry<PowerUp, ImageView>> it = nodes.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<PowerUp, ImageView> entry = it.next();
            if (!powerUps.contains(entry.getKey())) {
                pane.getChildren().remove(entry.getValue());
                it.remove();
            }
        }

        for (PowerUp powerUp : powerUps) {
            ImageView node = nodes.computeIfAbsent(powerUp, this::createNode);
            node.setX(powerUp.getX());
            node.setY(powerUp.getY());
        }
    }

    @Override
    public ImageView getNode() {
        // Not applicable for list renderer 
        return null;
    }
    
    private ImageView createNode(PowerUp powerUp) {
        ImageView imageView = new ImageView(spriteMap.get(powerUp.getType())[0]);
        imageView.setFitWidth(powerUp.getWidth());
        imageView.setFitHeight(powerUp.getHeight());
        pane.getChildren().add(imageView);
        return imageView;
    }

    private void startAnimation() {
        new AnimationTimer() {
            private long lastTime = 0;

            @Override
            public void handle(long now) {
                if (lastTime == 0) {
                    lastTime = now;
                    return;
                }

                double delta = (now - lastTime) / 1e9; // nano -> giây
                lastTime = now;
                elapsedTime += delta;

                int currentFrame = (int) ((elapsedTime / FRAME_DURATION) % FRAME_COUNT);

                for (Map.Entry<PowerUp, ImageView> entry : nodes.entrySet()) {
                    PowerUp p = entry.getKey();
                    ImageView view = entry.getValue();
                    Image[] frames = spriteMap.get(p.getType());
                    if (frames != null && frames.length > 0) {
                        view.setImage(frames[currentFrame]);
                    }
                }
            }
        }.start();
    }
}

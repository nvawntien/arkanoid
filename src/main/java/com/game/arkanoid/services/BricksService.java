package com.game.arkanoid.services;

import com.game.arkanoid.models.Brick;
import com.game.arkanoid.utils.Constants;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public final class BricksService {

    private int bricksRemaining;

    public BricksService() {
    }

    // --- Tạo từ layout 2D (ĐÃ SỬA) ---
    public List<Brick> createBricksFromLayout(int[][] layout) {
        List<Brick> createdBricks = new ArrayList<>();
        for (int row = 0; row < Constants.BRICK_ROWS; row++) {
            for (int col = 0; col < Constants.BRICK_COLS; col++) {
                
                int brickHealth = layout[row][col]; 
                if (brickHealth > 0) {
                    double brickX = col * Constants.BRICK_WIDTH + 22;
                    double brickY = row * Constants.BRICK_HEIGHT + 172;

                    createdBricks.add(new Brick( brickX,  brickY,  Constants.BRICK_WIDTH,  Constants.BRICK_HEIGHT, brickHealth ));
                }
            }
        }
        bricksRemaining = countAlive(createdBricks);
        return createdBricks;
    }

    // --- Tạo từ file resource (ĐÃ SỬA) ---
    public List<Brick> createBricksFromResource(String resourcePath) {
        List<String> lines = readResourceLines(resourcePath);
        List<Brick> bricks = new ArrayList<>();

        for (int row = 0; row < lines.size(); row++) {
            String line = lines.get(row).trim();
            String[] tokens = line.isEmpty() ? new String[0] : line.split("\\s+");
            int maxCols = Math.min(tokens.length, Constants.BRICK_COLS);

            for (int col = 0; col < maxCols; col++) {
                String token = tokens[col];
                if (token.isEmpty()) continue;

                int health; // Đây là health đọc từ file
                try {
                    health = Integer.parseInt(token);
                } catch (NumberFormatException ex) {
                    continue;
                }
                if (health <= 0) continue;

                double x = col * Constants.BRICK_WIDTH + 22;
                double y = row * Constants.BRICK_HEIGHT + 172;
                
                bricks.add(new Brick( x, y, Constants.BRICK_WIDTH, Constants.BRICK_HEIGHT, health ));
            }
        }

        bricksRemaining = countAlive(bricks);
        return bricks;
    }
    private int countAlive(List<Brick> bricks) {
        return (int) bricks.stream().filter(b -> !b.isDestroyed() && !b.isIndestructible()).count();
    }

    public boolean handleBrickHit(Brick brick) {
        if (brick.isDestroyed() || brick.isIndestructible()) return false;

        brick.setHealth(brick.getHealth() - 1);
        if (brick.isDestroyed()) {
            bricksRemaining = Math.max(0, bricksRemaining - 1);
            return true;
        }
        return false;
    }

    public boolean allBricksCleared(List<Brick> bricks) {
        // chỉ kiểm tra bricks thường
        return bricks.stream()
                     .filter(b -> !b.isIndestructible())
                     .allMatch(Brick::isDestroyed);
    }

    public int getBricksRemaining() {
        return bricksRemaining;
    }

    public void recalculateBricksRemaining(List<Brick> bricks) {
        bricksRemaining = countAlive(bricks);
    }

    private List<String> readResourceLines(String resourcePath) {
        List<String> out = new ArrayList<>();
        try (InputStream in = getClass().getResourceAsStream(resourcePath)) {
            if (in == null) throw new IllegalArgumentException("Missing resource: " + resourcePath);
            try (BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (!line.isBlank()) out.add(line);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed reading level: " + resourcePath, e);
        }
        return out;
    }
}

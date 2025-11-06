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

/**
 * Pure brick-related logic: reading layouts and applying damage.
 */
public final class BricksService {

    private int bricksRemaining;

    public BricksService() {
    }

    /**
     * Build bricks from a 2D layout array.
     */
    public List<Brick> createBricksFromLayout(int[][] layout) {
        List<Brick> createdBricks = new ArrayList<>();
        for (int row = 0; row < Constants.BRICK_ROWS; row++) {
            for (int col = 0; col < Constants.BRICK_COLS; col++) {
                int brickHealth = layout[row][col];
                if (brickHealth > 0) {
                    double brickX = col * Constants.BRICK_WIDTH + 22;
                    double brickY = row * Constants.BRICK_HEIGHT + 172;
                    createdBricks.add(new Brick(brickX, brickY, Constants.BRICK_WIDTH, Constants.BRICK_HEIGHT, brickHealth));
                }
            }
        }
        bricksRemaining = createdBricks.size();
        return createdBricks;
    }

    /**
     * Create bricks by reading a level layout from a resource text file.
     * Each line contains digits, where 0 = empty and 1..4 = brick durability.
     */
    public List<Brick> createBricksFromResource(String resourcePath) {
        List<String> lines = readResourceLines(resourcePath);
        List<Brick> bricks = new ArrayList<>();
        for (int row = 0; row < lines.size(); row++) {
            String line = lines.get(row).trim();
            for (int col = 0; col < line.length(); col++) {
                char ch = line.charAt(col);
                if (ch < '0' || ch > '9') {
                    continue;
                }
                int health = ch - '0';
                if (health <= 0) {
                    continue;
                }
                double x = col * Constants.BRICK_WIDTH + 22;
                double y = row * Constants.BRICK_HEIGHT + 172;
                bricks.add(new Brick(x, y, Constants.BRICK_WIDTH, Constants.BRICK_HEIGHT, Math.min(health, 4)));
            }
        }
        bricksRemaining = bricks.size();
        return bricks;
    }

    private List<String> readResourceLines(String resourcePath) {
        List<String> out = new ArrayList<>();
        try (InputStream in = getClass().getResourceAsStream(resourcePath)) {
            if (in == null) {
                throw new IllegalArgumentException("Missing resource: " + resourcePath);
            }
            try (BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (!line.isBlank()) {
                        out.add(line);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed reading level: " + resourcePath, e);
        }
        return out;
    }

    /**
     * Apply damage to a brick and return true if the brick was destroyed.
     */
    public boolean handleBrickHit(Brick brick) {
        if (brick.isDestroyed()) {
            return false;
        }
        brick.setHealth(brick.getHealth() - 1);
        if (brick.isDestroyed()) {
            bricksRemaining = Math.max(0, bricksRemaining - 1);
            return true;
        }
        return false;
    }

    public boolean allBricksCleared(List<Brick> bricks) {
        return bricks.stream().allMatch(Brick::isDestroyed);
    }

    public int getBricksRemaining() {
        return bricksRemaining;
    }

    public void recalculateBricksRemaining(List<Brick> bricks) {
        long alive = bricks.stream().filter(b -> !b.isDestroyed()).count();
        bricksRemaining = (int) alive;
    }
}

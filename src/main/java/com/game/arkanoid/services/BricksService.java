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
 * Service class responsible for creating, managing, and updating bricks in the game.
 * Handles creation from 2D layouts or resource files, brick hits, and remaining brick count.
 */
public final class BricksService {

    /** Number of non-indestructible bricks still alive */
    private int bricksRemaining;

    /** Default constructor */
    public BricksService() {
    }

    /**
     * Creates bricks from a 2D layout array.
     * Each non-zero value in the layout represents a brick's health.
     *
     * @param layout 2D array representing brick health values
     * @return list of created Brick objects
     */
    public List<Brick> createBricksFromLayout(int[][] layout) {
        List<Brick> createdBricks = new ArrayList<>();
        for (int row = 0; row < Constants.BRICK_ROWS; row++) {
            for (int col = 0; col < Constants.BRICK_COLS; col++) {
                
                int brickHealth = layout[row][col]; 
                if (brickHealth > 0) {
                    double brickX = col * Constants.BRICK_WIDTH + 22;
                    double brickY = row * Constants.BRICK_HEIGHT + 300;

                    createdBricks.add(new Brick( brickX,  brickY,  Constants.BRICK_WIDTH,  Constants.BRICK_HEIGHT, brickHealth ));
                }
            }
        }
        bricksRemaining = countAlive(createdBricks);
        return createdBricks;
    }

    /**
     * Creates bricks by reading a resource file.
     * Each non-zero integer in the file represents a brick's health.
     *
     * @param resourcePath path to the resource file
     * @return list of created Brick objects
     */
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

                int health;
                try {
                    health = Integer.parseInt(token);
                } catch (NumberFormatException ex) {
                    continue;
                }
                if (health <= 0) continue;

                double x = col * Constants.BRICK_WIDTH + 22;
                double y = row * Constants.BRICK_HEIGHT + 250;
                
                bricks.add(new Brick( x, y, Constants.BRICK_WIDTH, Constants.BRICK_HEIGHT, health ));
            }
        }

        bricksRemaining = countAlive(bricks);
        return bricks;
    }

    /**
     * Handles a hit on a brick.
     * Decreases brick health and updates remaining brick count if destroyed.
     *
     * @param brick the brick that was hit
     * @return true if the brick was destroyed by this hit
     */
    public boolean handleBrickHit(Brick brick) {
        if (brick.isDestroyed() || brick.isIndestructible()) return false;

        brick.setHealth(brick.getHealth() - 1);
        if (brick.isDestroyed()) {
            bricksRemaining = Math.max(0, bricksRemaining - 1);
            return true;
        }
        return false;
    }

    /**
     * Checks if all non-indestructible bricks have been cleared.
     *
     * @param bricks list of bricks to check
     * @return true if all destructible bricks are destroyed
     */
    public boolean allBricksCleared(List<Brick> bricks) {
        return bricks.stream()
                     .filter(b -> !b.isIndestructible())
                     .allMatch(Brick::isDestroyed);
    }

    /**
     * Returns the current number of remaining non-indestructible bricks.
     *
     * @return remaining bricks count
     */
    public int getBricksRemaining() {
        return bricksRemaining;
    }

    /**
     * Recalculates the number of remaining non-indestructible bricks.
     *
     * @param bricks list of bricks to count
     */
    public void recalculateBricksRemaining(List<Brick> bricks) {
        bricksRemaining = countAlive(bricks);
    }

    /**
     * Counts the number of alive, destructible bricks in a list.
     *
     * @param bricks list of bricks
     * @return count of alive bricks
     */
    private int countAlive(List<Brick> bricks) {
        return (int) bricks.stream().filter(b -> !b.isDestroyed() && !b.isIndestructible()).count();
    }

    /**
     * Reads all non-blank lines from a resource file.
     *
     * @param resourcePath path to the resource
     * @return list of non-blank lines
     */
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

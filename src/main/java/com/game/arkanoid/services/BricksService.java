package com.game.arkanoid.services;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.game.arkanoid.models.Brick;
import com.game.arkanoid.utils.Constants;

public class BricksService {

    public BricksService () {}
    /* 
    *tạo 1 mảng các brick được đọc vào qua mảng 2 chiều layout
    * có thể cho mỗi loại brick 1 màu ...
    * hơi bngu =) @tuan
    */ 
    public List<Brick> createBricksFromLayout(int[][] layout) {
        List<Brick> createdBricks = new ArrayList<>();
        
            for (int row = 0 ; row < Constants.BRICK_ROWS ; row ++) {
                for (int col = 0 ; col < Constants.BRICK_COLS; col++ ) {
                    int brickHealth = layout[row][col];
                    if (brickHealth > 0) {
                        double brickX = col * (Constants.BRICK_WIDTH)+22;
                        double brickY = row * (Constants.BRICK_HEIGHT) + 172;
                        createdBricks.add(new Brick(brickX, brickY, Constants.BRICK_WIDTH, Constants.BRICK_HEIGHT, brickHealth));
                    }
                }
            }
            return createdBricks ;
        
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
                if (ch < '0' || ch > '9') continue;
                int health = ch - '0';
                if (health <= 0) continue;
                double x = col * (Constants.BRICK_WIDTH) + 22;
                double y = row * (Constants.BRICK_HEIGHT) + 172;
                bricks.add(new Brick(x, y, Constants.BRICK_WIDTH, Constants.BRICK_HEIGHT, Math.min(health, 4)));
            }
        }
        return bricks;
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
    /*
    * hàm phá gạch
    */
    public boolean handleBrickHit(Brick brick) {
            if (brick.isDestroyed()) {
                return false;
            }
            brick.setHealth(brick.getHealth() - 1);
            if (brick.isDestroyed()) {
                return true;
            }
            return false;
    }
    /*
    * you win ...
    */
    public boolean AllBricksDestroyed(List<Brick> currentBricks) {
            return currentBricks.stream().allMatch(Brick::isDestroyed);
    }    
    
    /** More idiomatic alias. */
    public boolean allBricksCleared(List<Brick> bricks) {
        return AllBricksDestroyed(bricks);
    }
    
}

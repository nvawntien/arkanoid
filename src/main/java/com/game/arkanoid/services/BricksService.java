package com.game.arkanoid.services;
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
    
        for (int row = 0 ; row < layout.length ; row ++) {
            for (int col = 0 ; col < layout[0].length ; col++ ) {
                int brickHealth = layout[row][col];
                if (brickHealth > 0) {
                    double brickX = col * (Constants.BRICK_WIDTH);
                    double brickY = row * (Constants.BRICK_HEIGHT) + 50;
                    createdBricks.add(new Brick(brickX, brickY, Constants.BRICK_WIDTH, Constants.BRICK_HEIGHT, brickHealth));
                }
            }
        }
        return createdBricks ;
    
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
    
}

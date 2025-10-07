package com.game.arkanoid.models;

public class Brick extends GameObject {

    private int health; //số máu còn lại của brick;
    public Brick (double x , double y,double width, double height, int initialHealth) {

        super(x,y,width,height);
        this.health = initialHealth;

        // updateStyle();
    }
    //brick kh update theo tg mà theo từng event
    @Override
    public void update(double dt) {

    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public boolean isDestroyed() {
        return health <= 0;
    }
    
   
}

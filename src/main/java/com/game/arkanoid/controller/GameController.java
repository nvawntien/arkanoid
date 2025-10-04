package com.game.arkanoid.controller;

import com.game.arkanoid.models.Ball;
import com.game.arkanoid.models.Paddle;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Game-controller class.
 * Responsibilities:
 *      (i)   own game objects.
 *      (ii)  add them to the FXML Pane and position them.
 *      (iii) handle user input.
 *      (iv)  keep correct layouts when resizing window.
 */
public class GameController implements Initializable {

    @FXML private Pane playfield;   // <Pane fx:id="playfield"> 

    private Paddle paddle;
    private Ball ball;
    private boolean ballDocked = true; // prelaunch state: ball on paddle

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        paddle = new Paddle(0, 0);     
        ball   = new Ball(0, 0);

        // Add nodes to the scene graph
        playfield.getChildren().addAll(paddle.getNode(), ball.getNode());

        // Layout once the Pane has real size
        Platform.runLater(() -> {
            layoutInitial();
            playfield.requestFocus(); // keyboard input (optional)
        });

        // Keep layout on resize
        playfield.widthProperty().addListener((o, ov, nv) -> layoutOnResize());
        playfield.heightProperty().addListener((o, ov, nv) -> layoutOnResize());

        // Optional: simple left/right controls to see things move
        playfield.setFocusTraversable(true);
        playfield.setOnKeyPressed(this::handleKeyPressed);
    }

    private void layoutInitial() {
        double w = playfield.getWidth()  > 0 ? playfield.getWidth()  : playfield.getPrefWidth();
        double h = playfield.getHeight() > 0 ? playfield.getHeight() : playfield.getPrefHeight();

        // Paddle bottom-center with 20px bottom margin
        double px = (w - paddle.getWidth()) / 2.0;
        double py = h - paddle.getHeight() - 20;
        paddle.setPosition(px, py);

        dockBallToPaddle();
    }

    private void layoutOnResize() {
        double py = playfield.getHeight() - paddle.getHeight() - 20;
        paddle.setY(py);

        
        if (ballDocked) dockBallToPaddle();
    }

    private void dockBallToPaddle() {
        double cx = paddle.getX() + paddle.getWidth() / 2.0;
        double cy = paddle.getY() - ball.getRadius();
        ball.setCenter(cx, cy);
    }

    
    private void handleKeyPressed(KeyEvent e) {
        switch (e.getCode()) {
            case LEFT  -> movePaddle(-12);
            case RIGHT -> movePaddle(12);
            case SPACE -> ballDocked = false;           
            case R     -> { ballDocked = true; dockBallToPaddle(); } 
        }
    }

    private void movePaddle(double dx) {
        double minX = 0;
        double maxX = playfield.getWidth() - paddle.getWidth();
        double newX = Math.max(minX, Math.min(paddle.getX() + dx, maxX));
        paddle.setX(newX);
        if (ballDocked) dockBallToPaddle();
    }
}

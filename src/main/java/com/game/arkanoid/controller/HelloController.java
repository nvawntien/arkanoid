package com.game.arkanoid.controller;

import com.game.arkanoid.services.HelloService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class HelloController {

    @FXML
    private TextField nameField;

    @FXML
    private Label welcomeLabel;

    private HelloService helloService = new HelloService();

    @FXML
    private void handleOk() {
        String name = nameField.getText();
        String message = helloService.buildWelcomeMessage(name);
        welcomeLabel.setText(message);
    }
}

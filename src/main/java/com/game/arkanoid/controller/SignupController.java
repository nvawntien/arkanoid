package com.game.arkanoid.controller;

import com.game.arkanoid.container.AppContext;
import com.game.arkanoid.services.DatabaseService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.util.Objects;

public final class SignupController {

    private final SceneController navigator;

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button createButton;
    @FXML private Label messageLabel;
    @FXML private Hyperlink backToLoginLink;

    public SignupController(SceneController navigator) {
        this.navigator = Objects.requireNonNull(navigator);
    }

    @FXML
    private void initialize() {
        messageLabel.setText("");
        createButton.setOnAction(e -> onCreate());
        if (backToLoginLink != null) {
            backToLoginLink.setOnAction(e -> navigator.showLogin());
        }
    }

    private void onCreate() {
        String username = usernameField.getText() == null ? "" : usernameField.getText().trim();
        String password = passwordField.getText() == null ? "" : passwordField.getText();
        if (username.isEmpty() || password.isEmpty()) {
            showMessage("Please enter username and password.");
            return;
        }
        setBusy(true);
        AppContext.getInstance().db().signup(username, password)
                .whenComplete((user, err) -> Platform.runLater(() -> {
                    setBusy(false);
                    if (err != null) {
                        Throwable cause = err.getCause() != null ? err.getCause() : err;
                        if (cause instanceof DatabaseService.NameExistsException) {
                            showMessage("Name already exists");
                        } else {
                            showMessage("Signup failed: " + cause.getClass().getSimpleName());
                            cause.printStackTrace();
                        }
                        return;
                    }
                    // Go back to login after successful signup
                    // add a successful signup message, you will be moved to login screen and a countdown, use a for loop to count down from 3 to 1
                    new Thread(() -> {
                        try {
                            for (int i = 3; i >= 1; i--) {
                                int count = i;
                                Platform.runLater(() -> showMessage("Signup successful! Redirecting in " + count + "..."));
                                Thread.sleep(1000);
                            }
                        } catch (InterruptedException ignored) {}
                        Platform.runLater(() -> navigator.showLogin());
                    }).start();
                }));
    }

    private void setBusy(boolean busy) {
        createButton.setDisable(busy);
        usernameField.setDisable(busy);
        passwordField.setDisable(busy);
        if (backToLoginLink != null) backToLoginLink.setDisable(busy);
    }

    private void showMessage(String text) {
        messageLabel.setText(text);
    }
}


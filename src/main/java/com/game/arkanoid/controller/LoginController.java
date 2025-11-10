package com.game.arkanoid.controller;

import com.game.arkanoid.container.AppContext;
import com.game.arkanoid.services.DatabaseService.InvalidCredentialsException;
import com.game.arkanoid.services.DatabaseService.UserNotFoundException;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.util.Objects;

public final class LoginController {

    private final SceneController navigator;

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Label messageLabel;
    @FXML private Hyperlink createAccountLink;

    public LoginController(SceneController navigator) {
        this.navigator = Objects.requireNonNull(navigator);
    }

    @FXML
    private void initialize() {
        messageLabel.setText("");
        loginButton.setOnAction(e -> onLogin());
        if (createAccountLink != null) createAccountLink.setOnAction(e -> navigator.showSignup());
    }

    private void onLogin() {
        String username = usernameField.getText() == null ? "" : usernameField.getText().trim();
        String password = passwordField.getText() == null ? "" : passwordField.getText();
        if (username.isEmpty() || password.isEmpty()) {
            showMessage("Please enter username and password.");
            return;
        }
        setBusy(true);
        // Normalize username for consistent account (matches hasher/db service)
        String uname = com.game.arkanoid.utils.PasswordHasher.normalize(username);
        AppContext.getInstance().db().login(uname, password)
                .whenComplete((user, err) -> Platform.runLater(() -> {
                    try {
                        setBusy(false);
                        if (err != null) {
                            Throwable cause = (err.getCause() != null) ? err.getCause() : err;
                            if (cause instanceof InvalidCredentialsException) {
                                showMessage("Incorrect username or password! Please try again.");
                            } else if (cause instanceof UserNotFoundException) {
                                showMessage("User not found. Please create an account.");
                            } else {
                                showMessage("Login failed: " + cause.getClass().getSimpleName());
                                System.err.println("Login error: ");
                                cause.printStackTrace();
                            }
                            return;
                        }
                        AppContext.getInstance().setCurrentUser(user);
                        showMessage("Login successful. Loading menu...");
                        navigator.showMenu();
                    } catch (Exception ex) {
                        showMessage("Unexpected error: " + ex.getClass().getSimpleName());
                        ex.printStackTrace();
                    }
                }));
    }

    private void setBusy(boolean busy) {
        loginButton.setDisable(busy);
        usernameField.setDisable(busy);
        passwordField.setDisable(busy);
    }

    private void showMessage(String text) {
        messageLabel.setText(text);
    }
}

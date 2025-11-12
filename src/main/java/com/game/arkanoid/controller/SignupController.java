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

/**
 * Controller responsible for handling user signup actions.
 * <p>
 * This class manages the signup UI, validates input, interacts with the
 * {@link DatabaseService} to create new user accounts, and navigates back
 * to the login screen upon successful registration.
 * </p>
 */
public final class SignupController {

    /** Controller that manages scene navigation (e.g., to login screen). */
    private final SceneController navigator;

    /** Text field for entering a new username. */
    @FXML private TextField usernameField;

    /** Password field for entering a new password. */
    @FXML private PasswordField passwordField;

    /** Button to trigger account creation. */
    @FXML private Button createButton;

    /** Label used to display status or error messages. */
    @FXML private Label messageLabel;

    /** Hyperlink that navigates back to the login screen. */
    @FXML private Hyperlink backToLoginLink;

    /**
     * Constructs the controller with a reference to the scene navigator.
     *
     * @param navigator the {@link SceneController} used for navigation
     */
    public SignupController(SceneController navigator) {
        this.navigator = Objects.requireNonNull(navigator);
    }

    /**
     * Initializes the signup view.
     * <p>
     * Binds UI actions to their corresponding handlers and clears messages.
     * </p>
     */
    @FXML
    private void initialize() {
        messageLabel.setText("");
        createButton.setOnAction(e -> onCreate());
        if (backToLoginLink != null) {
            backToLoginLink.setOnAction(e -> navigator.showLogin());
        }
    }

    /**
     * Handles the signup button click event.
     * <p>
     * Validates input fields, calls the database signup method, and provides feedback.
     * If signup succeeds, a countdown message is shown before redirecting to the login screen.
     * </p>
     */
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

                    // Success: show countdown and redirect to login
                    new Thread(() -> {
                        try {
                            for (int i = 3; i >= 1; i--) {
                                int count = i;
                                Platform.runLater(() ->
                                        showMessage("Signup successful! Redirecting in " + count + "..."));
                                Thread.sleep(1000);
                            }
                        } catch (InterruptedException ignored) {}
                        Platform.runLater(() -> navigator.showLogin());
                    }).start();
                }));
    }

    /**
     * Enables or disables all input fields and buttons.
     *
     * @param busy true to disable UI during async operation, false to enable it again
     */
    private void setBusy(boolean busy) {
        createButton.setDisable(busy);
        usernameField.setDisable(busy);
        passwordField.setDisable(busy);
        if (backToLoginLink != null) backToLoginLink.setDisable(busy);
    }

    /**
     * Displays a message in the message label.
     *
     * @param text the text to display
     */
    private void showMessage(String text) {
        messageLabel.setText(text);
    }
}

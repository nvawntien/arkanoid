package com.game.arkanoid.controller;

import com.game.arkanoid.container.AppContext;
import com.game.arkanoid.services.DatabaseService.InvalidCredentialsException;
import com.game.arkanoid.services.DatabaseService.UserNotFoundException;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.util.Objects;

/**
 * Controller class for the Login scene.
 * <p>
 * Handles user login logic, input validation, password visibility toggle,
 * and navigation to other scenes (e.g., signup or menu).
 * <p>
 * Responsibilities:
 * <ul>
 *     <li>Bind UI fields and buttons from FXML.</li>
 *     <li>Validate user input before attempting login.</li>
 *     <li>Authenticate user asynchronously via {@link com.game.arkanoid.services.DatabaseService}.</li>
 *     <li>Provide feedback messages for success or error states.</li>
 *     <li>Navigate to main menu or signup scene on successful login or user request.</li>
 * </ul>
 */
public final class LoginController {

    /** Scene navigation controller used for switching between views */
    private final SceneController navigator;

    // --- FXML UI elements ---
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordVisibleField;   // Plain text field for visible password
    @FXML private CheckBox showPasswordCheckBox;    // Checkbox to toggle password visibility
    @FXML private Button loginButton;
    @FXML private Label messageLabel;
    @FXML private Hyperlink createAccountLink;

    /**
     * Constructor.
     *
     * @param navigator the scene controller used for switching between views
     */
    public LoginController(SceneController navigator) {
        this.navigator = Objects.requireNonNull(navigator);
    }

    /**
     * Initializes the controller after the FXML has been loaded.
     * <p>
     * Sets up button handlers, field bindings, and password visibility logic.
     */
    @FXML
    private void initialize() {
        messageLabel.setText("");
        loginButton.setOnAction(e -> onLogin());

        if (createAccountLink != null) {
            createAccountLink.setOnAction(e -> navigator.showSignup());
        }

        // Bind text between visible and hidden password fields
        if (passwordVisibleField != null && passwordField != null) {
            passwordVisibleField.textProperty().bindBidirectional(passwordField.textProperty());
        }

        // Handle checkbox toggle for showing/hiding password
        if (showPasswordCheckBox != null) {
            showPasswordCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
                boolean show = newVal;
                passwordVisibleField.setVisible(show);
                passwordVisibleField.setManaged(show);
                passwordField.setVisible(!show);
                passwordField.setManaged(!show);
            });
        }
    }

    /**
     * Handles the login process:
     * <ul>
     *   <li>Validates user input.</li>
     *   <li>Authenticates via {@link com.game.arkanoid.services.DatabaseService}.</li>
     *   <li>Shows success or error messages.</li>
     *   <li>Redirects to the main menu on success.</li>
     * </ul>
     */
    private void onLogin() {
        String username = usernameField.getText() == null ? "" : usernameField.getText().trim();

        // Get the password from whichever field is visible
        String password;
        if (showPasswordCheckBox != null && showPasswordCheckBox.isSelected()) {
            password = passwordVisibleField.getText() == null ? "" : passwordVisibleField.getText();
        } else {
            password = passwordField.getText() == null ? "" : passwordField.getText();
        }

        if (username.isEmpty() || password.isEmpty()) {
            showMessage("Please enter username and password.");
            return;
        }

        setBusy(true);

        // Normalize username for consistent hashing and lookup
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

                        // Successful login
                        AppContext.getInstance().setCurrentUser(user);
                        showMessage("Login successful. Loading menu...");
                        navigator.showMenu();

                    } catch (Exception ex) {
                        showMessage("Unexpected error: " + ex.getClass().getSimpleName());
                        ex.printStackTrace();
                    }
                }));
    }

    /**
     * Enables or disables form controls to prevent interaction during login.
     *
     * @param busy true to disable inputs, false to enable them
     */
    private void setBusy(boolean busy) {
        loginButton.setDisable(busy);
        usernameField.setDisable(busy);
        passwordField.setDisable(busy);
        passwordVisibleField.setDisable(busy);
        showPasswordCheckBox.setDisable(busy);
    }

    /**
     * Displays a message to the user in the {@link #messageLabel}.
     *
     * @param text the message text
     */
    private void showMessage(String text) {
        messageLabel.setText(text);
    }
}

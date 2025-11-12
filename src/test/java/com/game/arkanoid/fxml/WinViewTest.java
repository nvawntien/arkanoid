package com.game.arkanoid.fxml;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

/**
 * ✅ JUnit + TestFX test that opens WinView.fxml on a real JavaFX window.
 * Run with: mvn test -Dtest=WinViewTest
 */
public class WinViewTest extends ApplicationTest {

    @Override
    public void start(Stage stage) throws Exception {
        // 👉 Load your FXML file
        Parent root = FXMLLoader.load(
            getClass().getResource("/com/game/arkanoid/fxml/WinView.fxml")
        );

        // 👉 Create and show scene
        Scene scene = new Scene(root, 800, 600);
        stage.setScene(scene);
        stage.setTitle("Test - WinView");
        stage.show();
    }

    @Test
    public void shouldDisplayWinView() {
        System.out.println("🎮 WinView.fxml displayed successfully!");
        // You can add asserts here later, e.g.:
        // verifyThat("#winLabel", hasText("YOU WIN!"));
    }
}

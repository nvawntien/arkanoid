package com.game.arkanoid.app;

<<<<<<< HEAD
import com.game.arkanoid.container.Container;
import com.game.arkanoid.controller.*;
=======
import com.game.arkanoid.view.SceneNavigator;
>>>>>>> a9eee5a2bca1284950d0d4fcf12faa800d6661e0
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
<<<<<<< HEAD
    public void start(Stage stage) throws Exception {

        Container container = new Container();

        // 2. Load FXML và inject controller
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/game/arkanoid/fxml/MenuView.fxml"));
        loader.setControllerFactory(cls -> {
        if (cls == MenuController.class) {
            return new MenuController( container.getMenuService());   // inject Container
        }
        try { return cls.getDeclaredConstructor().newInstance(); }
        catch (Exception e) { throw new RuntimeException(e); }
    });

        Parent root = loader.load();
        Scene scene = new Scene(root);
        stage.setScene(scene);
=======
    public void start(Stage stage) {
        stage.setTitle("Arkanoid");
        stage.setResizable(false);
        SceneNavigator navigator = new SceneNavigator(stage);
        navigator.showMenu();
>>>>>>> a9eee5a2bca1284950d0d4fcf12faa800d6661e0
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
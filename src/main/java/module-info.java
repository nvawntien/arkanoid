module com.game.arkanoid {
    // JavaFX core
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires javafx.swing;

    // Standard Java modules
    requires java.sql;
    requires java.desktop;

    // Third-party
    requires org.postgresql.jdbc;
    requires com.almasb.fxgl.all;
    requires java.dotenv;

    // Exports for JavaFX
    exports com.game.arkanoid.app;
    opens com.game.arkanoid.app to javafx.fxml;

    exports com.game.arkanoid.controller;
    opens com.game.arkanoid.controller to javafx.fxml;

    exports com.game.arkanoid.config;
    opens com.game.arkanoid.config to javafx.fxml;

    exports com.game.arkanoid.services;
    opens com.game.arkanoid.services to javafx.fxml;

    exports com.game.arkanoid.models;
    opens com.game.arkanoid.models to javafx.base;

    // ✅ Allow TestFX and FXML loading from test packages
    opens com.game.arkanoid.fxml to javafx.fxml, org.testfx, javafx.graphics;
}
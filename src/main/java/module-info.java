module com.game.arkanoid {
    // --- JavaFX core modules ---
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires javafx.web;
    requires javafx.swing;

    // --- Standard Java modules ---
    requires java.sql;
    requires java.desktop;

    // --- Third-party UI / FXGL frameworks ---
    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires org.postgresql.jdbc;

    // --- External libraries (auto modules) ---
    // Dotenv auto module
    requires java.dotenv;

    // --- Exports ---
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
}

module com.game.arkanoid {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;

    exports com.game.arkanoid.app;
    opens com.game.arkanoid.app to javafx.fxml;
    exports com.game.arkanoid.controller;
    opens com.game.arkanoid.controller to javafx.fxml;
}
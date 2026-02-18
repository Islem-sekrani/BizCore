module com.example.front {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;

    opens com.example.front to javafx.fxml;
    opens com.example.front.controllers to javafx.fxml;
    opens com.example.front.models to javafx.base;

    exports com.example.front;
    exports com.example.front.controllers;
    exports com.example.front.models;
}
module com.gestion {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.sql;
    requires itextpdf;
    requires java.desktop;

    opens com.gestion to javafx.fxml;
    opens com.gestion.controllers to javafx.fxml;
    opens com.gestion.entities to javafx.base;

    exports com.gestion;
    exports com.gestion.entities;
    exports com.gestion.services;
    exports com.gestion.interfaces;
    exports com.gestion.tools;
}

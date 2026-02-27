module com.gestion {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.sql;
    requires java.desktop;
    requires itextpdf;
    requires com.google.protobuf;


    opens com.gestion to javafx.fxml;
    opens com.gestion.controllers to javafx.fxml;
    opens com.gestion.models to javafx.base;
    
    exports com.gestion;
    exports com.gestion.controllers;
    exports com.gestion.models;
}

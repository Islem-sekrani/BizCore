module com.gestion {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    opens com.gestion to javafx.fxml;
    opens com.gestion.controllers to javafx.fxml;
    opens com.gestion.models to javafx.base;
    
    exports com.gestion;
    exports com.gestion.controllers;
    exports com.gestion.models;}

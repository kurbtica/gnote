module org.openjfx.sio2E4 {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
	requires com.fasterxml.jackson.databind;
	requires com.fasterxml.jackson.core;

	requires java.net.http;
	requires com.fasterxml.jackson.annotation;
	requires javafx.graphics;


    // Exportez le package contenant votre contrôleur
    exports org.openjfx.sio2E4.controller;

    // Ouvrez le package du contrôleur pour permettre l'accès depuis javafx.fxml
    opens org.openjfx.sio2E4.controller;

    opens org.openjfx.sio2E4; // Si nécessaire pour l'injection de dépendance avec JavaFX
    exports org.openjfx.sio2E4;
    
    exports org.openjfx.sio2E4.model to com.fasterxml.jackson.databind;


}


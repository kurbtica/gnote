module org.openjfx.sio2E4 {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.core;

    requires java.net.http;
    requires com.fasterxml.jackson.annotation;
    requires javafx.graphics;
    requires java.desktop;
    requires org.yaml.snakeyaml;
    opens org.openjfx.sio2E4 to javafx.fxml, org.yaml.snakeyaml;
    exports org.openjfx.sio2E4.service;
    // Exportez le package contenant votre contrôleur
    exports org.openjfx.sio2E4.controller;
    exports org.openjfx.sio2E4.controller.auth;
    exports org.openjfx.sio2E4.controller.user;
    exports org.openjfx.sio2E4.controller.user.cards;
    exports org.openjfx.sio2E4.controller.evaluation;
    exports org.openjfx.sio2E4.util;

    // Ouvrez le package du contrôleur pour permettre l'accès depuis javafx.fxml
    opens org.openjfx.sio2E4.controller to javafx.fxml;
    opens org.openjfx.sio2E4.controller.auth to javafx.fxml;
    opens org.openjfx.sio2E4.controller.user to javafx.fxml;
    opens org.openjfx.sio2E4.controller.user.cards to javafx.fxml;
    opens org.openjfx.sio2E4.controller.evaluation to javafx.fxml;

    // Si nécessaire pour l'injection de dépendance avec JavaFX
    exports org.openjfx.sio2E4;

    exports org.openjfx.sio2E4.model to com.fasterxml.jackson.databind;
    exports org.openjfx.sio2E4.constants;
    opens org.openjfx.sio2E4.constants;


}
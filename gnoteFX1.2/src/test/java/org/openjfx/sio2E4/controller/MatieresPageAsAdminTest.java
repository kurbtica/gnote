package org.openjfx.sio2E4.controller;

import javafx.application.Platform;
import javafx.scene.control.TableView;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openjfx.sio2E4.BaseUiAsAdminTest;
import org.openjfx.sio2E4.model.Matiere;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.util.WaitForAsyncUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(ApplicationExtension.class)
public class MatieresPageAsAdminTest extends BaseUiAsAdminTest {

    //@Test
    public void addAndRemoveMatieresTest(FxRobot robot) {
        // given:
        robot.clickOn("Matières");
        String nomMatiere = "Test ajout de matière";

        // 1) Ajouter d'une matière
        robot.clickOn("#libelleField").write(nomMatiere);
        robot.clickOn("Ajouter");
        robot.clickOn("OK");

        // when/then:
        // Attendre que la nouvelle vue se charge
        sleep(1000);
        WaitForAsyncUtils.waitForFxEvents();

        // 2) Récupérer la table et vérifier que la matière est bien ajoutée
        @SuppressWarnings("unchecked")
        TableView<Matiere> addedTable = robot.lookup("#matieresTable").queryAs(TableView.class);

        Optional<Matiere> added = addedTable.getItems().stream()
                .filter(m -> m.getLibelle().equals(nomMatiere))
                .findFirst();

        assertTrue(added.isPresent(), "La matière ajoutée doit apparaître dans le tableau");

        // 3) Sélectionner la matière dans la table
        Platform.runLater(() -> addedTable.getSelectionModel().select(added.get()));
        sleep(1000);
        WaitForAsyncUtils.waitForFxEvents();

        // 4) Cliquer sur le bouton "Supprimer"
        robot.clickOn("Supprimer"); // ou robot.clickOn("#deleteButton") si tu ajoutes un fx:id
        robot.clickOn("OK");
        sleep(1000);
        WaitForAsyncUtils.waitForFxEvents();

        // 5) Vérifier que la matière n'est plus dans la table
        // On récupère la table misse à jour
        @SuppressWarnings("unchecked")
        TableView<Matiere> removeTable = robot.lookup("#matieresTable").queryAs(TableView.class);
        boolean stillPresent = removeTable.getItems().stream()
                .anyMatch(m -> m.getLibelle().equals(nomMatiere));

        assertFalse(stillPresent, "La matière doit être supprimée du tableau après clic sur 'Supprimer'");

    }
}

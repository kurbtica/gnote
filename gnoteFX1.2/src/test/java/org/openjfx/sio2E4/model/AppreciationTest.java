package org.openjfx.sio2E4.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AppreciationTest {

    @Test
    void testConstructorAndGetters() {
        User user = new User();
        user.setId(1);
        user.setNom("Test");

        Matiere matiere = new Matiere();
        matiere.setId(1);
        matiere.setLibelle("Math");

        Appreciation app = new Appreciation(10, user, matiere, "Bon trimestre");

        assertEquals(10, app.getId());
        assertEquals(user, app.getEleve());
        assertEquals(matiere, app.getMatiere());
        assertEquals("Bon trimestre", app.getAppreciation());
    }

    @Test
    void testSetters() {
        Appreciation app = new Appreciation();
        app.setId(15);
        
        User user = new User();
        app.setEleve(user);
        
        Matiere matiere = new Matiere();
        app.setMatiere(matiere);
        
        app.setAppreciation("Attention aux bavardages");

        assertEquals(15, app.getId());
        assertEquals(user, app.getEleve());
        assertEquals(matiere, app.getMatiere());
        assertEquals("Attention aux bavardages", app.getAppreciation());
    }
}

package org.openjfx.sio2E4.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MatiereTest {

    @Test
    void testGettersAndSetters() {
        Matiere matiere = new Matiere();
        matiere.setId(1);
        matiere.setLibelle("Mathématiques");

        assertEquals(1, matiere.getId());
        assertEquals("Mathématiques", matiere.getLibelle());
    }

    @Test
    void testToString() {
        Matiere matiere = new Matiere();
        matiere.setLibelle("Informatique");
        
        assertEquals("Informatique", matiere.toString());
    }
}

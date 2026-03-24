package org.openjfx.sio2E4.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class NoteTypeTest {

    @Test
    void testGettersAndSetters() {
        NoteType type = new NoteType();
        type.setId(3);
        type.setLibelle("TP");

        assertEquals(3, type.getId());
        assertEquals("TP", type.getLibelle());
    }

    @Test
    void testToString() {
        NoteType type = new NoteType();
        type.setLibelle("Exposé");
        
        assertEquals("Exposé", type.toString());
    }
}

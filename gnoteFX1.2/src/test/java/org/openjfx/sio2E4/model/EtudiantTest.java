package org.openjfx.sio2E4.model;

import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class EtudiantTest {

    @Test
    void testGettersAndSetters() {
        Etudiant etudiant = new Etudiant();
        etudiant.setId(1);
        etudiant.setNom("Dupont");
        etudiant.setPrenom("Jean");
        etudiant.setEmail("jean.dupont@test.com");
        etudiant.setAdresse("1 rue de la Paix");
        etudiant.setTelephone("0102030405");

        Etudiant.Role role = new Etudiant.Role();
        role.setId(2);
        role.setLibelle("ETUDIANT");
        etudiant.setRole(role);

        assertEquals(1, etudiant.getId());
        assertEquals("Dupont", etudiant.getNom());
        assertEquals("Jean", etudiant.getPrenom());
        assertEquals("jean.dupont@test.com", etudiant.getEmail());
        assertEquals("1 rue de la Paix", etudiant.getAdresse());
        assertEquals("0102030405", etudiant.getTelephone());
        
        assertNotNull(etudiant.getRole());
        assertEquals(2, etudiant.getRole().getId());
        assertEquals("ETUDIANT", etudiant.getRole().getLibelle());
    }

    @Test
    void testAppreciations() {
        Etudiant etudiant = new Etudiant();
        Map<String, String> appreciations = new HashMap<>();
        appreciations.put("Math", "Très bien");
        
        etudiant.setAppreciations(appreciations);
        
        assertNotNull(etudiant.getAppreciations());
        assertEquals("Très bien", etudiant.getAppreciations().get("Math"));
        
        // Test null safety as implemented in setAppreciations
        etudiant.setAppreciations(null);
        assertNotNull(etudiant.getAppreciations());
        assertTrue(etudiant.getAppreciations().isEmpty());
    }
}

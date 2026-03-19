package org.openjfx.sio2E4.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour les modèles Evaluation et Note.
 *
 * Ces tests vérifient la construction et la manipulation des évaluations
 * et des notes, qui sont au cœur de l'application Gnotes.
 *
 * On teste ici notamment des calculs "métier" :
 * - Calcul de la moyenne des notes d'une évaluation
 * - Valeurs limites (notes à 0, à 20, liste vide)
 */
@DisplayName("Tests des modèles — Evaluation & Note")
class EvaluationNoteTest {

    private Evaluation evaluation;
    private User enseignant;
    private User eleve1;
    private User eleve2;
    private User eleve3;

    @BeforeEach
    void setUp() {
        // Création de l'enseignant
        enseignant = new User("Moreau", "Pierre", "p.moreau@lycee.fr",
                Role.ENSEIGNANT, "", "0612345678");

        // Création de 3 élèves pour les tests
        eleve1 = new User("Alice", "A", "alice@lycee.fr", Role.ETUDIANT, "", "");
        eleve2 = new User("Bob",   "B", "bob@lycee.fr",   Role.ETUDIANT, "", "");
        eleve3 = new User("Carol", "C", "carol@lycee.fr", Role.ETUDIANT, "", "");

        // Création d'une évaluation de base
        evaluation = new Evaluation();
        evaluation.setTitre("DS Mathématiques n°1");
        evaluation.setCoefficient(2.0);
        evaluation.setDate("2026-03-18");
        evaluation.setEnseignant(enseignant);
    }

    // -------------------------------------------------------
    //  Tests de construction de l'Evaluation
    // -------------------------------------------------------

    @Test
    @DisplayName("Une évaluation doit correctement stocker son titre et son coefficient")
    void testEvaluation_construction() {
        assertEquals("DS Mathématiques n°1", evaluation.getTitre());
        assertEquals(2.0,                    evaluation.getCoefficient(), 0.001);
        assertEquals("2026-03-18",           evaluation.getDate());
    }

    @Test
    @DisplayName("L'enseignant d'une évaluation doit être correctement référencé")
    void testEvaluation_enseignantCorrect() {
        assertNotNull(evaluation.getEnseignant(),
            "L'évaluation doit avoir un enseignant");
        assertEquals("Moreau", evaluation.getEnseignant().getNom(),
            "Le nom de l'enseignant doit correspondre");
    }

    // -------------------------------------------------------
    //  Tests de construction d'une Note
    // -------------------------------------------------------

    @Test
    @DisplayName("Une note doit stocker correctement sa valeur et son élève")
    void testNote_construction() {
        Note note = new Note();
        note.setValeur(15.5);
        note.setEleve(eleve1);
        note.setEvaluation(evaluation);

        assertEquals(15.5,   note.getValeur(), 0.001);
        assertEquals(eleve1, note.getEleve());
        assertEquals(evaluation, note.getEvaluation());
    }

    @Test
    @DisplayName("Une note à 0 doit être acceptée (absence ou zéro mérité)")
    void testNote_valeurZero() {
        Note note = new Note();
        note.setValeur(0.0);

        assertEquals(0.0, note.getValeur(), 0.001,
            "Une note à 0 est valide et doit être stockée correctement");
    }

    @Test
    @DisplayName("Une note à 20 doit être acceptée (note maximale)")
    void testNote_valeurMax() {
        Note note = new Note();
        note.setValeur(20.0);

        assertEquals(20.0, note.getValeur(), 0.001,
            "Une note à 20 est valide et doit être stockée correctement");
    }

    // -------------------------------------------------------
    //  Tests de calcul de moyenne (logique métier)
    // -------------------------------------------------------

    @Test
    @DisplayName("La moyenne de notes entières doit être exacte")
    void testMoyenne_notesEnitieres() {
        // Notes : 10, 14, 16 → Moyenne = 40 / 3 = 13.33...
        ArrayList<Note> notes = new ArrayList<>();
        notes.add(creerNote(eleve1, 10.0));
        notes.add(creerNote(eleve2, 14.0));
        notes.add(creerNote(eleve3, 16.0));
        evaluation.setNotes(notes);

        double moyenne = calculerMoyenne(evaluation);

        assertEquals(13.33, moyenne, 0.01,
            "La moyenne de 10, 14 et 16 doit être environ 13.33");
    }

    @Test
    @DisplayName("La moyenne avec une seule note doit retourner cette note")
    void testMoyenne_uneSeuleNote() {
        ArrayList<Note> notes = new ArrayList<>();
        notes.add(creerNote(eleve1, 17.5));
        evaluation.setNotes(notes);

        double moyenne = calculerMoyenne(evaluation);

        assertEquals(17.5, moyenne, 0.001,
            "La moyenne d'une seule note doit être égale à cette note");
    }

    @Test
    @DisplayName("La moyenne d'une évaluation sans notes doit retourner 0")
    void testMoyenne_aucuneNote() {
        evaluation.setNotes(new ArrayList<>());

        double moyenne = calculerMoyenne(evaluation);

        assertEquals(0.0, moyenne, 0.001,
            "Sans aucune note, la moyenne doit être 0 sans lever d'exception");
    }

    @Test
    @DisplayName("La liste de notes d'une évaluation ne doit pas être null après initialisation")
    void testEvaluation_listeNotesNonNull() {
        evaluation.setNotes(new ArrayList<>());

        assertNotNull(evaluation.getNotes(),
            "La liste de notes ne doit pas être null");
        assertEquals(0, evaluation.getNotes().size(),
            "La liste doit être vide si aucune note n'a été ajoutée");
    }

    // -------------------------------------------------------
    //  Méthodes utilitaires privées (pour ne pas répéter le code)
    // -------------------------------------------------------

    /**
     * Crée une Note simple avec un élève et une valeur donnés.
     * Méthode utilitaire pour éviter la duplication de code dans les tests.
     */
    private Note creerNote(User eleve, double valeur) {
        Note note = new Note();
        note.setEleve(eleve);
        note.setValeur(valeur);
        note.setEvaluation(evaluation);
        return note;
    }

    /**
     * Calcule la moyenne des notes d'une évaluation.
     * Cette logique devrait idéalement se trouver dans le modèle ou un service,
     * mais on la teste ici pour valider le comportement attendu.
     */
    private double calculerMoyenne(Evaluation eval) {
        ArrayList<Note> notes = eval.getNotes();
        if (notes == null || notes.isEmpty()) return 0.0;

        double total = notes.stream().mapToDouble(Note::getValeur).sum();
        return total / notes.size();
    }
}

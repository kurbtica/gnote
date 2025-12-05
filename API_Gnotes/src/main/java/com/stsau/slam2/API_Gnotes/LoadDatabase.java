package com.stsau.slam2.API_Gnotes;

import com.stsau.slam2.API_Gnotes.model.*;
import com.stsau.slam2.API_Gnotes.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Configuration
class LoadDatabase {

    private static final Logger log = LoggerFactory.getLogger(LoadDatabase.class);

    @Bean
    CommandLineRunner initDatabase(UserRepository userRepository,
                                   MatiereRepository matiereRepository,
                                   NoteTypeRepository noteTypeRepository,
                                   EvaluationRepository evaluationRepository, // Ajout
                                   NoteRepository noteRepository) {       // Ajout

        return args -> {

            // --- 1. Création des MATIERES ---
            Matiere math = new Matiere("MATH");
            Matiere cyber = new Matiere("CYBER");
            Matiere anglais = new Matiere("ANGLAIS");
            Matiere cejma = new Matiere("CEJMA");

            if (matiereRepository.count() == 0) {
                // On sauvegarde et on réassigne l'objet pour avoir son ID généré
                math = matiereRepository.save(math);
                cyber = matiereRepository.save(cyber);
                anglais = matiereRepository.save(anglais);
                cejma = matiereRepository.save(cejma);

                matiereRepository.save(new Matiere("SUP DISPO"));
                matiereRepository.save(new Matiere("ATELIER PRO"));
                matiereRepository.save(new Matiere("CEJM"));
                matiereRepository.save(new Matiere("CULTURE G"));
                log.info("Matières chargées.");
            } else {
                // Si la BDD est déjà remplie, on récupère celles dont on a besoin pour la suite
                // (Ceci est une simplification pour l'exemple, en prod on ferait des findByName)
                List<Matiere> all = matiereRepository.findAll();
                if(!all.isEmpty()) {
                    math = all.stream().filter(m -> m.getLibelle().equals("MATH")).findFirst().orElse(null);
                    cyber = all.stream().filter(m -> m.getLibelle().equals("CYBER")).findFirst().orElse(null);
                    anglais = all.stream().filter(m -> m.getLibelle().equals("ANGLAIS")).findFirst().orElse(null);
                }
            }

            // --- 2. Création des TYPES DE NOTES ---
            NoteType ds = new NoteType("Devoir Surveillé");
            NoteType tp = new NoteType("TP");
            NoteType oral = new NoteType("Exposé/Oral");

            if (noteTypeRepository.count() == 0) {
                ds = noteTypeRepository.save(ds);
                tp = noteTypeRepository.save(tp);
                oral = noteTypeRepository.save(oral);
                noteTypeRepository.save(new NoteType("Devoir Maison"));
                log.info("Types de notes chargés.");
            } else {
                List<NoteType> allTypes = noteTypeRepository.findAll();
                if(!allTypes.isEmpty()) {
                    ds = allTypes.stream().filter(t -> t.getLibelle().equals("Devoir Surveillé")).findFirst().orElse(null);
                    tp = allTypes.stream().filter(t -> t.getLibelle().equals("TP")).findFirst().orElse(null);
                    oral = allTypes.stream().filter(t -> t.getLibelle().equals("Exposé/Oral")).findFirst().orElse(null);
                }
            }

            // --- 3. Création des UTILISATEURS ---
            // On prépare des listes pour stocker nos objets afin de les utiliser plus bas
            List<User> etudiants = new ArrayList<>();
            User profMath = new User("Bernard", "Sophie", Role.ENSEIGNANT, "sophie.bernard@example.com", "Boulevard de la Gare", "0611223344");
            User profCyber = new User("Enseignant", "Durand", Role.ENSEIGNANT, "pierre.durand@example.com", "Avenue Victor Hugo", "0605060708");
            User profAnglais = new User("Petit", "Claire", Role.ENSEIGNANT, "claire.petit@example.com", "Rue Nationale", "0622334455");

            if (userRepository.count() == 0) {
                // Sauvegarde des profs
                profMath = userRepository.save(profMath);
                profCyber = userRepository.save(profCyber);
                profAnglais = userRepository.save(profAnglais);
                userRepository.save(new User("Guerin", "Alice", Role.ENSEIGNANT, "alice.guerin@example.com", "Rue du Parc", "0612121212"));
                userRepository.save(new User("Test", "prof", Role.ENSEIGNANT, "prof@lycee.local", "test", "0102030405"));

                // Sauvegarde des admins
                userRepository.save(new User("Admin", "Martin", Role.ADMIN, "martin.admin@example.com", "Rue de la Liberté", "0708091011"));
                userRepository.save(new User("Renard", "Maxime", Role.ADMIN, "maxime.renard@example.com", "Impasse des Pins", "0688997788"));
                userRepository.save(new User("Test", "admin", Role.ADMIN, "admin@lycee.local", "test", "0102030405"));

                // Sauvegarde des étudiants et ajout à la liste
                etudiants.add(userRepository.save(new User("Leclerc", "Emma", Role.ETUDIANT, "emma.leclerc@example.com", "Rue des Acacias", "0602030405")));
                etudiants.add(userRepository.save(new User("Morel", "Lucas", Role.ETUDIANT, "lucas.morel@example.com", "Rue des Érables", "0677889900")));
                etudiants.add(userRepository.save(new User("Dupont", "Nicolas", Role.ETUDIANT, "nicolas.dupont@example.com", "Rue de la Mairie", "0644556677")));
                etudiants.add(userRepository.save(new User("Test", "etudiant", Role.ETUDIANT, "etudiant@lycee.local", "test", "0102030405")));

                log.info("Utilisateurs chargés.");
            }

            // --- 4. Création des EVALUATIONS et NOTES ---
            // On ne crée les données que si les tables sont vides pour éviter les doublons au redémarrage
            if (evaluationRepository.count() == 0 && !etudiants.isEmpty()) {

                // --- EVALUATION 1 : MATHS (DS) ---
                Evaluation evalMath = new Evaluation();
                evalMath.setTitre("Contrôle sur les matrices");
                evalMath.setCoefficient(2.0);
                evalMath.setDate("2023-10-15");
                evalMath.setMatiere(math);
                evalMath.setEnseignant(profMath);
                evalMath.setNoteType(ds);
                evalMath = evaluationRepository.save(evalMath);

                // Notes pour Eval 1
                creerNote(noteRepository, etudiants.get(0), evalMath, 15.0); // Emma
                creerNote(noteRepository, etudiants.get(1), evalMath, 12.5); // Lucas
                creerNote(noteRepository, etudiants.get(2), evalMath, 18.0); // Nicolas
                creerNote(noteRepository, etudiants.get(3), evalMath, 08.0); // Test

                // --- EVALUATION 2 : CYBER (TP) ---
                Evaluation evalCyber = new Evaluation();
                evalCyber.setTitre("TP Firewall");
                evalCyber.setCoefficient(1.0);
                evalCyber.setDate("2023-11-05");
                evalCyber.setMatiere(cyber);
                evalCyber.setEnseignant(profCyber);
                evalCyber.setNoteType(tp);
                evalCyber = evaluationRepository.save(evalCyber);

                // Notes pour Eval 2
                creerNote(noteRepository, etudiants.get(0), evalCyber, 16.0);
                creerNote(noteRepository, etudiants.get(1), evalCyber, 14.0);
                creerNote(noteRepository, etudiants.get(2), evalCyber, 19.5);
                creerNote(noteRepository, etudiants.get(3), evalCyber, 10.0);

                // --- EVALUATION 3 : ANGLAIS (ORAL) ---
                Evaluation evalAnglais = new Evaluation();
                evalAnglais.setTitre("Présentation Stage");
                evalAnglais.setCoefficient(3.0);
                evalAnglais.setDate("2023-12-01");
                evalAnglais.setMatiere(anglais);
                evalAnglais.setEnseignant(profAnglais);
                evalAnglais.setNoteType(oral);
                evalAnglais = evaluationRepository.save(evalAnglais);

                // Notes pour Eval 3
                creerNote(noteRepository, etudiants.get(0), evalAnglais, 14.5);
                creerNote(noteRepository, etudiants.get(1), evalAnglais, 13.0);
                creerNote(noteRepository, etudiants.get(2), evalAnglais, 11.0);
                creerNote(noteRepository, etudiants.get(3), evalAnglais, 12.0);

                log.info("Evaluations et Notes chargées.");
            }
        };
    }

    // Petite méthode utilitaire pour alléger le code principal
    private void creerNote(NoteRepository repo, User eleve, Evaluation eval, Double valeur) {
        Note note = new Note();
        note.setEleve(eleve);
        note.setEvaluation(eval);
        note.setValeur(valeur);
        note.setModification(Timestamp.from(Instant.now()));
        repo.save(note);
        log.info("Note créée : " + valeur + " pour " + eleve.getNom());
    }
}
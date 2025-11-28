package com.stsau.slam2.API_Gnotes;

import com.stsau.slam2.API_Gnotes.model.Matiere;
import com.stsau.slam2.API_Gnotes.model.NoteType;
import com.stsau.slam2.API_Gnotes.model.Role;
import com.stsau.slam2.API_Gnotes.model.User;
import com.stsau.slam2.API_Gnotes.repository.MatiereRepository;
import com.stsau.slam2.API_Gnotes.repository.NoteTypeRepository;
import com.stsau.slam2.API_Gnotes.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class LoadDatabase {

    private static final Logger log = LoggerFactory.getLogger(LoadDatabase.class);

    @Bean
        // Bean indique que l'instance de CommandeLineRunner est un singleton avec une injection de dépendances
        // LoadDatabase dépend de repository
    CommandLineRunner initDatabase(UserRepository userRepository, MatiereRepository matiereRepository, NoteTypeRepository noteTypeRepository) {

        return args -> {
            if (userRepository.count() == 0) {
                log.info("Preloading " + userRepository.save(new User("Enseignant", "Durand", Role.ENSEIGNANT, "pierre.durand@example.com", "Avenue Victor Hugo", "0605060708")));
                log.info("Preloading " + userRepository.save(new User("Admin", "Martin", Role.ADMIN, "martin.admin@example.com", "Rue de la Liberté", "0708091011")));
                log.info("Preloading " + userRepository.save(new User("Leclerc", "Emma", Role.ETUDIANT, "emma.leclerc@example.com", "Rue des Acacias", "0602030405")));
                log.info("Preloading " + userRepository.save(new User("Bernard", "Sophie", Role.ENSEIGNANT, "sophie.bernard@example.com", "Boulevard de la Gare", "0611223344")));
                log.info("Preloading " + userRepository.save(new User("Morel", "Lucas", Role.ETUDIANT, "lucas.morel@example.com", "Rue des Érables", "0677889900")));
                log.info("Preloading " + userRepository.save(new User("Petit", "Claire", Role.ENSEIGNANT, "claire.petit@example.com", "Rue Nationale", "0622334455")));
                log.info("Preloading " + userRepository.save(new User("Dupont", "Nicolas", Role.ETUDIANT, "nicolas.dupont@example.com", "Rue de la Mairie", "0644556677")));
                log.info("Preloading " + userRepository.save(new User("Guerin", "Alice", Role.ENSEIGNANT, "alice.guerin@example.com", "Rue du Parc", "0612121212")));
                log.info("Preloading " + userRepository.save(new User("Renard", "Maxime", Role.ADMIN, "maxime.renard@example.com", "Impasse des Pins", "0688997788")));

                // Utilisateur de Test
                log.info("Preloading " + userRepository.save(new User("Test", "admin", Role.ADMIN, "admin@lycee.local", "test", "0102030405")));
                log.info("Preloading " + userRepository.save(new User("Test", "prof", Role.ENSEIGNANT, "prof@lycee.local", "test", "0102030405")));
                log.info("Preloading " + userRepository.save(new User("Test", "etudiant", Role.ETUDIANT, "etudiant@lycee.local", "test", "0102030405")));
            }

            if (matiereRepository.count() == 0) {
                log.info("Preloading " + matiereRepository.save(new Matiere("CEJMA")));
                log.info("Preloading " + matiereRepository.save(new Matiere("CYBER")));
                log.info("Preloading " + matiereRepository.save(new Matiere("SUP DISPO")));
                log.info("Preloading " + matiereRepository.save(new Matiere("ATELIER PRO")));

                log.info("Preloading " + matiereRepository.save(new Matiere("ANGLAIS")));
                log.info("Preloading " + matiereRepository.save(new Matiere("CEJM")));
                log.info("Preloading " + matiereRepository.save(new Matiere("CULTURE G")));
                log.info("Preloading " + matiereRepository.save(new Matiere("MATH")));
            }

            if (noteTypeRepository.count() == 0) {
                log.info("Preloading " + noteTypeRepository.save(new NoteType("Devoir Maison")));
                log.info("Preloading " + noteTypeRepository.save(new NoteType("Devoir Surveillé")));
                log.info("Preloading " + noteTypeRepository.save(new NoteType("Exposé/Oral")));
                log.info("Preloading " + noteTypeRepository.save(new NoteType("TP")));
            }
        };
    }
}
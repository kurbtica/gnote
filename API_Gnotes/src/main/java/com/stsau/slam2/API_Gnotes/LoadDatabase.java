package com.stsau.slam2.API_Gnotes;

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
    CommandLineRunner initDatabase(UserRepository userRepository) {

        return args -> {
//            log.info("Preloading " + userRepository.save(new User("Enseignant","Durand", Role.ENSEIGNANT,"pierre.durand@example.com","Avenue Victor Hugo","0605060708")));
//            log.info("Preloading " + userRepository.save(new User( "Admin", "Martin", Role.ADMIN,"martin.admin@example.com", "Rue de la Liberté","0708091011" )));
//            log.info("Preloading " + userRepository.save(new User( "test", "azdfe", Role.ETUDIANT,"martin.admin@example.com", "Rue de la Liberté","0708091011" )));

        };
    }
}
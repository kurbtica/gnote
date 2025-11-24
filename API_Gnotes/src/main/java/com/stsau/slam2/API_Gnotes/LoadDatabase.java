package com.stsau.slam2.API_Gnotes;

import com.stsau.slam2.API_Gnotes.model.Role;
import com.stsau.slam2.API_Gnotes.repository.RoleRepository;
import com.stsau.slam2.API_Gnotes.model.User;
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
    CommandLineRunner initDatabase(UserRepository userRepository, RoleRepository roleRepository) {

        return args -> {
            //ajout des role
            log.info("Preloading " + roleRepository.save(new Role("ADMIN")));
            log.info("Preloading " + roleRepository.save(new Role("ENSEIGNANT")));
            log.info("Preloading " + roleRepository.save(new Role("ETUDIANT")));


            log.info("Preloading " + userRepository.save(new User("Enseignant","Durand", 2,"pierre.durand@example.com","Avenue Victor Hugo","0605060708")));
            log.info("Preloading " + userRepository.save(new User( "Admin", "Martin",1,"martin.admin@example.com", "Rue de la Liberté","0708091011" )));
            log.info("Preloading " + userRepository.save(new User( "test", "azdfe",1,"martin.admin@example.com", "Rue de la Liberté","0708091011" )));

        };
    }
}
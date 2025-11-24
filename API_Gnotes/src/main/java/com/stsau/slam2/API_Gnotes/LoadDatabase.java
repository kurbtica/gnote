package com.stsau.slam2.API_Gnotes;

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
    CommandLineRunner initDatabase(UserRepository repository) {

        return args -> {
            log.info("Preloading " + repository.save(new User("Enseignant","Durand", 2,"pierre.durand@example.com","Avenue Victor Hugo","0605060708")));
            log.info("Preloading " + repository.save(new User( "Admin", "Martin",1,"martin.admin@example.com", "Rue de la Liberté","0708091011" )));
            log.info("Preloading " + repository.save(new User( "test", "azdfe",1,"martin.admin@example.com", "Rue de la Liberté","0708091011" )));

        };
    }
}
package com.stsau.slam2.API_Gnotes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ApiGnotesApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApiGnotesApplication.class, args);
	}
	/*@Bean
	public CommandLineRunner repairAdminPassword(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		return args -> {
			User admin = userRepository.findByEmail("admin@lycee.local").orElse(null);
			if (admin != null) {
				String newHash = passwordEncoder.encode("password");
				admin.setPassword(newHash);
				userRepository.save(admin);
				System.out.println("✅ MOT DE PASSE RÉPARÉ : L'utilisateur admin a été mis à jour avec un hash valide !");
			}
		};
	}*/
}
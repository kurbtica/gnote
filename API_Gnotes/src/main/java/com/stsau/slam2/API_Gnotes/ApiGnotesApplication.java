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
}
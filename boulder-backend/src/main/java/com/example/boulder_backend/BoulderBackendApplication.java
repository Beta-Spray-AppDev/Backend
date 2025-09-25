package com.example.boulder_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class BoulderBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BoulderBackendApplication.class, args);
		System.out.println("Server startet!");
	}

}

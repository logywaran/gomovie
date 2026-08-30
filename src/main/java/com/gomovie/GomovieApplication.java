package com.gomovie;

import org.springframework.boot.SpringApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableScheduling
public class GomovieApplication {

	public static void main(String[] args) {
		SpringApplication.run(GomovieApplication.class, args);
	}

}

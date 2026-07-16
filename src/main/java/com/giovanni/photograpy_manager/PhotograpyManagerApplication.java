package com.giovanni.photograpy_manager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableAsync
public class PhotograpyManagerApplication {
	public static void main(String[] args) {
		SpringApplication.run(PhotograpyManagerApplication.class, args);
	}

}

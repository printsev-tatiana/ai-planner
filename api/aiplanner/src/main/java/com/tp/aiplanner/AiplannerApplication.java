package com.tp.aiplanner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class AiplannerApplication {

	public static void main(String[] args) {
		SpringApplication.run(AiplannerApplication.class, args);
	}

}

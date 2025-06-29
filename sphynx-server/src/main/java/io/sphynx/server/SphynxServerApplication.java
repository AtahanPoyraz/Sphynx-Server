package io.sphynx.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class SphynxServerApplication {
	public static void main(String[] args) {
		SpringApplication.run(SphynxServerApplication.class, args);
	}
}

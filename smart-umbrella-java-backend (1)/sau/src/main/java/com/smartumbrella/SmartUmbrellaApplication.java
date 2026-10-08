package com.smartumbrella;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Entry point. Spring Boot is used for the REST API, security and database access (JPA). */
@SpringBootApplication
@EnableScheduling
public class SmartUmbrellaApplication {
    public static void main(String[] args) {
        SpringApplication.run(SmartUmbrellaApplication.class, args);
    }
}

package de.trafficvalidator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot main application class for the Traffic Light Validator.
 * This class serves as the entry point for the application.
 */
@SpringBootApplication
public class TrafficValidatorApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(TrafficValidatorApplication.class, args);
    }
}
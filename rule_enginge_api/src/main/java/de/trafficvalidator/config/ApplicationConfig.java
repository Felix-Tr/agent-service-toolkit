package de.trafficvalidator.config;

import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Application configuration class for beans and services
 */
@Configuration
public class ApplicationConfig {
    
    /**
     * Creates and configures a KieContainer for Drools rule execution
     */
    @Bean
    public KieContainer kieContainer() {
        return KieServices.Factory.get().getKieClasspathContainer();
    }
}
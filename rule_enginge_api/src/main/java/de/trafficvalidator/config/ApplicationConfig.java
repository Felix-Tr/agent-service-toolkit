package de.trafficvalidator.config;

import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import javax.annotation.PreDestroy;

/**
 * Application configuration class for beans and services
 */
@Configuration
public class ApplicationConfig {
    
    private KieContainer kieContainer;
    
    /**
     * Creates and configures a KieContainer for Drools rule execution
     */
    @Bean
    public KieContainer kieContainer() {
        KieServices kieServices = KieServices.Factory.get();
        kieContainer = kieServices.getKieClasspathContainer();
        return kieContainer;
    }
    
    /**
     * Properly dispose of the KieContainer when the application context is closed
     */
    @PreDestroy
    public void destroy() {
        if (kieContainer != null) {
            kieContainer.dispose();
        }
    }
}
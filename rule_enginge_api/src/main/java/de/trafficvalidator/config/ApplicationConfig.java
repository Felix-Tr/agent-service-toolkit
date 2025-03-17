package de.trafficvalidator.config;

import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import javax.annotation.PreDestroy;

/**
 * Application configuration class for beans and services
 */
@Configuration
public class ApplicationConfig {
    
    private KieContainer kieContainer;
    
    /**
     * Creates and configures a KieContainer for Drools rule execution
     * with proper handling of ClassLoader issues
     */
    @Bean
    public KieContainer kieContainer() {
        // Get the KieServices instance
        KieServices kieServices = KieServices.Factory.get();
        
        // Create a new KieContainer with the current ClassLoader
        // This approach uses a specific ClassLoader to avoid conflicts
        ClassLoader classLoader = getClass().getClassLoader();
        kieContainer = kieServices.getKieClasspathContainer(classLoader);
        
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
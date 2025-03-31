package de.trafficvalidator.config;

import org.drools.ruleunits.api.RuleUnitProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Application configuration class for beans and services
 */
@Configuration
public class ApplicationConfig {
    
    /**
     * Configures the Drools Rule Unit Provider
     * This ensures the Rule Unit system is properly initialized
     */
    @Bean
    public void configureRuleUnitProvider() {
        // This is a no-op method that ensures Spring recognizes the Drools Rule Unit system
        RuleUnitProvider provider = RuleUnitProvider.get();
    }
}
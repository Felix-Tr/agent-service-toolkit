package de.trafficvalidator.config;

import de.trafficvalidator.rules.CyclistArrowRuleUnit;
import org.drools.ruleunits.api.RuleUnitInstance;
import org.drools.ruleunits.api.RuleUnitProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * Configuration class to ensure Rule Units are properly discovered by Drools.
 */
@Configuration
public class DroolsConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(DroolsConfig.class);
    
    /**
     * Pre-creates a Rule Unit instance during application startup.
     * This ensures the CyclistArrowRuleUnit is registered with Drools.
     */
    @PostConstruct
    public void initRuleUnits() {
        logger.info("Initializing Rule Units with Drools");
        
        try {
            // Create a temporary rule unit instance to register it with Drools
            CyclistArrowRuleUnit ruleUnit = new CyclistArrowRuleUnit();
            try (RuleUnitInstance<CyclistArrowRuleUnit> instance = 
                    RuleUnitProvider.get().createRuleUnitInstance(ruleUnit)) {
                // Just creating the instance is enough to register it
                // The try-with-resources will close it automatically
            }
            
            logger.info("Successfully initialized CyclistArrowRuleUnit");
        } catch (Exception e) {
            logger.error("Failed to initialize rule units: {}", e.getMessage(), e);
        }
    }
} 
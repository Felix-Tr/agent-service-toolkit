package de.trafficvalidator.rules;

import de.trafficvalidator.model.Connection;
import org.drools.ruleunits.api.RuleUnitData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Registry for all rule unit categories in the system.
 * This class maintains a registry of available rule unit types.
 */
@Service
public class RuleUnitRegistry {
    private static final Logger logger = LoggerFactory.getLogger(RuleUnitRegistry.class);
    
    private static final Map<String, Class<? extends RuleUnitData>> ruleUnitTypes = new HashMap<>();
    
    static {
        // Register rule unit classes
        ruleUnitTypes.put("cyclist-arrow", CyclistArrowRuleUnit.class);
        ruleUnitTypes.put("signal-group", SignalGroupRuleUnit.class);
    }
    
    /**
     * Gets a rule unit class for a specific category
     * 
     * @param category The rule unit category
     * @return The rule unit class for the category, or null if not found
     */
    public Class<? extends RuleUnitData> getRuleUnitClass(String category) {
        return ruleUnitTypes.get(category);
    }
    
    /**
     * Creates a rule unit instance for a category with a collection of connections
     * 
     * @param category The rule unit category
     * @param connections The collection of connections to add to the rule unit
     * @return A rule unit instance, or null if the category is not registered
     */
    public RuleUnitData createRuleUnit(String category, Collection<Connection> connections) {
        Class<? extends RuleUnitData> ruleUnitClass = getRuleUnitClass(category);
        if (ruleUnitClass == null) {
            return null;
        }
        
        try {
            // Try to create using constructor that takes Collection<Connection>
            try {
                return ruleUnitClass.getConstructor(Collection.class).newInstance(connections);
            } catch (NoSuchMethodException e) {
                // Fall back to default constructor
                RuleUnitData ruleUnit = ruleUnitClass.getConstructor().newInstance();
                
                // Try to add connections using addConnection method
                try {
                    Method addConnectionMethod = ruleUnitClass.getMethod("addConnection", Connection.class);
                    for (Connection connection : connections) {
                        addConnectionMethod.invoke(ruleUnit, connection);
                    }
                } catch (NoSuchMethodException ex) {
                    logger.warn("Could not add connections to rule unit: no addConnection method found");
                }
                
                return ruleUnit;
            }
        } catch (Exception e) {
            logger.error("Failed to create rule unit for category {}", category, e);
            throw new RuntimeException("Failed to create rule unit for category: " + category, e);
        }
    }
    
    /**
     * Gets all registered rule unit categories
     * 
     * @return A set of all registered rule unit categories
     */
    public Set<String> getAvailableCategories() {
        return ruleUnitTypes.keySet();
    }
} 
package de.trafficvalidator.rules;

import de.trafficvalidator.model.Connection;
import de.trafficvalidator.model.Intersection;
import de.trafficvalidator.model.ValidationResult;
import org.drools.core.impl.InternalKieContainer;
import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Implements rule validation for green cyclist arrow exclusion criteria.
 */
public class GreenCyclistArrowRules {
    private static final Logger logger = LoggerFactory.getLogger(GreenCyclistArrowRules.class);
    
    private KieContainer kieContainer;
    
    public GreenCyclistArrowRules() {
        try {
            // Initialize Drools KIE container
            KieServices kieServices = KieServices.Factory.get();
            kieContainer = kieServices.getKieClasspathContainer();
            
            logger.info("Drools rule engine initialized successfully");
        } catch (Exception e) {
            logger.error("Failed to initialize Drools rule engine", e);
            throw new RuntimeException("Failed to initialize rule engine", e);
        }
    }
    
    /**
     * Validates all right-turn connections for cyclists at an intersection
     */
    public List<ValidationResult> validateIntersection(Intersection intersection) {
        logger.info("Validating intersection {} for green cyclist arrow rules", intersection.getId());
        
        List<ValidationResult> results = new ArrayList<>();
        
        // Find all right-turn connections that allow cyclists
        List<Connection> rightTurnConnections = intersection.getCyclistRightTurnConnections();
        logger.info("Found {} cyclist right-turn connections to validate", rightTurnConnections.size());
        
        // Validate each connection
        for (Connection connection : rightTurnConnections) {
            ValidationResult result = validateConnection(intersection, connection);
            results.add(result);
        }
        
        return results;
    }
    
    /**
     * Validates a specific connection against the green cyclist arrow rules
     */
    public ValidationResult validateConnection(Intersection intersection, Connection connection) {
        if (!connection.isCyclistRightTurn()) {
            logger.warn("Connection {} is not a cyclist right turn, skipping validation", connection);
            ValidationResult result = new ValidationResult(connection);
            result.addFailure("Not a cyclist right turn connection");
            return result;
        }
        
        logger.info("Validating connection: {}", connection);
        
        // Create validation result
        ValidationResult result = new ValidationResult(connection);
        
        // Create KIE session for rules
        KieSession kieSession = kieContainer.newKieSession("GreenCyclistArrowRulesSession");
        
        try {
            // Insert facts into session
            kieSession.insert(connection);
            kieSession.insert(result);
            
            // Insert all other connections from the intersection
            for (Connection otherConnection : intersection.getConnections()) {
                if (otherConnection != connection) {
                    kieSession.insert(otherConnection);
                }
            }
            
            // Fire rules
            int rulesFired = kieSession.fireAllRules();
            logger.debug("Fired {} rules for connection {}", rulesFired, connection);
            
            // Log validation result
            if (result.isValid()) {
                logger.info("Connection {} is valid for green cyclist arrow", connection);
            } else {
                logger.info("Connection {} is invalid for green cyclist arrow: {}", connection, result.getReasons());
            }
            
            return result;
        } finally {
            kieSession.dispose();
        }
    }
    
    /**
     * Manual validation for testing or when Drools is not available
     */
    public ValidationResult validateConnectionManually(Intersection intersection, Connection connection) {
        ValidationResult result = new ValidationResult(connection);
        
        if (!connection.isCyclistRightTurn()) {
            result.addFailure("Not a cyclist right turn connection");
            return result;
        }
        
        // Check for conflict-free left turn from opposite direction
        List<Connection> conflictingLeftTurns = intersection.getConflictingLeftTurnConnections(connection);
        for (Connection conflictingConnection : conflictingLeftTurns) {
            if (conflictingConnection.getSignalGroup() != null) {
                if (conflictingConnection.getSignalGroup().isDiagonalLeftTurn()) {
                    result.addFailure("VwV-StVo zu § 37, XI., 1. b) - Opposing traffic has a green diagonal arrow for left turn (DN signal group: " + 
                        conflictingConnection.getSignalGroup().getName() + ")");
                } else if (conflictingConnection.getSignalGroup().controlsOnlyLeftTurns()) {
                    result.addFailure("VwV-StVo zu § 37, XI., 1. a) - Opposing traffic has a conflict-free left turn signal (dedicated signal group: " + 
                        conflictingConnection.getSignalGroup().getName() + ")");
                }
            }
        }
        
        // Check for directional arrows in traffic lights (not supported in this version)
        // Would need additional data model support
        
        return result;
    }
}
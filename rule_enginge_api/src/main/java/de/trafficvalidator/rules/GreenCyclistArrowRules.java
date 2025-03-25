package de.trafficvalidator.rules;

import de.trafficvalidator.model.Connection;
import de.trafficvalidator.model.Intersection;
import de.trafficvalidator.model.SignalGroup;
import de.trafficvalidator.model.ValidationResult;
import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.StatelessKieSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Implements rule validation for green cyclist arrow exclusion criteria.
 * <p>
 * This class uses Drools rules defined in cyclist_arrow_rules.drl to validate
 * if a green cyclist arrow sign (Verkehrszeichen 721) can be installed at a specific
 * right turn connection.
 * </p>
 */
public class GreenCyclistArrowRules {
    private static final Logger logger = LoggerFactory.getLogger(GreenCyclistArrowRules.class);
    private static final String SESSION_NAME = "GreenCyclistArrowRulesSession";
    private static final String DRL_FILE = "de/trafficvalidator/rules/cyclist_arrow_rules.drl";

    private final KieContainer kieContainer;

    /**
     * Constructor that accepts an existing KieContainer
     * 
     * @param kieContainer The KieContainer to use for rule execution
     */
    public GreenCyclistArrowRules(KieContainer kieContainer) {
        this.kieContainer = kieContainer;
        logger.info("GreenCyclistArrowRules initialized with provided KieContainer");
        validateRuleResources();
    }

    /**
     * Default constructor that creates a new KieContainer
     * This is kept for backward compatibility and testing
     * but should be avoided in a Spring environment
     * @deprecated Use the constructor with KieContainer parameter instead
     */
    @Deprecated
    public GreenCyclistArrowRules() {
        try {
            // Initialize Drools KIE container
            KieServices kieServices = KieServices.Factory.get();
            // Use the current ClassLoader to avoid conflicts
            kieContainer = kieServices.getKieClasspathContainer(getClass().getClassLoader());
            
            logger.info("Drools rule engine initialized successfully with new KieContainer");
            validateRuleResources();
        } catch (Exception e) {
            logger.error("Failed to initialize Drools rule engine", e);
            throw new RuntimeException("Failed to initialize rule engine", e);
        }
    }

    /**
     * Validates that the required rule resources are available
     */
    private void validateRuleResources() {
        ClassLoader classLoader = getClass().getClassLoader();
        if (classLoader.getResource(DRL_FILE) == null) {
            logger.warn("DRL file {} not found in classpath", DRL_FILE);
        } else {
            logger.info("DRL file {} found in classpath", DRL_FILE);
        }
    }

    /**
     * Validates all connections at an intersection against green cyclist arrow rules
     */
    public List<ValidationResult> validateIntersection(Intersection intersection) {
        logger.info("Validating intersection {} for green cyclist arrow rules", intersection.getId());

        List<ValidationResult> results = new ArrayList<>();

        // Let the rule engine determine which connections to validate by passing all connections
        List<Connection> connections = intersection.getConnections();
        logger.info("Passing {} connections to rule engine for validation", connections.size());

        // Validate each connection - let the rules determine which are relevant
        for (Connection connection : connections) {
            ValidationResult result = validateConnection(intersection, connection);
            // Only add non-empty results to the list (rule engine will skip irrelevant connections)
            if ((!result.getReasons().isEmpty() || result.isValid()) && connection.isCyclistRightTurn()) {
                results.add(result);
            }
        }

        logger.info("Found {} relevant validation results", results.size());
        return results;
    }

    /**
     * Validates a specific connection against the green cyclist arrow rules
     */
    public ValidationResult validateConnection(Intersection intersection, Connection connection) {
        logger.debug("Validating connection: {}", connection);

        // Create validation result
        ValidationResult result = new ValidationResult(connection);

        // Create KIE session for rules
        StatelessKieSession kieSession = kieContainer.newStatelessKieSession(SESSION_NAME);

        // Only pass the minimum required facts
        List<Object> facts = new ArrayList<>();
        facts.add(connection);
        facts.add(result);
        if (connection.getId() == 34 || connection.getId() == 33 || connection.getId() == 27) {
            logger.info("Validating connection with id 34");
        }
        // Execute the stateless session with minimal facts
        kieSession.execute(facts);

        // Log validation result only if it's a cyclist right turn (otherwise rules won't have done anything)
        if (connection.isCyclistRightTurn()) {
            if (result.isValid()) {
                logger.info("Connection {} is valid for green cyclist arrow", connection);
            } else {
                logger.info("Connection {} is invalid for green cyclist arrow: {}", connection, result.getReasons());
            }
        }

        return result;
    }
}
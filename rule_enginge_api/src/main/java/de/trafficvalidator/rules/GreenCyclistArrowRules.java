package de.trafficvalidator.rules;

import de.trafficvalidator.model.Connection;
import de.trafficvalidator.model.Direction;
import de.trafficvalidator.model.Intersection;
import de.trafficvalidator.model.Lane;
import de.trafficvalidator.model.SignalGroup;
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
            kieSession.insert(intersection);

            // Insert all other connections from the intersection
            for (Connection otherConnection : intersection.getConnections()) {
                if (otherConnection != connection) {
                    kieSession.insert(otherConnection);
                }
            }

            // Insert all physical signal groups (vt) for evaluation
            for (SignalGroup physicalSignalGroup : intersection.getPhysicalSignalGroups().values()) {
                kieSession.insert(physicalSignalGroup);
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

        // Check VwV-StVo zu § 37, XI., 1. a):
        // No conflict-free left turn signals for opposing traffic
        checkConflictFreeLeftTurn(intersection, connection, result);

        // Check VwV-StVo zu § 37, XI., 1. b):
        // No green diagonal arrow for opposing left-turning traffic
        checkDiagonalGreenArrow(intersection, connection, result);

        // Check VwV-StVo zu § 37, XI., 1. c):
        // No directional arrows in the traffic lights for the right-turn lane
        checkDirectionalArrows(connection, result);

        return result;
    }

    /**
     * Checks if there are conflict-free left turn signals for opposing traffic
     */
    private void checkConflictFreeLeftTurn(Intersection intersection, Connection rightTurn, ValidationResult result) {
        // Find the opposing ingress direction
        if (rightTurn.getIngressLane() == null || rightTurn.getIngressLane().getCardinalDirection() == null) {
            logger.warn("Cannot determine opposing direction for connection {}", rightTurn);
            return;
        }

        // Get the opposite direction
        Direction oppositeDirection = rightTurn.getIngressLane().getCardinalDirection().getOpposite();

        // Find lanes from the opposite direction
        List<Lane> opposingLanes = intersection.getIngressLanesByDirection()
                .getOrDefault(oppositeDirection, new ArrayList<>());

        // Check for conflict-free left turn connections
        for (Lane opposingLane : opposingLanes) {
            List<Connection> leftTurns = opposingLane.getLeftTurnConnections();

            for (Connection leftTurn : leftTurns) {
                // Skip if not targeting the same egress lane as the right turn
                if (leftTurn.getEgressLane() != rightTurn.getEgressLane()) {
                    continue;
                }

                // Check if this left turn has a dedicated signal group
                int physicalSignalGroupId = leftTurn.getPhysicalSignalGroupId();
                if (physicalSignalGroupId <= 0) {
                    continue;
                }

                SignalGroup physicalGroup = intersection.getPhysicalSignalGroup(physicalSignalGroupId);
                if (physicalGroup == null) {
                    continue;
                }

                // Check for conflict-free signaling
                if (intersection.hasConflictFreeLeftTurn(physicalSignalGroupId)) {
                    result.addFailure("VwV-StVo zu § 37, XI., 1. a) - Opposing traffic has a conflict-free left turn signal " +
                            "from " + opposingLane.getCardinalDirection() + " (physical signal group ID: " + physicalSignalGroupId + ")");
                    return;
                }
            }
        }
    }

    /**
     * Checks if there is a green diagonal arrow for opposing left-turning traffic
     */
    private void checkDiagonalGreenArrow(Intersection intersection, Connection rightTurn, ValidationResult result) {
        // Find the opposing ingress direction
        if (rightTurn.getIngressLane() == null || rightTurn.getIngressLane().getCardinalDirection() == null) {
            logger.warn("Cannot determine opposing direction for connection {}", rightTurn);
            return;
        }

        // Get the opposite direction
        Direction oppositeDirection = rightTurn.getIngressLane().getCardinalDirection().getOpposite();

        // Find lanes from the opposite direction
        List<Lane> opposingLanes = intersection.getIngressLanesByDirection()
                .getOrDefault(oppositeDirection, new ArrayList<>());

        // Check for left turn connections with diagonal green arrows
        for (Lane opposingLane : opposingLanes) {
            List<Connection> leftTurns = opposingLane.getLeftTurnConnections();

            for (Connection leftTurn : leftTurns) {
                // Skip if not targeting the same egress lane as the right turn
                if (leftTurn.getEgressLane() != rightTurn.getEgressLane()) {
                    continue;
                }

                // Check if this left turn has a physical signal group
                int physicalSignalGroupId = leftTurn.getPhysicalSignalGroupId();
                if (physicalSignalGroupId <= 0) {
                    continue;
                }

                SignalGroup physicalGroup = intersection.getPhysicalSignalGroup(physicalSignalGroupId);
                if (physicalGroup == null) {
                    continue;
                }

                // Check if it's a diagonal green arrow (DN type)
                if (physicalGroup.getType() == SignalGroup.SignalGroupType.DN) {
                    result.addFailure("VwV-StVo zu § 37, XI., 1. b) - Opposing traffic has a green diagonal arrow for left turn " +
                            "from " + opposingLane.getCardinalDirection() + " (DN signal group: " + physicalGroup.getName() + ")");
                    return;
                }
            }
        }
    }

    /**
     * Checks if there are directional arrows in the traffic lights for the right-turn lane
     * Note: This information might not be present in the data model and would need additional input
     */
    private void checkDirectionalArrows(Connection connection, ValidationResult result) {
        // This check requires information about directional arrows in traffic lights
        // which is typically not present in the data model
        // In a real-world scenario, this would need additional data sources

        // For now, we'll log a warning about this limitation
        logger.warn("Cannot check for directional arrows in traffic lights for connection {} " +
                "due to missing data", connection);
    }
}
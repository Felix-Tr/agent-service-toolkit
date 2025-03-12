package de.trafficvalidator.util;

import de.trafficvalidator.model.Connection;
import de.trafficvalidator.model.Direction;
import de.trafficvalidator.model.Intersection;
import de.trafficvalidator.model.Lane;
import de.trafficvalidator.model.SignalGroup;
import de.trafficvalidator.model.ValidationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Utility methods for validation and output formatting.
 */
public class ValidationUtils {
    private static final Logger logger = LoggerFactory.getLogger(ValidationUtils.class);
    
    /**
     * Formats validation results for display
     */
    public static String formatValidationResults(List<ValidationResult> results) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("===== GREEN CYCLIST ARROW VALIDATION RESULTS =====\n\n");
        
        // Count valid and invalid connections
        long validCount = results.stream().filter(ValidationResult::isValid).count();
        long invalidCount = results.size() - validCount;
        
        sb.append(String.format("Total connections checked: %d (Valid: %d, Invalid: %d)\n\n", 
                results.size(), validCount, invalidCount));
        
        // Sort results by direction for nicer output
        Collections.sort(results, Comparator.comparing(ValidationResult::getConnectionDirection));
        
        // Format each result
        for (ValidationResult result : results) {
            sb.append(result.toString()).append("\n\n");
        }
        
        return sb.toString();
    }
    
    /**
     * Prints intersection summary information for debugging
     */
    public static String summarizeIntersection(Intersection intersection) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("===== INTERSECTION SUMMARY =====\n");
        sb.append("ID: ").append(intersection.getId())
          .append(" (Region: ").append(intersection.getRegionId()).append(")\n");
        
        if (intersection.getName() != null) {
            sb.append("Name: ").append(intersection.getName()).append("\n");
        }
        
        sb.append("Revision: ").append(intersection.getRevision()).append("\n");
        sb.append("Reference Point: (").append(intersection.getRefLat())
          .append(", ").append(intersection.getRefLong()).append(")\n\n");
        
        // Count lane types
        Map<Direction, List<Lane>> ingressLanes = intersection.getIngressLanesByDirection();
        Map<Direction, List<Lane>> egressLanes = intersection.getEgressLanesByDirection();
        
        sb.append("Ingress Lanes by Direction:\n");
        for (Map.Entry<Direction, List<Lane>> entry : ingressLanes.entrySet()) {
            sb.append("  ").append(entry.getKey()).append(": ")
              .append(entry.getValue().size()).append(" lanes\n");
            
            // Detail each lane
            for (Lane lane : entry.getValue()) {
                sb.append("    Lane ").append(lane.getId());
                if (lane.isVehicleLane()) sb.append(" [Vehicle]");
                if (lane.isBikeLane()) sb.append(" [Bike]");
                if (lane.allowsCyclists()) sb.append(" [Cyclists Allowed]");
                sb.append("\n");
            }
        }
        
        sb.append("\nEgress Lanes by Direction:\n");
        for (Map.Entry<Direction, List<Lane>> entry : egressLanes.entrySet()) {
            sb.append("  ").append(entry.getKey()).append(": ")
              .append(entry.getValue().size()).append(" lanes\n");
        }
        
        // Count connections by type
        List<Connection> rightTurns = intersection.getConnections().stream()
                .filter(Connection::isRightTurn).collect(Collectors.toList());
        List<Connection> leftTurns = intersection.getConnections().stream()
                .filter(Connection::isLeftTurn).collect(Collectors.toList());
        List<Connection> straightAhead = intersection.getConnections().stream()
                .filter(Connection::isStraight).collect(Collectors.toList());
        
        sb.append("\nConnections:\n");
        sb.append("  Total: ").append(intersection.getConnections().size()).append("\n");
        sb.append("  Right Turns: ").append(rightTurns.size()).append("\n");
        sb.append("  Left Turns: ").append(leftTurns.size()).append("\n");
        sb.append("  Straight Ahead: ").append(straightAhead.size()).append("\n");
        
        // Signal groups
        sb.append("\nSignal Groups:\n");
        for (SignalGroup group : intersection.getSignalGroups().values()) {
            sb.append("  ").append(group.getId()).append(": ")
              .append(group.getName()).append(" (").append(group.getType()).append(") - ")
              .append(group.getControlledConnections().size()).append(" connections\n");
        }
        
        return sb.toString();
    }
}
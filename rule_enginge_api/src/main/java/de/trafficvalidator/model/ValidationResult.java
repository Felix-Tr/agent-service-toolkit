package de.trafficvalidator.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the result of validating a green cyclist arrow (Verkehrszeichen 721) rule.
 */
public class ValidationResult {
    private final Connection connection;
    private final List<String> reasons = new ArrayList<>();
    private final List<String> executedRules = new ArrayList<>();
    private boolean valid = true;
    
    public ValidationResult(Connection connection) {
        this.connection = connection;
    }
    
    /**
     * Adds a failure reason and marks the result as invalid
     */
    public void addFailure(String reason) {
        this.valid = false;
        this.reasons.add(reason);
    }

    public Connection getConnection() {
        return connection;
    }

    public boolean isValid() {
        return valid;
    }

    public List<String> getReasons() {
        return new ArrayList<>(reasons);
    }

    public List<String> getExecutedRules() {
        return new ArrayList<>(executedRules);
    }

    public void addExecutedRule(String ruleName) {
        executedRules.add(ruleName);
    }
    
    /**
     * Returns a human-readable direction for this connection
     */
    public String getConnectionDirection() {
        Lane ingressLane = connection.getIngressLane();
        Lane egressLane = connection.getEgressLane();
        
        if (ingressLane == null || egressLane == null || 
            ingressLane.getDirection() == null || egressLane.getDirection() == null) {
            return "Unknown";
        }
        
        // Include both cardinal directions and maneuver type
        String maneuverInfo = "";
        if (connection.isStraight()) {
            maneuverInfo = " [Straight]";
        } else if (connection.isLeftTurn()) {
            maneuverInfo = " [Left Turn]";
        } else if (connection.isRightTurn()) {
            maneuverInfo = " [Right Turn]";
        } else if (connection.isUTurn()) {
            maneuverInfo = " [U-Turn]";
        }
        
        return ingressLane.getDirection().name() + " → " + egressLane.getDirection().name() + maneuverInfo;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Connection ").append(getConnectionDirection());
        sb.append(" (").append(connection.getIngressLane().getId())
          .append(" → ").append(connection.getEgressLane().getId()).append("): ");
        
        if (valid) {
            sb.append("VALID - Can place Verkehrszeichen 721");
        } else {
            sb.append("INVALID - Cannot place Verkehrszeichen 721 due to:");
            for (String reason : reasons) {
                sb.append("\n  - ").append(reason);
            }
        }
        
        return sb.toString();
    }
}
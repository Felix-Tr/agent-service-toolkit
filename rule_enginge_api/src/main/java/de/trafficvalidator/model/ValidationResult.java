package de.trafficvalidator.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the result of validating a green cyclist arrow (Verkehrszeichen 721) rule.
 */
public class ValidationResult {
    private Connection connection;
    private boolean isValid;
    private List<String> reasons = new ArrayList<>();
    
    public ValidationResult(Connection connection) {
        this.connection = connection;
        this.isValid = true; // Assume valid until proven otherwise
    }
    
    /**
     * Adds a failure reason and marks the result as invalid
     */
    public void addFailure(String reason) {
        this.isValid = false;
        this.reasons.add(reason);
    }

    public Connection getConnection() {
        return connection;
    }

    public boolean isValid() {
        return isValid;
    }

    public List<String> getReasons() {
        return reasons;
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
        
        return ingressLane.getDirection().name() + " → " + egressLane.getDirection().name();
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Connection ").append(getConnectionDirection());
        sb.append(" (").append(connection.getIngressLane().getId())
          .append(" → ").append(connection.getEgressLane().getId()).append("): ");
        
        if (isValid) {
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
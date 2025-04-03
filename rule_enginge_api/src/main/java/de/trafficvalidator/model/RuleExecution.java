package de.trafficvalidator.model;

/**
 * Represents a rule execution event.
 */
public class RuleExecution {
    private final String ruleName;
    private final int connectionId;
    private final long timestamp;

    public RuleExecution(String ruleName, int connectionId) {
        this.ruleName = ruleName;
        this.connectionId = connectionId;
        this.timestamp = System.currentTimeMillis();
    }

    public String getRuleName() {
        return ruleName;
    }

    public int getConnectionId() {
        return connectionId;
    }

    public long getTimestamp() {
        return timestamp;
    }
} 
package de.trafficvalidator.model;

/**
 * Represents a rule execution event.
 */
public class RuleExecution {
    private final String ruleName;
    private final long timestamp;

    public RuleExecution(String ruleName) {
        this.ruleName = ruleName;
        this.timestamp = System.currentTimeMillis();
    }

    public String getRuleName() {
        return ruleName;
    }

    public long getTimestamp() {
        return timestamp;
    }
} 
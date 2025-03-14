package de.trafficvalidator.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a physical signal group from the STG file (German "Signalgruppe").
 * A physical signal group controls one or more connections at an intersection.
 */
public class SignalGroup {
    private int physicalSignalGroupId;         // Physical signal group ID (vt) from STG file
    private String name;                       // Name like "FV01"
    private SignalGroupType type;              // Type like FV, DN, RD, FG
    private List<Connection> controlledConnections = new ArrayList<>();

    /**
     * Signal group types as described in the specification
     */
    public enum SignalGroupType {
        FV,  // Fahrverkehr (Individual vehicle traffic)
        DN,  // Diagonalgrünpfeile für Linksabbieger (Diagonal green arrow for left turn)
        RD,  // Radverkehr (Bicycle traffic)
        FG   // Fußgänger (Pedestrian traffic)
    }

    public SignalGroup(int physicalSignalGroupId, String name, SignalGroupType type) {
        this.physicalSignalGroupId = physicalSignalGroupId;
        this.name = name;
        this.type = type;
    }

    /**
     * Parse a signal group type from a string
     */
    public static SignalGroupType parseType(String typeStr) {
        if (typeStr == null || typeStr.isEmpty()) {
            throw new IllegalArgumentException("Signal group type cannot be empty");
        }

        try {
            return SignalGroupType.valueOf(typeStr);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown signal group type: " + typeStr);
        }
    }

    /**
     * Checks if this signal group is for diagonal left turn
     */
    public boolean isDiagonalLeftTurn() {
        return type == SignalGroupType.DN;
    }

    /**
     * Checks if this signal group is for bicycle traffic
     */
    public boolean isBicycleSignal() {
        return type == SignalGroupType.RD;
    }

    /**
     * Checks if this signal group controls exclusively left turn connections
     */
    public boolean controlsOnlyLeftTurns() {
        if (controlledConnections.isEmpty()) {
            return false;
        }

        for (Connection connection : controlledConnections) {
            if (!connection.isLeftTurn()) {
                return false;
            }
        }

        return true;
    }

    /**
     * Checks if this signal group controls conflict-free left turns
     * This is the case for diagonal green arrows and dedicated left turn signals
     */
    public boolean providesConflictFreeLeftTurn() {
        return isDiagonalLeftTurn() || controlsOnlyLeftTurns();
    }

    // Getters and Setters

    public int getPhysicalSignalGroupId() {
        return physicalSignalGroupId;
    }

    public void setPhysicalSignalGroupId(int physicalSignalGroupId) {
        this.physicalSignalGroupId = physicalSignalGroupId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SignalGroupType getType() {
        return type;
    }

    public void setType(SignalGroupType type) {
        this.type = type;
    }

    public List<Connection> getControlledConnections() {
        return controlledConnections;
    }

    public void addControlledConnection(Connection connection) {
        this.controlledConnections.add(connection);
        connection.setSignalGroup(this);
    }

    @Override
    public String toString() {
        return "SignalGroup{" +
                "physicalSignalGroupId=" + physicalSignalGroupId +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", connections=" + controlledConnections.size() +
                '}';
    }
}
package de.trafficvalidator.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a physical signal group from the STG file (German "Signalgruppe").
 * A physical signal group controls one or more connections at an intersection.
 * 
 * Note: This represents a PHYSICAL signal group (VT in MAPEM XML), not a logical DSRC:signalGroup.
 * In German traffic engineering, a physical signal group ("Signalgruppe") corresponds to one or 
 * more actual signal heads ("Signalgeber") that control traffic movements at an intersection.
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
        FG,  // Fußgänger (Pedestrian traffic)
        RA   // Rechtsabbiegepfeil (Right turn arrow signal)
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
     * Checks if this signal group is a right turn arrow
     */
    public boolean isAdditionalRightTurnArrow() {
        return type == SignalGroupType.RA;
    }

    /**
     * Checks if this signal group has directional arrows
     * This includes diagonal left turn (DN), right turn arrows (RA), 
     * and signal groups that exclusively control left turns
     */
    public boolean hasDirectionalArrows() {
        return isDiagonalLeftTurn() || isAdditionalRightTurnArrow() || controlsOnlyLeftTurns();
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

    /**
     * Checks if this signal group controls exclusively right turn connections
     */
    public boolean controlsOnlyRightTurns() {
        if (controlledConnections.isEmpty()) {
            return false;
        }

        for (Connection connection : controlledConnections) {
            if (!connection.isRightTurn()) {
                return false;
            }
        }

        return true;
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

    /**
     * Adds a controlled connection to this physical signal group
     * @param connection The connection controlled by this physical signal group
     */
    public void addControlledConnection(Connection connection) {
        if (connection != null && !controlledConnections.contains(connection)) {
            this.controlledConnections.add(connection);
            connection.addSignalGroup(this);
        }
    }

    /**
     * Returns the ID of this physical signal group (VT)
     * @return The physical signal group ID
     */
    public int getId() {
        return physicalSignalGroupId;
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
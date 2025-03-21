package de.trafficvalidator.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a traffic stream from the MAPEM file.
 * A traffic stream links lanes through a connection and associates them with
 * physical signal groups (German "Signalgruppe") via the VT ID.
 */
public class TrafficStream {
    private int refLaneId;               // Reference lane ID from MAPEM
    private int refConnectTo;            // Connected lane ID from MAPEM
    private int intersectionPart;        // Intersection part from MAPEM
    private int physicalSignalGroupId;   // VT value from MAPEM - links to STG physical signal group
    private boolean isPrimary;           // Primary or secondary from MAPEM

    private Lane refLane;                // Resolved reference to ingress lane
    private Lane connectToLane;          // Resolved reference to egress lane
    private Connection connection;       // Resolved reference to connection (for backward compatibility)
    private List<Connection> connections = new ArrayList<>(); // List of all connections for this stream
    private SignalGroup signalGroup;     // Resolved reference to physical signal group

    public TrafficStream() {
    }

    /**
     * Links this traffic stream to the appropriate connection
     * @deprecated Use addConnection instead
     */
    @Deprecated
    public void linkToConnection(Connection connection) {
        this.connection = connection;
        
        // Also add to the list for new implementation
        if (connection != null) {
            addConnection(connection);
        }
    }

    /**
     * Adds a connection to this traffic stream
     * @param connection The connection to add
     */
    public void addConnection(Connection connection) {
        if (connection != null && !connections.contains(connection)) {
            connections.add(connection);
            
            // For backward compatibility
            if (this.connection == null) {
                this.connection = connection;
            }
            
            // Apply physical signal group ID to the connection
            connection.addPhysicalSignalGroupId(physicalSignalGroupId);
        }
    }

    /**
     * Gets all connections associated with this traffic stream
     * @return List of connections
     */
    public List<Connection> getConnections() {
        return new ArrayList<>(connections);
    }

    /**
     * Links this traffic stream to the appropriate physical signal group
     */
    public void linkToSignalGroup(SignalGroup signalGroup) {
        this.signalGroup = signalGroup;

        if (signalGroup != null) {
            // Link all connections to this signal group
            for (Connection conn : connections) {
                signalGroup.addControlledConnection(conn);
            }
            
            // For backward compatibility
            if (connection != null) {
                signalGroup.addControlledConnection(connection);
            }
        }
    }

    // Getters and Setters

    public int getRefLaneId() {
        return refLaneId;
    }

    public void setRefLaneId(int refLaneId) {
        this.refLaneId = refLaneId;
    }

    public int getRefConnectTo() {
        return refConnectTo;
    }

    public void setRefConnectTo(int refConnectTo) {
        this.refConnectTo = refConnectTo;
    }

    public int getIntersectionPart() {
        return intersectionPart;
    }

    public void setIntersectionPart(int intersectionPart) {
        this.intersectionPart = intersectionPart;
    }

    public int getPhysicalSignalGroupId() {
        return physicalSignalGroupId;
    }

    public void setPhysicalSignalGroupId(int physicalSignalGroupId) {
        this.physicalSignalGroupId = physicalSignalGroupId;
    }

    public boolean isPrimary() {
        return isPrimary;
    }

    public void setPrimary(boolean primary) {
        isPrimary = primary;
    }

    public Lane getRefLane() {
        return refLane;
    }

    public void setRefLane(Lane refLane) {
        this.refLane = refLane;
    }

    public Lane getConnectToLane() {
        return connectToLane;
    }

    public void setConnectToLane(Lane connectToLane) {
        this.connectToLane = connectToLane;
    }

    /**
     * @return The primary connection (for backward compatibility)
     * @deprecated Use getConnections() instead to get all connections
     */
    @Deprecated
    public Connection getConnection() {
        return connection;
    }

    /**
     * @param connection The connection to set as primary
     * @deprecated Use addConnection() instead
     */
    @Deprecated
    public void setConnection(Connection connection) {
        this.connection = connection;
        
        // Also add to the list for new implementation
        if (connection != null) {
            addConnection(connection);
        }
    }

    public SignalGroup getSignalGroup() {
        return signalGroup;
    }

    /**
     * @deprecated Use linkToSignalGroup() instead to ensure logical signal group ID is properly maintained
     */
    @Deprecated
    public void setSignalGroup(SignalGroup signalGroup) {
        this.signalGroup = signalGroup;
    }

    @Override
    public String toString() {
        return "TrafficStream{" +
                "refLaneId=" + refLaneId +
                ", refConnectTo=" + refConnectTo +
                ", physicalSignalGroupId=" + physicalSignalGroupId +
                ", isPrimary=" + isPrimary +
                ", connections=" + connections.size() +
                '}';
    }
}
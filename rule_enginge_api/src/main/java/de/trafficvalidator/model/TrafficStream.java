package de.trafficvalidator.model;

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
    private Connection connection;       // Resolved reference to connection
    private SignalGroup signalGroup;     // Resolved reference to physical signal group

    public TrafficStream() {
    }

    /**
     * Links this traffic stream to the appropriate connection
     */
    public void linkToConnection(Connection connection) {
        this.connection = connection;

        if (connection != null) {
            connection.setPhysicalSignalGroupId(physicalSignalGroupId);
        }
    }

    /**
     * Links this traffic stream to the appropriate physical signal group
     */
    public void linkToSignalGroup(SignalGroup signalGroup) {
        this.signalGroup = signalGroup;

        if (connection != null && signalGroup != null) {
            signalGroup.addControlledConnection(connection);
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

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
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
                '}';
    }
}
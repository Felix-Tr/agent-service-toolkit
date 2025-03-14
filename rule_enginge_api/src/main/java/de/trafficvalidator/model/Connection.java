package de.trafficvalidator.model;

/**
 * Represents a connection between an ingress and egress lane.
 * Connections are controlled by signal groups and define possible maneuvers.
 */
public class Connection {
    /**
     * Enum representing the type of maneuver for this connection
     */
    public enum ManeuverType {
        STRAIGHT,
        LEFT_TURN,
        RIGHT_TURN,
        U_TURN,
        UNKNOWN
    }

    private int id;
    private Lane ingressLane;
    private Lane egressLane;
    private SignalGroup signalGroup;             // Reference to physical signal group
    private int connectionId;                    // From MAPEM (connectionID)
    private int physicalSignalGroupId;           // VT from TrafficStream
    private ManeuverType maneuverType = ManeuverType.UNKNOWN; // Type of maneuver

    // Maneuver types decoded from DSRC:maneuver binary string
    private boolean maneuverStraightAllowed;     // Bit 0
    private boolean maneuverLeftAllowed;         // Bit 1
    private boolean maneuverRightAllowed;        // Bit 2
    private boolean maneuverUTurnAllowed;        // Bit 3
    private boolean maneuverLeftTurnOnRedAllowed;  // Bit 4
    private boolean maneuverRightTurnOnRedAllowed; // Bit 5
    private boolean maneuverLaneChangeAllowed;     // Bit 6
    private boolean maneuverNoStoppingAllowed;     // Bit 7
    private boolean yieldAllwaysRequired;          // Bit 8
    private boolean goWithHalt;                    // Bit 9
    private boolean caution;                       // Bit 10

    // Connection-specific overrides of lane permissions
    private boolean allowsCyclists;

    /**
     * Creates a connection between ingress and egress lanes
     */
    public Connection(Lane ingressLane, Lane egressLane) {
        this.ingressLane = ingressLane;
        this.egressLane = egressLane;

        // Link connection to lanes
        if (ingressLane != null) {
            ingressLane.addOutgoingConnection(this);
        }

        if (egressLane != null) {
            egressLane.addIncomingConnection(this);
        }

    }

    /**
     * Sets maneuver flags from binary string as defined in DSRC specification
     * AllowedManeuvers ::= BIT STRING
     */
    public void setManeuversFromBinary(String maneuvers) {
        if (maneuvers == null || maneuvers.isEmpty()) {
            return;
        }

        // Remove any spaces or underscores
        maneuvers = maneuvers.replaceAll("[\\s_]", "");

        // Check each bit position (as described in the standard)
        if (maneuvers.length() >= 1 && maneuvers.charAt(0) == '1') {
            maneuverStraightAllowed = true;
            maneuverType = ManeuverType.STRAIGHT;
        }

        if (maneuvers.length() >= 2 && maneuvers.charAt(1) == '1') {
            maneuverLeftAllowed = true;
            maneuverType = ManeuverType.LEFT_TURN;
        }

        if (maneuvers.length() >= 3 && maneuvers.charAt(2) == '1') {
            maneuverRightAllowed = true;
            maneuverType = ManeuverType.RIGHT_TURN;
        }

        if (maneuvers.length() >= 4 && maneuvers.charAt(3) == '1') {
            maneuverUTurnAllowed = true;
            maneuverType = ManeuverType.U_TURN;
        }

        // Optional: Parse additional maneuver information
        if (maneuvers.length() >= 5 && maneuvers.charAt(4) == '1') {
            maneuverLeftTurnOnRedAllowed = true;
        }

        if (maneuvers.length() >= 6 && maneuvers.charAt(5) == '1') {
            maneuverRightTurnOnRedAllowed = true;
        }

        if (maneuvers.length() >= 7 && maneuvers.charAt(6) == '1') {
            maneuverLaneChangeAllowed = true;
        }

        if (maneuvers.length() >= 8 && maneuvers.charAt(7) == '1') {
            maneuverNoStoppingAllowed = true;
        }

        if (maneuvers.length() >= 9 && maneuvers.charAt(8) == '1') {
            yieldAllwaysRequired = true;
        }

        if (maneuvers.length() >= 10 && maneuvers.charAt(9) == '1') {
            goWithHalt = true;
        }

        if (maneuvers.length() >= 11 && maneuvers.charAt(10) == '1') {
            caution = true;
        }
    }

    /**
     * Determines if this connection represents a left turn
     */
    public boolean isLeftTurn() {
        return maneuverLeftAllowed;
    }

    /**
     * Determines if this connection represents a right turn
     */
    public boolean isRightTurn() {
        return maneuverRightAllowed;
    }

    /**
     * Determines if this connection represents a straight path
     */
    public boolean isStraight() {
        return maneuverStraightAllowed;
    }

    /**
     * Determines if this connection represents a U-turn
     */
    public boolean isUTurn() {
        return maneuverUTurnAllowed;
    }

    /**
     * Determines if this connection represents a right turn for cyclists
     */
    public boolean isCyclistRightTurn() {
        return isRightTurn() && allowsCyclists &&
                ingressLane != null && ingressLane.allowsCyclists();
    }

    /**
     * Determines if this connection represents a left turn from opposite direction
     * that would conflict with cyclists turning right
     */
    public boolean isConflictingLeftTurn(Connection cyclistRightTurn) {
        // Must be a left turn
        if (!isLeftTurn()) {
            return false;
        }

        // Must have signal group that provides conflict-free left turn
        if (signalGroup == null || !signalGroup.providesConflictFreeLeftTurn()) {
            return false;
        }

        // Must have opposite ingress direction to the cyclist's lane
        if (ingressLane == null || ingressLane.getDirection() == null ||
                cyclistRightTurn == null || cyclistRightTurn.getIngressLane() == null ||
                cyclistRightTurn.getIngressLane().getDirection() == null) {
            return false;
        }

        if (ingressLane.getDirection().getOpposite() != cyclistRightTurn.getIngressLane().getDirection()) {
            return false;
        }

        // Must target the same egress lane as cyclist's right turn or a conflicting path
        return egressLane != null && egressLane == cyclistRightTurn.getEgressLane();
    }

    // Getters and Setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Lane getIngressLane() {
        return ingressLane;
    }

    public void setIngressLane(Lane ingressLane) {
        this.ingressLane = ingressLane;
    }

    public Lane getEgressLane() {
        return egressLane;
    }

    public void setEgressLane(Lane egressLane) {
        this.egressLane = egressLane;
    }

    public SignalGroup getSignalGroup() {
        return signalGroup;
    }

    public void setSignalGroup(SignalGroup signalGroup) {
        this.signalGroup = signalGroup;
    }

    public int getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(int connectionId) {
        this.connectionId = connectionId;
    }

    public int getPhysicalSignalGroupId() {
        return physicalSignalGroupId;
    }

    public void setPhysicalSignalGroupId(int physicalSignalGroupId) {
        this.physicalSignalGroupId = physicalSignalGroupId;
    }

    /**
     * @deprecated Use setManeuversFromBinary or isLeftTurn() instead
     */
    @Deprecated
    public void setLeftTurn(boolean leftTurn) {
        maneuverLeftAllowed = leftTurn;
        if (leftTurn) {
            maneuverType = ManeuverType.LEFT_TURN;
        }
    }

    /**
     * @deprecated Use setManeuversFromBinary or isStraight() instead
     */
    @Deprecated
    public void setStraight(boolean straight) {
        maneuverStraightAllowed = straight;
        if (straight) {
            maneuverType = ManeuverType.STRAIGHT;
        }
    }

    /**
     * @deprecated Use setManeuversFromBinary or isRightTurn() instead
     */
    @Deprecated
    public void setRightTurn(boolean rightTurn) {
        maneuverRightAllowed = rightTurn;
        if (rightTurn) {
            maneuverType = ManeuverType.RIGHT_TURN;
        }
    }

    /**
     * @deprecated Use setManeuversFromBinary or isUTurn() instead
     */
    @Deprecated
    public void setUTurn(boolean uTurn) {
        maneuverUTurnAllowed = uTurn;
        if (uTurn) {
            maneuverType = ManeuverType.U_TURN;
        }
    }

    public boolean allowsCyclists() {
        return allowsCyclists;
    }

    public void setAllowsCyclists(boolean allowsCyclists) {
        this.allowsCyclists = allowsCyclists;
    }

    public boolean isManeuverLeftTurnOnRedAllowed() {
        return maneuverLeftTurnOnRedAllowed;
    }

    public boolean isManeuverRightTurnOnRedAllowed() {
        return maneuverRightTurnOnRedAllowed;
    }

    public boolean isManeuverLaneChangeAllowed() {
        return maneuverLaneChangeAllowed;
    }

    public boolean isManeuverNoStoppingAllowed() {
        return maneuverNoStoppingAllowed;
    }

    public boolean isYieldAllwaysRequired() {
        return yieldAllwaysRequired;
    }

    public boolean isGoWithHalt() {
        return goWithHalt;
    }

    public boolean isCaution() {
        return caution;
    }

    /**
     * Gets the maneuver type for this connection
     */
    public ManeuverType getManeuverType() {
        return maneuverType;
    }

    /**
     * Sets the maneuver type for this connection
     */
    public void setManeuverType(ManeuverType maneuverType) {
        this.maneuverType = maneuverType;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Connection{id=").append(id);

        if (ingressLane != null) {
            sb.append(", ingress=").append(ingressLane.getId());
        }

        if (egressLane != null) {
            sb.append(", egress=").append(egressLane.getId());
        }

        sb.append(", physicalSG=").append(physicalSignalGroupId);
        sb.append(", maneuverType=").append(maneuverType);

        sb.append(", maneuvers=[");
        if (maneuverStraightAllowed) sb.append("Straight ");
        if (maneuverLeftAllowed) sb.append("Left ");
        if (maneuverRightAllowed) sb.append("Right ");
        if (maneuverUTurnAllowed) sb.append("UTurn ");
        sb.append("]");

        return sb.toString() + "}";
    }
}
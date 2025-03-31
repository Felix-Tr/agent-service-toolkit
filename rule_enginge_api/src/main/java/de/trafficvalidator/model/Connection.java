package de.trafficvalidator.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a connection between an ingress and egress lane.
 * Connections are controlled by signal groups and define possible maneuvers.
 */
public class Connection {
    public int getLogicalSignalGroupId() {
        return logicalSignalGroupId;
    }

    public void setLogicalSignalGroupId(int logicalSignalGroupId) {
        this.logicalSignalGroupId = logicalSignalGroupId;
    }

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
    private SignalGroup signalGroup;             // Reference to physical signal group (German "Signalgruppe", VT)
    private List<SignalGroup> signalGroups = new ArrayList<>();  // Multiple physical signal groups
    private int connectionId;                    // From MAPEM (connectionID)
    private int physicalSignalGroupId;           // VT from TrafficStream (physical signal group)
    private int logicalSignalGroupId;           // VT from TrafficStream (physical signal group)
    private List<Integer> physicalSignalGroupIds = new ArrayList<>();  // Multiple physical signal group IDs (VT)
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
        return isRightTurn() && allowsCyclists();
    }

    /**
     * Determines if this connection allows cyclists
     * Calculates the intersection of allowed traffic types between ingress and egress lanes
     * and considers any connection-specific overrides
     */
    public boolean allowsCyclists() {
        // Both ingress and egress lanes must allow cyclists
        return ingressLane != null && ingressLane.allowsCyclists() && 
               egressLane != null && egressLane.allowsCyclists();
    }
    
    /**
     * Determines if this connection allows pedestrians
     * Calculates the intersection of allowed traffic types
     */
    public boolean allowsPedestrians() {
        return ingressLane != null && ingressLane.allowsPedestrians() && 
               egressLane != null && egressLane.allowsPedestrians();
    }
    
    /**
     * Determines if this connection allows individual motorized vehicles
     * Calculates the intersection of allowed traffic types
     */
    public boolean allowsIndividualMotorizedVehicles() {
        return ingressLane != null && ingressLane.allowsIndividualMotorizedVehicles() && 
               egressLane != null && egressLane.allowsIndividualMotorizedVehicles();
    }
    
    /**
     * Determines if this connection allows public transport
     * Calculates the intersection of allowed traffic types
     */
    public boolean allowsPublicTransport() {
        return ingressLane != null && ingressLane.allowsPublicTransport() && 
               egressLane != null && egressLane.allowsPublicTransport();
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

    /**
     * Gets the primary physical signal group (German "Signalgruppe") controlling this connection
     * @return The primary physical signal group (VT)
     */
    public SignalGroup getSignalGroup() {
        return signalGroup;
    }

    /**
     * Sets the primary physical signal group (German "Signalgruppe") controlling this connection
     * @param signalGroup The physical signal group (VT)
     */
    public void setSignalGroup(SignalGroup signalGroup) {
        this.signalGroup = signalGroup;
    }

    public int getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(int connectionId) {
        this.connectionId = connectionId;
    }

    /**
     * Gets the primary physical signal group ID (VT) for this connection
     */
    public int getPhysicalSignalGroupId() {
        return physicalSignalGroupId;
    }

    /**
     * Sets the primary physical signal group ID (VT) for this connection
     */
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

    /**
     * Gets the opposite direction of this connection's ingress lane
     * This is a simple accessor method suitable for a domain model
     */
    public Direction getOppositeIngressDirection() {
        if (ingressLane == null || ingressLane.getDirection() == null) {
            return null;
        }
        return ingressLane.getDirection().getOpposite();
    }

    /**
     * Checks if this connection targets the same egress lane as the specified connection
     * This is a simple comparison method suitable for a domain model
     */
    public boolean sharesEgressLaneWith(Connection other) {
        return egressLane != null && other != null && 
               other.getEgressLane() != null && 
               egressLane.equals(other.getEgressLane());
    }

    /**
     * Checks if this connection comes from the opposite direction as the specified connection
     * This is a simple comparison method suitable for a domain model
     */
    public boolean isFromOppositeDirectionOf(Connection other) {
        if (ingressLane == null || ingressLane.getDirection() == null ||
            other == null || other.getIngressLane() == null || 
            other.getIngressLane().getDirection() == null) {
            return false;
        }
        
        return ingressLane.getDirection().getOpposite() == 
               other.getIngressLane().getDirection();
    }

    /**
     * Checks if this connection represents a left turn that would conflict with
     * the given cyclist right turn. A left turn is conflicting if:
     * 1. It is a left turn
     * 2. It comes from the opposite direction of the cyclist right turn
     * 3. It shares the same egress lane as the cyclist right turn
     */
    public boolean isConflictingLeftTurn(Connection cyclistRightTurn) {
        return isLeftTurn() && 
               this != cyclistRightTurn && 
               isFromOppositeDirectionOf(cyclistRightTurn) && 
               sharesEgressLaneWith(cyclistRightTurn);
    }

    /**
     * Adds a physical signal group ID (VT) to this connection
     * @param physicalSignalGroupId The physical signal group ID from the MAPEM file
     */
    public void addPhysicalSignalGroupId(int physicalSignalGroupId) {
        // Set the primary one for backward compatibility
        if (this.physicalSignalGroupId == 0) {
            this.physicalSignalGroupId = physicalSignalGroupId;
        }
        
        // Add to list if not already present
        if (!physicalSignalGroupIds.contains(physicalSignalGroupId)) {
            physicalSignalGroupIds.add(physicalSignalGroupId);
        }
    }

    /**
     * Gets the list of all physical signal group IDs (VT) for this connection
     * @return List of physical signal group IDs
     */
    public List<Integer> getPhysicalSignalGroupIds() {
        return new ArrayList<>(physicalSignalGroupIds);
    }

    /**
     * Returns whether this connection has a specific physical signal group ID (VT)
     * @param physicalSignalGroupId The physical signal group ID to check
     * @return true if this connection has the specified physical signal group ID
     */
    public boolean hasPhysicalSignalGroupId(int physicalSignalGroupId) {
        return this.physicalSignalGroupIds.contains(physicalSignalGroupId);
    }

    /**
     * Adds a physical signal group (German "Signalgruppe") to this connection
     * @param signalGroup The physical signal group to add
     */
    public void addSignalGroup(SignalGroup signalGroup) {
        // Set the primary one for backward compatibility
        if (this.signalGroup == null) {
            this.signalGroup = signalGroup;
        }
        
        // Add to list if not already present and not null
        if (signalGroup != null && !signalGroups.contains(signalGroup)) {
            signalGroups.add(signalGroup);
        }
    }

    /**
     * Gets all physical signal groups (German "Signalgruppe") for this connection
     * @return List of physical signal groups
     */
    public List<SignalGroup> getSignalGroups() {
        return new ArrayList<>(signalGroups);
    }

    /**
     * Checks if this connection has any signal groups assigned
     * @return true if the connection has at least one signal group, false otherwise
     */
    public boolean hasSignalGroups() {
        return signalGroups != null && !signalGroups.isEmpty();
    }

    /**
     * Returns a string representation of all signal groups associated with this connection
     * @return A string listing the signal groups with their properties
     */
    public String getConnectedSignalGroups() {
        if (signalGroups == null || signalGroups.isEmpty()) {
            return "No signal groups";
        }
        
        return signalGroups.stream()
            .map(sg -> String.format("%s (type=%s, isDiagonal=%s, isRightTurnArrow=%s, isLeftTurnFull=%s)",
                sg.getName(),
                sg.getType(),
                sg.isDiagonalLeftTurn(),
                sg.isAdditionalRightTurnArrow(),
                sg.istLinksabbiegerVollscheibe()))
            .collect(Collectors.joining(", "));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Connection{id=").append(id)
          .append(", connectionId=").append(connectionId);
        
        if (ingressLane != null && egressLane != null) {
            sb.append(", lanes=").append(ingressLane.getId())
              .append("->").append(egressLane.getId());
        }
        
        if (!signalGroups.isEmpty()) {
            sb.append(", signalGroups=[");
            for (int i = 0; i < signalGroups.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(signalGroups.get(i).getPhysicalSignalGroupId());
            }
            sb.append("]");
        }
        
        if (maneuverType != ManeuverType.UNKNOWN) {
            sb.append(", maneuver=").append(maneuverType);
        }
        
        sb.append('}');
        return sb.toString();
    }
}
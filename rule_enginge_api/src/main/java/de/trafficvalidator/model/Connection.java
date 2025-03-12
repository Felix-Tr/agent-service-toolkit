package de.trafficvalidator.model;

/**
 * Represents a connection between an ingress and egress lane.
 * Connections are controlled by signal groups and define possible maneuvers.
 */
public class Connection {
    private int id;
    private Lane ingressLane;
    private Lane egressLane;
    private SignalGroup signalGroup;
    private int connectionId; // From MAPEM (connectionID)
    private int vtId;         // From MAPEM traffic streams (vt)
    
    // Maneuver types from MAPEM
    private boolean isLeftTurn;
    private boolean isStraight;
    private boolean isRightTurn;
    private boolean isUTurn;
    
    // Connection-specific overrides of lane permissions
    private boolean allowsCyclists;
    
    /**
     * Creates a connection between ingress and egress lanes
     * 
     * TODO: Review if this is implemented in a suitable way using the cardinal directions 
     * here instead of relative right, left, straight turn configuration from MAPEM.
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
        
        // Set maneuver types based on directions if available
        if (ingressLane != null && egressLane != null && 
            ingressLane.getDirection() != null && egressLane.getDirection() != null) {
            
            Direction ingressDir = ingressLane.getDirection();
            Direction egressDir = egressLane.getDirection();
            
            isLeftTurn = (ingressDir.getLeftTurn() == egressDir);
            isRightTurn = (ingressDir.getRightTurn() == egressDir);
            isStraight = (ingressDir == egressDir);
            isUTurn = (ingressDir.getOpposite() == egressDir);
        }
    }
    
    /**
     * Determines if this connection represents a right turn for cyclists
     */
    public boolean isCyclistRightTurn() {
        return isRightTurn && allowsCyclists && 
               ingressLane != null && ingressLane.allowsCyclists();
    }
    
    /**
     * Determines if this connection represents a left turn from opposite direction
     * that would conflict with cyclists turning right
     */
    public boolean isConflictingLeftTurn(Connection cyclistRightTurn) {
        // Must be a left turn
        if (!isLeftTurn) {
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
        
        // Must target the same egress lane as cyclist's right turn
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

    public int getVtId() {
        return vtId;
    }

    public void setVtId(int vtId) {
        this.vtId = vtId;
    }

    public boolean isLeftTurn() {
        return isLeftTurn;
    }

    public void setLeftTurn(boolean leftTurn) {
        isLeftTurn = leftTurn;
    }

    public boolean isStraight() {
        return isStraight;
    }

    public void setStraight(boolean straight) {
        isStraight = straight;
    }

    public boolean isRightTurn() {
        return isRightTurn;
    }

    public void setRightTurn(boolean rightTurn) {
        isRightTurn = rightTurn;
    }

    public boolean isUTurn() {
        return isUTurn;
    }

    public void setUTurn(boolean uTurn) {
        isUTurn = uTurn;
    }

    public boolean allowsCyclists() {
        return allowsCyclists;
    }

    public void setAllowsCyclists(boolean allowsCyclists) {
        this.allowsCyclists = allowsCyclists;
    }
    
    @Override
    public String toString() {
        return "Connection{" +
                "id=" + id +
                ", ingressLane=" + (ingressLane != null ? ingressLane.getId() : "null") +
                ", egressLane=" + (egressLane != null ? egressLane.getId() : "null") +
                ", signalGroup=" + (signalGroup != null ? signalGroup.getName() : "null") +
                ", isRightTurn=" + isRightTurn +
                '}';
    }
}
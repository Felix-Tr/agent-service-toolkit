package de.trafficvalidator.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Represents a lane at an intersection.
 * A lane can be either an ingress (approaching an intersection) or egress (leaving an intersection).
 */
public class Lane {
    private int id;
    private String name;
    private boolean isIngress;
    private boolean isEgress;
    private int approachId;
    private Direction direction;
    
    // Lane type attributes
    private boolean isVehicleLane;
    private boolean isBikeLane;
    private boolean isCrosswalk;
    
    // Shared with attributes (traffic types allowed on this lane)
    private boolean allowsIndividualMotorizedVehicles;  // Cars
    private boolean allowsCyclists;                     // Bicycles
    private boolean allowsPedestrians;                  // Pedestrians
    private boolean allowsPublicTransport;              // Buses, trams
    
    // Lists of connections to/from this lane
    private List<Connection> incomingConnections = new ArrayList<>();
    private List<Connection> outgoingConnections = new ArrayList<>();
    
    // Coordinates for direction calculation
    private List<NodePoint> nodeList = new ArrayList<>();
    
    public Lane(int id) {
        this.id = id;
    }
    
    /**
     * Helper class to represent a node point in a lane
     */
    public static class NodePoint {
        private double x;
        private double y;
        
        public NodePoint(double x, double y) {
            this.x = x;
            this.y = y;
        }
        
        public double getX() {
            return x;
        }
        
        public double getY() {
            return y;
        }
    }

    // Getters and Setters
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isIngress() {
        return isIngress;
    }

    public void setIngress(boolean ingress) {
        isIngress = ingress;
    }

    public boolean isEgress() {
        return isEgress;
    }

    public void setEgress(boolean egress) {
        isEgress = egress;
    }

    public int getApproachId() {
        return approachId;
    }

    public void setApproachId(int approachId) {
        this.approachId = approachId;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public boolean isVehicleLane() {
        return isVehicleLane;
    }

    public void setVehicleLane(boolean vehicleLane) {
        isVehicleLane = vehicleLane;
    }

    public boolean isBikeLane() {
        return isBikeLane;
    }

    public void setBikeLane(boolean bikeLane) {
        isBikeLane = bikeLane;
    }

    public boolean isCrosswalk() {
        return isCrosswalk;
    }

    public void setCrosswalk(boolean crosswalk) {
        isCrosswalk = crosswalk;
    }

    public boolean allowsIndividualMotorizedVehicles() {
        return allowsIndividualMotorizedVehicles;
    }

    public void setAllowsIndividualMotorizedVehicles(boolean allowsIndividualMotorizedVehicles) {
        this.allowsIndividualMotorizedVehicles = allowsIndividualMotorizedVehicles;
    }

    public boolean allowsCyclists() {
        return allowsCyclists;
    }

    public void setAllowsCyclists(boolean allowsCyclists) {
        this.allowsCyclists = allowsCyclists;
    }

    public boolean allowsPedestrians() {
        return allowsPedestrians;
    }

    public void setAllowsPedestrians(boolean allowsPedestrians) {
        this.allowsPedestrians = allowsPedestrians;
    }

    public boolean allowsPublicTransport() {
        return allowsPublicTransport;
    }

    public void setAllowsPublicTransport(boolean allowsPublicTransport) {
        this.allowsPublicTransport = allowsPublicTransport;
    }

    public List<Connection> getIncomingConnections() {
        return incomingConnections;
    }

    public void addIncomingConnection(Connection connection) {
        this.incomingConnections.add(connection);
    }

    public List<Connection> getOutgoingConnections() {
        return outgoingConnections;
    }

    public void addOutgoingConnection(Connection connection) {
        this.outgoingConnections.add(connection);
    }

    public List<NodePoint> getNodeList() {
        return nodeList;
    }

    public void addNode(double x, double y) {
        this.nodeList.add(new NodePoint(x, y));
    }
    
    /**
     * Returns first node coordinates (useful for direction calculations)
     */
    public NodePoint getFirstNode() {
        if (nodeList.isEmpty()) {
            return null;
        }
        return nodeList.get(0);
    }
    
    /**
     * Returns last node coordinates (useful for direction calculations)
     */
    public NodePoint getLastNode() {
        if (nodeList.isEmpty()) {
            return null;
        }
        return nodeList.get(nodeList.size() - 1);
    }
    
    /**
     * Gets outgoing connections for right turns only
     */
    public List<Connection> getRightTurnConnections() {
        List<Connection> rightTurns = new ArrayList<>();
        
        if (direction == null) {
            return rightTurns; // Can't determine without direction
        }
        
        Direction rightDirection = direction.getRightTurn();
        
        for (Connection conn : outgoingConnections) {
            Lane targetLane = conn.getEgressLane();
            if (targetLane.getDirection() == rightDirection) {
                rightTurns.add(conn);
            }
        }
        
        return rightTurns;
    }
    
    /**
     * Gets outgoing connections for left turns only
     */
    public List<Connection> getLeftTurnConnections() {
        List<Connection> leftTurns = new ArrayList<>();
        
        if (direction == null) {
            return leftTurns; // Can't determine without direction
        }
        
        Direction leftDirection = direction.getLeftTurn();
        
        for (Connection conn : outgoingConnections) {
            Lane targetLane = conn.getEgressLane();
            if (targetLane.getDirection() == leftDirection) {
                leftTurns.add(conn);
            }
        }
        
        return leftTurns;
    }
    
    @Override
    public String toString() {
        return "Lane{" +
                "id=" + id +
                ", direction=" + direction +
                ", isIngress=" + isIngress +
                ", isEgress=" + isEgress +
                '}';
    }
}
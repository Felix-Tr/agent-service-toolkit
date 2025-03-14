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
    private Direction cardinalDirection;  // Cardinal direction (N, S, E, W, etc.) based on coordinates

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

    // Add these fields to the Lane class
    private double stopLineX;
    private double stopLineY;
    private boolean hasStopLine;

    public Lane(int id) {
        this.id = id;
    }

    /**
     * Helper class to represent a node point in a lane
     */
    public static class NodePoint {
        private double x;
        private double y;
        private boolean isStopLine; // Added flag to mark stop line positions

        public NodePoint(double x, double y) {
            this.x = x;
            this.y = y;
            this.isStopLine = false;
        }

        public NodePoint(double x, double y, boolean isStopLine) {
            this.x = x;
            this.y = y;
            this.isStopLine = isStopLine;
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }
        
        public boolean isStopLine() {
            return isStopLine;
        }
        
        public void setStopLine(boolean stopLine) {
            isStopLine = stopLine;
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

    public Direction getCardinalDirection() {
        return cardinalDirection;
    }

    /**
     * Alias for getCardinalDirection() to maintain compatibility with existing code
     * @return the cardinal direction of the lane
     */
    public Direction getDirection() {
        return cardinalDirection;
    }

    public void setCardinalDirection(Direction cardinalDirection) {
        this.cardinalDirection = cardinalDirection;
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

    public void addNode(double x, double y, boolean isStopLine) {
        this.nodeList.add(new NodePoint(x, y, isStopLine));
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
     * Gets outgoing connections based on maneuver type (right turn)
     */
    public List<Connection> getRightTurnConnections() {
        List<Connection> rightTurns = new ArrayList<>();

        for (Connection conn : outgoingConnections) {
            if (conn.isRightTurn()) {
                rightTurns.add(conn);
            }
        }

        return rightTurns;
    }

    /**
     * Gets outgoing connections based on maneuver type (left turn)
     */
    public List<Connection> getLeftTurnConnections() {
        List<Connection> leftTurns = new ArrayList<>();

        for (Connection conn : outgoingConnections) {
            if (conn.isLeftTurn()) {
                leftTurns.add(conn);
            }
        }

        return leftTurns;
    }

    /**
     * Gets outgoing connections based on maneuver type (straight)
     */
    public List<Connection> getStraightConnections() {
        List<Connection> straightConns = new ArrayList<>();

        for (Connection conn : outgoingConnections) {
            if (conn.isStraight()) {
                straightConns.add(conn);
            }
        }

        return straightConns;
    }

    /**
     * Gets connections to a specific egress lane
     */
    public Connection getConnectionToLane(Lane egressLane) {
        for (Connection conn : outgoingConnections) {
            if (conn.getEgressLane() == egressLane) {
                return conn;
            }
        }
        return null;
    }

    /**
     * Gets connections from a specific ingress lane
     */
    public Connection getConnectionFromLane(Lane ingressLane) {
        for (Connection conn : incomingConnections) {
            if (conn.getIngressLane() == ingressLane) {
                return conn;
            }
        }
        return null;
    }

    /**
     * Gets connections associated with a specific physical signal group ID (vt)
     */
    public List<Connection> getConnectionsByPhysicalSignalGroupId(int physicalSignalGroupId) {
        List<Connection> result = new ArrayList<>();

        for (Connection conn : outgoingConnections) {
            if (conn.getPhysicalSignalGroupId() == physicalSignalGroupId) {
                result.add(conn);
            }
        }

        return result;
    }

    /**
     * Gets the stop line node if it exists
     */
    public NodePoint getStopLineNode() {
        for (NodePoint node : nodeList) {
            if (node.isStopLine()) {
                return node;
            }
        }
        return null;
    }

    // Add these methods to the Lane class
    public void setStopLinePosition(double x, double y) {
        this.stopLineX = x;
        this.stopLineY = y;
        this.hasStopLine = true;
    }

    public boolean hasStopLine() {
        return hasStopLine;
    }

    public double getStopLineX() {
        return stopLineX;
    }

    public double getStopLineY() {
        return stopLineY;
    }

    @Override
    public String toString() {
        // Calculate maneuver summary from outgoing connections
        int rightTurns = 0;
        int leftTurns = 0;
        int straightPaths = 0;
        int uTurns = 0;
        Set<Integer> signalGroupIds = new HashSet<>();
        
        for (Connection conn : outgoingConnections) {
            if (conn.isRightTurn()) rightTurns++;
            if (conn.isLeftTurn()) leftTurns++;
            if (conn.isStraight()) straightPaths++;
            if (conn.isUTurn()) uTurns++;
            
            if (conn.getPhysicalSignalGroupId() > 0) {
                signalGroupIds.add(conn.getPhysicalSignalGroupId());
            }
        }
        
        String maneuverSummary = "";
        if (!outgoingConnections.isEmpty()) {
            maneuverSummary = ", maneuvers=[" +
                    (rightTurns > 0 ? "Right:" + rightTurns + " " : "") +
                    (leftTurns > 0 ? "Left:" + leftTurns + " " : "") +
                    (straightPaths > 0 ? "Straight:" + straightPaths + " " : "") +
                    (uTurns > 0 ? "UTurn:" + uTurns + " " : "") +
                    "], signalGroups=" + signalGroupIds;
        }

        return "Lane{" +
                "id=" + id +
                ", direction=" + cardinalDirection +
                ", isIngress=" + isIngress +
                ", isEgress=" + isEgress +
                (outgoingConnections.isEmpty() ? "" : ", outConns=" + outgoingConnections.size() + maneuverSummary) +
                '}';
    }
}
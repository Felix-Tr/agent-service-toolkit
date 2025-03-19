package de.trafficvalidator.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Represents an intersection from the MAPEM file, containing lanes, connections and signal groups.
 */
public class Intersection {
    private int id;
    private int regionId;
    private String name;
    private int revision;

    // Reference point (from MAPEM file)
    private double refLat;
    private double refLong;
    
    // Calculated center (based on stop line positions)
    private double centerX;
    private double centerY;
    private boolean hasCenterCalculated = false;

    // Collections of elements
    private Map<Integer, Lane> lanes = new HashMap<>();
    private Map<Integer, SignalGroup> physicalSignalGroups = new HashMap<>(); // vt
    private List<Connection> connections = new ArrayList<>();
    private List<TrafficStream> trafficStreams = new ArrayList<>();

    public Intersection(int id, int regionId) {
        this.id = id;
        this.regionId = regionId;
    }

    /**
     * Sets the calculated center of the intersection
     */
    public void setCalculatedCenter(double x, double y) {
        this.centerX = x;
        this.centerY = y;
        this.hasCenterCalculated = true;
    }

    /**
     * Gets the X coordinate of the calculated center
     */
    public double getCenterX() {
        return centerX;
    }

    /**
     * Gets the Y coordinate of the calculated center
     */
    public double getCenterY() {
        return centerY;
    }

    /**
     * Checks if the intersection has a calculated center
     */
    public boolean hasCenterCalculated() {
        return hasCenterCalculated;
    }

    /**
     * Finds all cyclist right-turn connections at this intersection
     */
    public List<Connection> getCyclistRightTurnConnections() {
        return connections.stream()
                .filter(Connection::isCyclistRightTurn)
                .collect(Collectors.toList());
    }


    /**
     * Gets all ingress lanes by cardinal direction
     */
    public Map<Direction, List<Lane>> getIngressLanesByDirection() {
        Map<Direction, List<Lane>> result = new HashMap<>();

        for (Lane lane : lanes.values()) {
            if (lane.isIngress() && lane.getCardinalDirection() != null) {
                result.computeIfAbsent(lane.getCardinalDirection(), k -> new ArrayList<>()).add(lane);
            }
        }

        return result;
    }

    /**
     * Gets all egress lanes by cardinal direction
     */
    public Map<Direction, List<Lane>> getEgressLanesByDirection() {
        Map<Direction, List<Lane>> result = new HashMap<>();

        for (Lane lane : lanes.values()) {
            if (lane.isEgress() && lane.getCardinalDirection() != null) {
                result.computeIfAbsent(lane.getCardinalDirection(), k -> new ArrayList<>()).add(lane);
            }
        }

        return result;
    }

    /**
     * Gets all connections controlled by a specific physical signal group ID (vt)
     */
    public List<Connection> getConnectionsByPhysicalSignalGroupId(int physicalSignalGroupId) {
        return connections.stream()
                .filter(conn -> conn.getPhysicalSignalGroupId() == physicalSignalGroupId)
                .collect(Collectors.toList());
    }

    /**
     * Gets connections by maneuver type
     */
    public List<Connection> getConnectionsByManeuverType(boolean rightTurn, boolean leftTurn, boolean straight) {
        return connections.stream()
                .filter(conn ->
                        (rightTurn && conn.isRightTurn()) ||
                                (leftTurn && conn.isLeftTurn()) ||
                                (straight && conn.isStraight()))
                .collect(Collectors.toList());
    }

    /**
     * Checks if a physical signal group contains any connections with conflict-free left turns
     */
    public boolean hasConflictFreeLeftTurn(int physicalSignalGroupId) {
        List<Connection> groupConnections = getConnectionsByPhysicalSignalGroupId(physicalSignalGroupId);

        // All connections in the group must be left turns for it to be a conflict-free left turn
        boolean allLeftTurns = !groupConnections.isEmpty() &&
                groupConnections.stream().allMatch(Connection::isLeftTurn);

        // Check if the signal group is a diagonal left turn type
        SignalGroup physicalGroup = getPhysicalSignalGroup(physicalSignalGroupId);
        boolean isDiagonalLeftTurn = physicalGroup != null &&
                physicalGroup.getType() == SignalGroup.SignalGroupType.DN;

        return allLeftTurns || isDiagonalLeftTurn;
    }

    // Getters and Setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getRegionId() {
        return regionId;
    }

    public void setRegionId(int regionId) {
        this.regionId = regionId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getRevision() {
        return revision;
    }

    public void setRevision(int revision) {
        this.revision = revision;
    }

    public double getRefLat() {
        return refLat;
    }

    public void setRefLat(double refLat) {
        this.refLat = refLat;
    }

    public double getRefLong() {
        return refLong;
    }

    public void setRefLong(double refLong) {
        this.refLong = refLong;
    }

    public Lane getLane(int laneId) {
        return lanes.get(laneId);
    }

    public void addLane(Lane lane) {
        this.lanes.put(lane.getId(), lane);
    }

    public Map<Integer, Lane> getLanes() {
        return lanes;
    }

    public SignalGroup getPhysicalSignalGroup(int physicalSignalGroupId) {
        return physicalSignalGroups.get(physicalSignalGroupId);
    }

    public void addPhysicalSignalGroup(SignalGroup signalGroup) {
        this.physicalSignalGroups.put(signalGroup.getPhysicalSignalGroupId(), signalGroup);
    }

    /**
     * Gets a signal group by ID (physical only)
     */
    public SignalGroup getSignalGroup(int id) {
        return physicalSignalGroups.get(id);
    }

    /**
     * Adds a signal group to the physical signal groups map
     */
    public void addSignalGroup(SignalGroup signalGroup) {
        physicalSignalGroups.put(signalGroup.getPhysicalSignalGroupId(), signalGroup);
    }

    /**
     * Gets all signal groups (physical only)
     */
    public Map<Integer, SignalGroup> getSignalGroups() {
        return physicalSignalGroups;
    }

    public Map<Integer, SignalGroup> getPhysicalSignalGroups() {
        return physicalSignalGroups;
    }

    public List<Connection> getConnections() {
        return connections;
    }

    public void addConnection(Connection connection) {
        this.connections.add(connection);
    }

    public List<TrafficStream> getTrafficStreams() {
        return trafficStreams;
    }

    public void addTrafficStream(TrafficStream trafficStream) {
        this.trafficStreams.add(trafficStream);
    }

    @Override
    public String toString() {
        return "Intersection{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", lanes=" + lanes.size() +
                ", connections=" + connections.size() +
                ", trafficStreams=" + trafficStreams.size() +
                '}';
    }
}
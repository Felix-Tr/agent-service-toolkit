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
    
    // Reference point (center of intersection)
    private double refLat;
    private double refLong;
    
    // Collections of elements
    private Map<Integer, Lane> lanes = new HashMap<>();
    private Map<Integer, SignalGroup> signalGroups = new HashMap<>();
    private List<Connection> connections = new ArrayList<>();
    private List<TrafficStream> trafficStreams = new ArrayList<>();
    
    public Intersection(int id, int regionId) {
        this.id = id;
        this.regionId = regionId;
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
     * Finds all connections with conflict-free left turn signals that would
     * conflict with the given cyclist right turn
     */
    public List<Connection> getConflictingLeftTurnConnections(Connection cyclistRightTurn) {
        return connections.stream()
                .filter(conn -> conn.isConflictingLeftTurn(cyclistRightTurn))
                .collect(Collectors.toList());
    }
    
    /**
     * Gets all ingress lanes by approach direction
     */
    public Map<Direction, List<Lane>> getIngressLanesByDirection() {
        Map<Direction, List<Lane>> result = new HashMap<>();
        
        for (Lane lane : lanes.values()) {
            if (lane.isIngress() && lane.getDirection() != null) {
                result.computeIfAbsent(lane.getDirection(), k -> new ArrayList<>()).add(lane);
            }
        }
        
        return result;
    }
    
    /**
     * Gets all egress lanes by approach direction
     */
    public Map<Direction, List<Lane>> getEgressLanesByDirection() {
        Map<Direction, List<Lane>> result = new HashMap<>();
        
        for (Lane lane : lanes.values()) {
            if (lane.isEgress() && lane.getDirection() != null) {
                result.computeIfAbsent(lane.getDirection(), k -> new ArrayList<>()).add(lane);
            }
        }
        
        return result;
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

    public SignalGroup getSignalGroup(int signalGroupId) {
        return signalGroups.get(signalGroupId);
    }

    public void addSignalGroup(SignalGroup signalGroup) {
        this.signalGroups.put(signalGroup.getId(), signalGroup);
    }

    public Map<Integer, SignalGroup> getSignalGroups() {
        return signalGroups;
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
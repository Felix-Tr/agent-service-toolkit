package de.trafficvalidator.util;

import de.trafficvalidator.model.Direction;
import de.trafficvalidator.model.Intersection;
import de.trafficvalidator.model.Lane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Utility class to calculate cardinal directions for lanes based on their coordinates.
 * Direction calculation considers the lane's position relative to the calculated intersection center.
 */
public class DirectionCalculator {
    private static final Logger logger = LoggerFactory.getLogger(DirectionCalculator.class);

    private final Intersection intersection;
    private double centerX;
    private double centerY;

    // Direction sector boundaries in degrees (8 directions)
    // These match the Python example exactly
    private static final double NORTH_BOUNDARY_1 = 337.5;
    private static final double NORTH_BOUNDARY_2 = 22.5;
    private static final double NORTHEAST_BOUNDARY = 67.5;
    private static final double EAST_BOUNDARY = 112.5;
    private static final double SOUTHEAST_BOUNDARY = 157.5;
    private static final double SOUTH_BOUNDARY = 202.5;
    private static final double SOUTHWEST_BOUNDARY = 247.5;
    private static final double WEST_BOUNDARY = 292.5;
    private static final double NORTHWEST_BOUNDARY = 337.5;

    public DirectionCalculator(Intersection intersection) {
        this.intersection = intersection;
    }

    /**
     * Calculates cardinal directions for all lanes in the intersection
     */
    public void calculateDirections() {
        logger.info("Calculating cardinal directions for lanes");

        // Calculate the center of the intersection based on stop line positions
        calculateIntersectionCenter();
        
        logger.info("Calculated intersection center: ({}, {})", centerX, centerY);

        // Process each lane
        for (Lane lane : intersection.getLanes().values()) {
            if (lane.getNodeList().isEmpty()) {
                logger.warn("Lane {} has no nodes, cannot calculate direction", lane.getId());
                continue;
            }

            // For ingress lanes, calculate direction based on first node to calculated center
            if (lane.isIngress()) {
                Lane.NodePoint node = lane.getFirstNode();
                if (node != null) {
                    Direction direction = calculateCardinalDirection(node.getX(), node.getY());
                    lane.setCardinalDirection(direction);
                    logger.debug("Set direction for ingress lane {}: {}", lane.getId(), direction);
                }
            }
            // For egress lanes, calculate direction based on calculated center to last node
            else if (lane.isEgress()) {
                Lane.NodePoint node = lane.getLastNode();
                if (node != null) {
                    Direction direction = calculateCardinalDirection(node.getX(), node.getY());
                    lane.setCardinalDirection(direction);
                    logger.debug("Set direction for egress lane {}: {}", lane.getId(), direction);
                }
            }
        }

        // Log a summary of directions
        logDirectionSummary();
    }

    /**
     * Calculates the center of the intersection based on stop line positions
     */
    private void calculateIntersectionCenter() {
        List<Lane.NodePoint> stopLineNodes = new ArrayList<>();
        
        // Collect all stop line nodes from ingress lanes
        for (Lane lane : intersection.getLanes().values()) {
            if (lane.isIngress()) {
                Lane.NodePoint stopLineNode = lane.getStopLineNode();
                if (stopLineNode != null) {
                    stopLineNodes.add(stopLineNode);
                    logger.debug("Using stop line from lane {}: ({}, {})", 
                            lane.getId(), stopLineNode.getX(), stopLineNode.getY());
                }
            }
        }
        
        // If no stop lines found, fall back to reference point
        if (stopLineNodes.isEmpty()) {
            logger.warn("No stop lines found, using reference point as center");
            centerX = 0;
            centerY = 0;
            return;
        }
        
        // Calculate the average position of all stop lines
        double sumX = 0;
        double sumY = 0;
        
        for (Lane.NodePoint node : stopLineNodes) {
            sumX += node.getX();
            sumY += node.getY();
        }
        
        centerX = sumX / stopLineNodes.size();
        centerY = sumY / stopLineNodes.size();
        
        // Store calculated center in the intersection
        intersection.setCalculatedCenter(centerX, centerY);

        logger.info("Calculated intersection center from {} stop lines: ({}, {})", 
                stopLineNodes.size(), centerX, centerY);
    }

    /**
     * Calculates the cardinal direction from a point to the center
     * Implements the exact algorithm from the Python example
     */
    private Direction calculateCardinalDirection(double x, double y) {
        // Calculate dx and dy relative to center (point - center)
        double dx = x - centerX;
        double dy = y - centerY;
        
        // Calculate the angle in compass bearings (0° = North, 90° = East, etc.)
        // This matches the Python example: (90 - math.degrees(math.atan2(dy, dx))) % 360
        double angleRad = Math.atan2(dy, dx);
        double angleDeg = Math.toDegrees(angleRad);
        double compassAngle = (90 - angleDeg) % 360;
        
        // Ensure angle is positive (0-360)
        if (compassAngle < 0) {
            compassAngle += 360;
        }
        
        // Determine cardinal direction based on angle
        // This matches the sector boundaries from the Python example
        if (compassAngle < NORTH_BOUNDARY_2 || compassAngle >= NORTH_BOUNDARY_1) {
            return Direction.N;
        } else if (compassAngle < NORTHEAST_BOUNDARY) {
            return Direction.NE;
        } else if (compassAngle < EAST_BOUNDARY) {
            return Direction.E;
        } else if (compassAngle < SOUTHEAST_BOUNDARY) {
            return Direction.SE;
        } else if (compassAngle < SOUTH_BOUNDARY) {
            return Direction.S;
        } else if (compassAngle < SOUTHWEST_BOUNDARY) {
            return Direction.SW;
        } else if (compassAngle < WEST_BOUNDARY) {
            return Direction.W;
        } else {
            return Direction.NW;
        }
    }

    /**
     * Logs a summary of cardinal directions assigned to lanes
     */
    private void logDirectionSummary() {
        Map<Direction, Integer> directionCounts = new HashMap<>();

        // Count lanes in each direction
        for (Lane lane : intersection.getLanes().values()) {
            Direction direction = lane.getCardinalDirection();
            if (direction != null) {
                directionCounts.put(direction, directionCounts.getOrDefault(direction, 0) + 1);
            }
        }

        // Log summary
        StringBuilder sb = new StringBuilder("Direction assignment summary:");
        for (Direction direction : Direction.values()) {
            int count = directionCounts.getOrDefault(direction, 0);
            sb.append(" ").append(direction).append(": ").append(count);
        }

        logger.info(sb.toString());
    }

    /**
     * Gets lanes by cardinal direction
     */
    public Map<Direction, Lane> getRepresentativeLanesByDirection() {
        Map<Direction, Lane> result = new HashMap<>();

        // For each direction, find one representative lane
        for (Direction direction : Direction.values()) {
            for (Lane lane : intersection.getLanes().values()) {
                if (lane.getCardinalDirection() == direction && lane.isIngress()) {
                    result.put(direction, lane);
                    break;
                }
            }
        }

        return result;
    }

    /**
     * Calculates cardinal directions for all approaches in the intersection
     */
    public void calculateDirectionsForApproaches() {
        logger.info("Calculating cardinal directions for approaches");

        // Calculate the center of the intersection based on stop line positions
        calculateIntersectionCenter();
        
        logger.info("Calculated intersection center: ({}, {})", centerX, centerY);

        // Group lanes by approach ID
        Map<Integer, List<Lane>> approachLanes = new HashMap<>();
        
        for (Lane lane : intersection.getLanes().values()) {
            int approachId = lane.getApproachId();
            if (approachId > 0) {
                approachLanes.computeIfAbsent(approachId, k -> new ArrayList<>()).add(lane);
            }
        }
        
        logger.info("Found {} unique approach IDs", approachLanes.size());
        
        // Calculate direction for each approach
        for (Map.Entry<Integer, List<Lane>> entry : approachLanes.entrySet()) {
            int approachId = entry.getKey();
            List<Lane> lanes = entry.getValue();
            
            // Calculate average position for ingress lanes in this approach
            List<Lane> ingressLanes = lanes.stream()
                    .filter(Lane::isIngress)
                    .collect(Collectors.toList());
            
            if (!ingressLanes.isEmpty()) {
                // Calculate direction based on ingress lanes
                double sumX = 0;
                double sumY = 0;
                int count = 0;
                
                for (Lane lane : ingressLanes) {
                    Lane.NodePoint stopLine = lane.getStopLineNode();
                    if (stopLine != null) {
                        sumX += stopLine.getX();
                        sumY += stopLine.getY();
                        count++;
                    } else if (!lane.getNodeList().isEmpty()) {
                        Lane.NodePoint firstNode = lane.getFirstNode();
                        sumX += firstNode.getX();
                        sumY += firstNode.getY();
                        count++;
                    }
                }
                
                if (count > 0) {
                    double avgX = sumX / count;
                    double avgY = sumY / count;
                    
                    // Calculate direction using the same algorithm as the Python example
                    Direction direction = calculateCardinalDirection(avgX, avgY);
                    
                    // Apply this direction to all lanes in the approach
                    for (Lane lane : lanes) {
                        lane.setCardinalDirection(direction);
                    }
                    
                    logger.info("Set direction for approach {}: {} (based on {} ingress lanes)", 
                            approachId, direction, ingressLanes.size());
                }
            } else {
                // If no ingress lanes, try using egress lanes
                List<Lane> egressLanes = lanes.stream()
                        .filter(Lane::isEgress)
                        .collect(Collectors.toList());
                
                if (!egressLanes.isEmpty()) {
                    // Calculate direction based on egress lanes
                    double sumX = 0;
                    double sumY = 0;
                    int count = 0;
                    
                    for (Lane lane : egressLanes) {
                        if (!lane.getNodeList().isEmpty()) {
                            Lane.NodePoint lastNode = lane.getLastNode();
                            sumX += lastNode.getX();
                            sumY += lastNode.getY();
                            count++;
                        }
                    }
                    
                    if (count > 0) {
                        double avgX = sumX / count;
                        double avgY = sumY / count;
                        
                        // Calculate direction using the same algorithm as the Python example
                        Direction direction = calculateCardinalDirection(avgX, avgY);
                        
                        // Apply this direction to all lanes in the approach
                        for (Lane lane : lanes) {
                            lane.setCardinalDirection(direction);
                        }
                        
                        logger.info("Set direction for approach {}: {} (based on {} egress lanes)", 
                                approachId, direction, egressLanes.size());
                    }
                }
            }
        }
        
        // Log a summary of directions
        logDirectionSummary();
    }
}
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
     * Calculates the center of the intersection based on stop line positions
     */
    private void calculateIntersectionCenter() {
        List<Lane.NodePoint> stopLineNodes = new ArrayList<>();
        
        // Collect all stop line nodes from ingress lanes
        for (Lane lane : intersection.getLanes().values()) {
            if (lane.isIngress() && lane.isVehicleLane()) {
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
        logger.debug("Calculating direction for position (x={}, y={}), relative to center ({}, {})", 
            x, y, centerX, centerY);

        // Calculate dx and dy relative to center (point - center)
        double dx = x - centerX;
        double dy = y - centerY;
        logger.debug("Delta values: dx={}, dy={}", dx, dy);

        // Calculate the angle in compass bearings (0° = North, 90° = East, etc.)
        double angleRad = Math.atan2(dy, dx);
        double angleDeg = Math.toDegrees(angleRad);
        double compassAngle = (90 - angleDeg) % 360;
        
        if (compassAngle < 0) {
            compassAngle += 360;
        }
        
        logger.debug("Calculated compass angle: {} degrees", compassAngle);
        
        // Determine cardinal direction based on angle
        if (compassAngle < NORTH_BOUNDARY_2 || compassAngle >= NORTH_BOUNDARY_1) {
            logger.debug("Angle {} falls in NORTH sector ({}-360° or 0-{}°)", 
                compassAngle, NORTH_BOUNDARY_1, NORTH_BOUNDARY_2);
            return Direction.N;
        } else if (compassAngle < NORTHEAST_BOUNDARY) {
            logger.debug("Angle {} falls in NORTHEAST sector ({}-{}°)", 
                compassAngle, NORTH_BOUNDARY_2, NORTHEAST_BOUNDARY);
            return Direction.NE;
        } else if (compassAngle < EAST_BOUNDARY) {
            logger.debug("Angle {} falls in EAST sector ({}-{}°)", 
                compassAngle, NORTHEAST_BOUNDARY, EAST_BOUNDARY);
            return Direction.E;
        } else if (compassAngle < SOUTHEAST_BOUNDARY) {
            logger.debug("Angle {} falls in SOUTHEAST sector ({}-{}°)", 
                compassAngle, EAST_BOUNDARY, SOUTHEAST_BOUNDARY);
            return Direction.SE;
        } else if (compassAngle < SOUTH_BOUNDARY) {
            logger.debug("Angle {} falls in SOUTH sector ({}-{}°)", 
                compassAngle, SOUTHEAST_BOUNDARY, SOUTH_BOUNDARY);
            return Direction.S;
        } else if (compassAngle < SOUTHWEST_BOUNDARY) {
            logger.debug("Angle {} falls in SOUTHWEST sector ({}-{}°)", 
                compassAngle, SOUTH_BOUNDARY, SOUTHWEST_BOUNDARY);
            return Direction.SW;
        } else if (compassAngle < WEST_BOUNDARY) {
            logger.debug("Angle {} falls in WEST sector ({}-{}°)", 
                compassAngle, SOUTHWEST_BOUNDARY, WEST_BOUNDARY);
            return Direction.W;
        } else {
            logger.debug("Angle {} falls in NORTHWEST sector ({}-{}°)", 
                compassAngle, WEST_BOUNDARY, NORTHWEST_BOUNDARY);
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
                    .filter(Lane::isVehicleLane)
                    .toList();
            
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
                    
                    logger.debug("Calculated average position for approach {}: (x={}, y={}) from {} lanes", 
                        approachId, avgX, avgY, count);
                    
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
                throw new RuntimeException("Map has approach ID " + approachId + " but no ingress lane found");
            }
        }
        
        // Log a summary of directions
        logDirectionSummary();
    }
}
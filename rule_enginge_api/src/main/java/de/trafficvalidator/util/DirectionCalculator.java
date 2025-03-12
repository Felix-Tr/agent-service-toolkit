package de.trafficvalidator.util;

import de.trafficvalidator.model.Direction;
import de.trafficvalidator.model.Intersection;
import de.trafficvalidator.model.Lane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class to calculate directions for lanes based on their coordinates.
 */
public class DirectionCalculator {
    private static final Logger logger = LoggerFactory.getLogger(DirectionCalculator.class);
    
    private final Intersection intersection;
    
    public DirectionCalculator(Intersection intersection) {
        this.intersection = intersection;
    }
    
    /**
     * Calculates directions for all lanes in the intersection
     */
    public void calculateDirections() {
        logger.info("Calculating directions for lanes");
        
        // Get reference point (center of intersection)
        double refLat = intersection.getRefLat();
        double refLong = intersection.getRefLong();
        
        // Process each lane
        for (Lane lane : intersection.getLanes().values()) {
            if (lane.getNodeList().isEmpty()) {
                logger.warn("Lane {} has no nodes, cannot calculate direction", lane.getId());
                continue;
            }
            
            // For ingress lanes, calculate direction from first node to center
            if (lane.isIngress()) {
                Lane.NodePoint node = lane.getFirstNode();
                if (node != null) {
                    Direction direction = calculateDirection(node.getX(), node.getY());
                    lane.setDirection(direction);
                    logger.debug("Set direction for ingress lane {}: {}", lane.getId(), direction);
                }
            }
            // For egress lanes, calculate direction from center to last node
            else if (lane.isEgress()) {
                Lane.NodePoint node = lane.getLastNode();
                if (node != null) {
                    Direction direction = calculateDirection(node.getX(), node.getY());
                    lane.setDirection(direction);
                    logger.debug("Set direction for egress lane {}: {}", lane.getId(), direction);
                }
            }
        }
    }
    
    /**
     * Calculates direction from a node coordinate relative to center
     */
    private Direction calculateDirection(double x, double y) {
        // Calculate angle in radians
        double angle = Math.atan2(y, x);
        
        // Convert to degrees and normalize to 0-360
        double degrees = Math.toDegrees(angle);
        if (degrees < 0) {
            degrees += 360;
        }
        
        // Map to direction
        if (degrees >= 337.5 || degrees < 22.5) {
            return Direction.E;
        } else if (degrees >= 22.5 && degrees < 67.5) {
            return Direction.SE;
        } else if (degrees >= 67.5 && degrees < 112.5) {
            return Direction.S;
        } else if (degrees >= 112.5 && degrees < 157.5) {
            return Direction.SW;
        } else if (degrees >= 157.5 && degrees < 202.5) {
            return Direction.W;
        } else if (degrees >= 202.5 && degrees < 247.5) {
            return Direction.NW;
        } else if (degrees >= 247.5 && degrees < 292.5) {
            return Direction.N;
        } else {
            return Direction.NE;
        }
    }
}
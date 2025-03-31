package de.trafficvalidator.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.trafficvalidator.model.Connection;
import de.trafficvalidator.model.Intersection;
import de.trafficvalidator.model.Lane;
import de.trafficvalidator.model.SignalGroup;
import de.trafficvalidator.service.IntersectionService;
import de.trafficvalidator.service.ValidationService;

/**
 * Controller for debug endpoints to help troubleshoot validation issues
 */
@RestController
@RequestMapping("/api/debug")
public class DebugController {
    private static final Logger logger = LoggerFactory.getLogger(DebugController.class);
    
    private final ValidationService validationService;
    private final IntersectionService intersectionService;
    
    @Autowired
    public DebugController(ValidationService validationService, IntersectionService intersectionService) {
        this.validationService = validationService;
        this.intersectionService = intersectionService;
    }
    
    /**
     * Get detailed information about an intersection for debugging
     */
    @GetMapping("/intersection/{id}")
    public ResponseEntity<Map<String, Object>> getIntersectionDetails(@PathVariable String id) {
        try {
            Intersection intersection = intersectionService.loadIntersection(id);
            
            Map<String, Object> details = new HashMap<>();
            details.put("id", id);
            details.put("name", intersection.getName());
            details.put("connectionCount", intersection.getConnections().size());
            details.put("laneCount", intersection.getLanes().size());
            details.put("signalGroupCount", intersection.getPhysicalSignalGroups().size());
            
            // Add signal group details
            List<Map<String, Object>> signalGroupDetails = new ArrayList<>();
            for (SignalGroup sg : intersection.getPhysicalSignalGroups().values()) {
                Map<String, Object> sgInfo = new HashMap<>();
                sgInfo.put("id", sg.getId());
                sgInfo.put("name", sg.getName());
                sgInfo.put("type", sg.getType());
                sgInfo.put("isDiagonalLeftTurn", sg.isDiagonalLeftTurn());
                sgInfo.put("isAdditionalRightTurnArrow", sg.isAdditionalRightTurnArrow());
                sgInfo.put("isLeftTurnFull", sg.istLinksabbiegerVollscheibe());
                signalGroupDetails.add(sgInfo);
            }
            details.put("signalGroups", signalGroupDetails);
            
            // Add cyclist right turn information
            List<Map<String, Object>> cyclistRightTurns = new ArrayList<>();
            for (Connection conn : intersection.getConnections()) {
                if (conn.isCyclistRightTurn()) {
                    cyclistRightTurns.add(getConnectionDetails(conn, intersection));
                }
            }
            details.put("cyclistRightTurns", cyclistRightTurns);
            
            // Add left turn information
            List<Map<String, Object>> leftTurns = new ArrayList<>();
            for (Connection conn : intersection.getConnections()) {
                if (conn.isLeftTurn()) {
                    leftTurns.add(getConnectionDetails(conn, intersection));
                }
            }
            details.put("leftTurns", leftTurns);
            
            return ResponseEntity.ok(details);
        } catch (Exception e) {
            logger.error("Error getting intersection details", e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    /**
     * Get detailed information about a specific connection
     */
    private Map<String, Object> getConnectionDetails(Connection connection, Intersection intersection) {
        Map<String, Object> details = new HashMap<>();
        details.put("id", connection.getId());
        details.put("ingressLane", connection.getIngressLane() != null ? connection.getIngressLane().getId() : null);
        details.put("egressLane", connection.getEgressLane() != null ? connection.getEgressLane().getId() : null);
        details.put("maneuver", connection.getManeuverType());
        details.put("isCyclistRightTurn", connection.isCyclistRightTurn());
        details.put("isLeftTurn", connection.isLeftTurn());
        details.put("isRightTurn", connection.isRightTurn());
        details.put("signalGroups", connection.getConnectedSignalGroups());
        
        // Find left turns sharing the same egress lane
        if (connection.getEgressLane() != null) {
            List<Map<String, Object>> sharingLeftTurns = new ArrayList<>();
            for (Connection otherConn : intersection.getConnections()) {
                if (otherConn != connection && 
                    otherConn.isLeftTurn() && 
                    otherConn.getEgressLane() != null && 
                    otherConn.getEgressLane().getId() == connection.getEgressLane().getId()) {
                    
                    Map<String, Object> sharingTurn = new HashMap<>();
                    sharingTurn.put("id", otherConn.getId());
                    sharingTurn.put("signalGroups", otherConn.getConnectedSignalGroups());
                    sharingLeftTurns.add(sharingTurn);
                }
            }
            details.put("sharingLeftTurns", sharingLeftTurns);
        }
        
        return details;
    }
} 
package de.trafficvalidator.service;

import de.trafficvalidator.model.Connection;
import de.trafficvalidator.model.Direction;
import de.trafficvalidator.model.Intersection;
import de.trafficvalidator.model.Lane;
import de.trafficvalidator.model.SignalGroup;
import de.trafficvalidator.model.ValidationResult;
import de.trafficvalidator.parser.MapemParser;
import de.trafficvalidator.parser.StgParser;
import de.trafficvalidator.rules.GreenCyclistArrowRules;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Message;
import org.kie.api.builder.ReleaseId;
import org.kie.api.builder.Results;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for validating traffic light configurations.
 * This service integrates with the existing validation logic
 * and provides a higher-level API for the REST controller.
 */
@Service
public class ValidationService {
    private static final Logger logger = LoggerFactory.getLogger(ValidationService.class);

    private final StorageService storageService;
    private final KieContainer kieContainer;

    @Autowired
    public ValidationService(StorageService storageService, KieContainer kieContainer) {
        this.storageService = storageService;
        this.kieContainer = kieContainer;
    }

    /**
     * Validates an intersection configuration using the specified ruleset
     *
     * @param id The ID of the intersection configuration to validate
     * @param ruleset The ruleset to validate against
     * @return Validation results, including summary and detailed results
     */
    public Map<String, Object> validateIntersection(String id, String ruleset) {
        try {
            // Parse MAPEM and STG files
            Intersection intersection = loadIntersection(id);

            // Validate based on ruleset
            List<ValidationResult> results;

            if ("cyclist-arrow".equals(ruleset)) {
                results = validateWithCyclistArrowRules(intersection);
            } else {
                results = validateWithCustomRuleset(intersection, ruleset);
            }

            // Build response
            Map<String, Object> response = new HashMap<>();
            response.put("id", id);
            response.put("ruleset", ruleset);
            response.put("intersection", createIntersectionSummary(intersection));
            
            // Format results based on ruleset
            if ("cyclist-arrow".equals(ruleset)) {
                response.put("results", formatGroupedValidationResults(results));
            } else {
                response.put("results", formatValidationResults(results));
            }

            return response;

        } catch (Exception e) {
            logger.error("Failed to validate intersection {}", id, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("id", id);
            errorResponse.put("ruleset", ruleset);
            errorResponse.put("error", e.getMessage());
            return errorResponse;
        }
    }

    /**
     * Returns a summary of the intersection configuration without validation
     *
     * @param id The ID of the intersection configuration
     * @return Summary information about the intersection
     */
    public Map<String, Object> getIntersectionSummary(String id) {
        try {
            // Parse MAPEM and STG files
            Intersection intersection = loadIntersection(id);

            // Build response
            Map<String, Object> response = new HashMap<>();
            response.put("id", id);
            response.put("summary", createIntersectionSummary(intersection));
            response.put("directionData", createDirectionSummary(intersection));
            response.put("signalGroups", createSignalGroupSummary(intersection));

            return response;

        } catch (Exception e) {
            logger.error("Failed to get summary for intersection {}", id, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("id", id);
            errorResponse.put("error", e.getMessage());
            return errorResponse;
        }
    }

    /**
     * Lists available intersection IDs and rulesets
     *
     * @return Available configurations and rulesets
     */
    public Map<String, Object> getAvailableConfigurations() {
        Map<String, Object> response = new HashMap<>();
        response.put("intersections", storageService.getAvailableIntersectionIds());
        response.put("rulesets", storageService.getAvailableRulesets());
        return response;
    }

    /**
     * Loads and parses an intersection configuration
     *
     * @param id The ID of the intersection configuration
     * @return Parsed Intersection object
     * @throws Exception If parsing fails
     */
    private Intersection loadIntersection(String id) throws Exception {
        // Get MAPEM file
        InputStream mapemStream = storageService.getMapemFile(id);

        // Parse MAPEM
        MapemParser mapemParser = new MapemParser();
        Intersection intersection = mapemParser.parse(mapemStream);

        // Get STG file
        InputStream stgStream = storageService.getStgFile(id);

        // Parse STG and update intersection
        StgParser stgParser = new StgParser();
        stgParser.parse(stgStream);
        stgParser.updateIntersection(intersection);

        return intersection;
    }

    /**
     * Validates intersection using built-in cyclist arrow rules
     *
     * @param intersection The intersection to validate
     * @return Validation results
     */
    private List<ValidationResult> validateWithCyclistArrowRules(Intersection intersection) {
        // Use the existing rules implementation with the injected KieContainer
        GreenCyclistArrowRules rules = new GreenCyclistArrowRules(kieContainer);
        return rules.validateIntersection(intersection);
    }

    /**
     * Validates intersection using a custom ruleset
     *
     * @param intersection The intersection to validate
     * @param ruleset The ruleset name to use
     * @return Validation results
     */
    private List<ValidationResult> validateWithCustomRuleset(Intersection intersection, String ruleset) {
        KieContainer customKieContainer = null;
        KieSession kieSession = null;
        
        try {
            // Load the rule file
            InputStream ruleStream = storageService.getRulesetFile(ruleset);

            // Create a dynamic KieContainer with the custom rules
            customKieContainer = createKieContainerFromRules(ruleset, ruleStream);

            // Create a session for each validation
            kieSession = customKieContainer.newKieSession();
            
            // Create results list
            List<ValidationResult> results = new ArrayList<>();
            
            // Insert intersection fact
            kieSession.insert(intersection);
            
            // Insert all physical signal groups
            for (SignalGroup group : intersection.getPhysicalSignalGroups().values()) {
                kieSession.insert(group);
            }
            
            // Process each connection separately
            for (Connection connection : intersection.getConnections()) {
                ValidationResult result = new ValidationResult(connection);
                
                // Insert facts
                kieSession.insert(connection);
                kieSession.insert(result);
                
                results.add(result);
            }

            // Fire rules
            kieSession.fireAllRules();

            // Return results (they will be updated by the rules)
            return results;
        } catch (Exception e) {
            logger.error("Failed to validate with custom ruleset: {}", ruleset, e);
            throw new RuntimeException("Failed to validate with custom ruleset: " + ruleset, e);
        } finally {
            // Properly dispose of resources
            if (kieSession != null) {
                kieSession.dispose();
            }
            if (customKieContainer != null) {
                customKieContainer.dispose();
            }
        }
    }

    /**
     * Creates a KieContainer from a rule file
     *
     * @param ruleset The ruleset name
     * @param ruleStream The rule file input stream
     * @return A KieContainer for rule execution
     * @throws IOException If the rule file cannot be read
     */
    private KieContainer createKieContainerFromRules(String ruleset, InputStream ruleStream) throws IOException {
        KieServices kieServices = KieServices.Factory.get();
        KieFileSystem kfs = kieServices.newKieFileSystem();

        // Create a unique release ID for this ruleset with a timestamp to ensure uniqueness
        String timestamp = String.valueOf(System.currentTimeMillis());
        ReleaseId releaseId = kieServices.newReleaseId("de.trafficvalidator", "dynamic-rules-" + ruleset, "1.0.0-" + timestamp);
        kfs.generateAndWritePomXML(releaseId);

        // Read the rule file content
        byte[] ruleContent = ruleStream.readAllBytes();

        // Add the rule file to the KieFileSystem
        kfs.write("src/main/resources/rules/" + ruleset + ".drl", new String(ruleContent));

        // Build the rules
        KieBuilder kieBuilder = kieServices.newKieBuilder(kfs);
        kieBuilder.buildAll();

        // Check for errors
        Results results = kieBuilder.getResults();
        if (results.hasMessages(Message.Level.ERROR)) {
            throw new RuntimeException("Failed to build rules: " + results.getMessages());
        }

        // Create a KieContainer with the current ClassLoader
        return kieServices.newKieContainer(releaseId, getClass().getClassLoader());
    }

    /**
     * Creates a summary of the intersection
     *
     * @param intersection The intersection to summarize
     * @return A map containing summary information
     */
    private Map<String, Object> createIntersectionSummary(Intersection intersection) {
        Map<String, Object> summary = new HashMap<>();
        summary.put("id", intersection.getId());
        summary.put("name", intersection.getName());
        summary.put("regionId", intersection.getRegionId());
        summary.put("revision", intersection.getRevision());
        summary.put("laneCount", intersection.getLanes().size());
        summary.put("connectionCount", intersection.getConnections().size());
        summary.put("physicalSignalGroupCount", intersection.getPhysicalSignalGroups().size());

        return summary;
    }

    /**
     * Creates a summary of cardinal directions and maneuvers
     */
    private Map<String, Object> createDirectionSummary(Intersection intersection) {
        Map<String, Object> summary = new HashMap<>();

        // Summarize ingress lanes by direction
        Map<Direction, List<Lane>> ingressByDirection = intersection.getIngressLanesByDirection();
        Map<String, List<Integer>> ingressSummary = new HashMap<>();

        for (Map.Entry<Direction, List<Lane>> entry : ingressByDirection.entrySet()) {
            List<Integer> laneIds = entry.getValue().stream()
                    .map(Lane::getId)
                    .collect(Collectors.toList());
            ingressSummary.put(entry.getKey().name(), laneIds);
        }

        summary.put("ingressLanesByDirection", ingressSummary);

        // Summarize egress lanes by direction
        Map<Direction, List<Lane>> egressByDirection = intersection.getEgressLanesByDirection();
        Map<String, List<Integer>> egressSummary = new HashMap<>();

        for (Map.Entry<Direction, List<Lane>> entry : egressByDirection.entrySet()) {
            List<Integer> laneIds = entry.getValue().stream()
                    .map(Lane::getId)
                    .collect(Collectors.toList());
            egressSummary.put(entry.getKey().name(), laneIds);
        }

        summary.put("egressLanesByDirection", egressSummary);

        // Summarize connections by maneuver type
        List<Connection> rightTurns = intersection.getConnectionsByManeuverType(true, false, false);
        List<Connection> leftTurns = intersection.getConnectionsByManeuverType(false, true, false);
        List<Connection> straightAhead = intersection.getConnectionsByManeuverType(false, false, true);

        Map<String, List<Map<String, Object>>> connectionsByManeuver = new HashMap<>();

        connectionsByManeuver.put("rightTurn", formatConnectionList(rightTurns));
        connectionsByManeuver.put("leftTurn", formatConnectionList(leftTurns));
        connectionsByManeuver.put("straight", formatConnectionList(straightAhead));

        summary.put("connectionsByManeuver", connectionsByManeuver);

        return summary;
    }

    /**
     * Creates a summary of signal groups
     */
    private Map<String, Object> createSignalGroupSummary(Intersection intersection) {
        Map<String, Object> summary = new HashMap<>();

        // Summarize physical signal groups
        List<Map<String, Object>> physicalGroups = new ArrayList<>();

        for (SignalGroup group : intersection.getPhysicalSignalGroups().values()) {
            Map<String, Object> groupInfo = new HashMap<>();
            groupInfo.put("id", group.getPhysicalSignalGroupId());
            groupInfo.put("name", group.getName());
            groupInfo.put("type", group.getType().name());
            groupInfo.put("controlsOnlyLeftTurns", group.istLinksabbiegerVollscheibe());

            // Get connections controlled by this physical group
            List<Connection> connections = intersection.getConnectionsByPhysicalSignalGroupId(group.getPhysicalSignalGroupId());
            groupInfo.put("connectionCount", connections.size());
            groupInfo.put("connections", formatConnectionList(connections));

            physicalGroups.add(groupInfo);
        }

        summary.put("physicalSignalGroups", physicalGroups);

        return summary;
    }

    /**
     * Formats a list of connections for JSON output
     */
    private List<Map<String, Object>> formatConnectionList(List<Connection> connections) {
        return connections.stream().map(conn -> {
            Map<String, Object> connInfo = new HashMap<>();
            connInfo.put("id", conn.getId());

            if (conn.getIngressLane() != null) {
                connInfo.put("ingressLaneId", conn.getIngressLane().getId());
                connInfo.put("ingressDirection",
                        conn.getIngressLane().getCardinalDirection() != null ?
                                conn.getIngressLane().getCardinalDirection().name() : null);
            }

            if (conn.getEgressLane() != null) {
                connInfo.put("egressLaneId", conn.getEgressLane().getId());
                connInfo.put("egressDirection",
                        conn.getEgressLane().getCardinalDirection() != null ?
                                conn.getEgressLane().getCardinalDirection().name() : null);
            }

            connInfo.put("physicalSignalGroupId", conn.getPhysicalSignalGroupId());
            connInfo.put("maneuvers", Map.of(
                    "rightTurn", conn.isRightTurn(),
                    "leftTurn", conn.isLeftTurn(),
                    "straight", conn.isStraight(),
                    "uTurn", conn.isUTurn()
            ));

            return connInfo;
        }).collect(Collectors.toList());
    }

    /**
     * Formats validation results into a map for JSON response
     *
     * @param results The validation results
     * @return A map containing formatted results
     */
    private List<Map<String, Object>> formatValidationResults(List<ValidationResult> results) {
        List<Map<String, Object>> formattedResults = new ArrayList<>();

        for (ValidationResult result : results) {
            Map<String, Object> formattedResult = new HashMap<>();
            formattedResult.put("connectionId", result.getConnection().getId());

            if (result.getConnection().getIngressLane() != null &&
                    result.getConnection().getEgressLane() != null) {

                Direction ingressDir = result.getConnection().getIngressLane().getCardinalDirection();
                Direction egressDir = result.getConnection().getEgressLane().getCardinalDirection();

                if (ingressDir != null && egressDir != null) {
                    formattedResult.put("direction", ingressDir.name() + " → " + egressDir.name());
                }
            }

            formattedResult.put("valid", result.isValid());

            if (!result.isValid()) {
                formattedResult.put("reasons", result.getReasons());
            }

            formattedResults.add(formattedResult);
        }

        return formattedResults;
    }
    
    /**
     * Formats validation results grouped by approach directions for the cyclist-arrow ruleset
     * This format is optimized for GPT agent consumption
     *
     * @param results The validation results
     * @return A map containing grouped validation results by approaches
     */
    private Map<String, Object> formatGroupedValidationResults(List<ValidationResult> results) {
        Map<String, Object> response = new HashMap<>();
        
        // Count validation stats
        long totalConnections = results.size();
        long cyclistRightTurns = results.stream()
                .filter(r -> r.getConnection().isCyclistRightTurn())
                .count();
        long validConnections = results.stream()
                .filter(r -> r.getConnection().isCyclistRightTurn() && r.isValid())
                .count();
        long invalidConnections = results.stream()
                .filter(r -> r.getConnection().isCyclistRightTurn() && !r.isValid())
                .count();
        
        // Add statistics to the response
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalConnections", totalConnections);
        statistics.put("cyclistRightTurns", cyclistRightTurns);
        statistics.put("validCyclistRightTurns", validConnections);
        statistics.put("invalidCyclistRightTurns", invalidConnections);
        response.put("statistics", statistics);
        
        // Group results by approach direction
        Map<String, List<Map<String, Object>>> approachGroups = new HashMap<>();
        
        for (ValidationResult result : results) {
            Connection connection = result.getConnection();
            Lane ingressLane = connection.getIngressLane();
            
            if (ingressLane != null && ingressLane.getDirection() != null) {
                String approachDirection = ingressLane.getDirection().name();
                
                // Create approach group if it doesn't exist
                if (!approachGroups.containsKey(approachDirection)) {
                    approachGroups.put(approachDirection, new ArrayList<>());
                }
                
                // Create connection details
                Map<String, Object> connectionDetails = new HashMap<>();
                connectionDetails.put("connectionId", connection.getId());
                
                // Add ingress and egress lanes
                if (ingressLane != null && connection.getEgressLane() != null) {
                    Direction ingressDir = ingressLane.getDirection();
                    Direction egressDir = connection.getEgressLane().getDirection();
                    
                    if (ingressDir != null && egressDir != null) {
                        connectionDetails.put("direction", ingressDir.name() + " → " + egressDir.name());
                    }
                }
                
                // Add maneuver type
                String maneuverType = "";
                if (connection.isStraight()) {
                    maneuverType = "Straight";
                } else if (connection.isLeftTurn()) {
                    maneuverType = "Left Turn";
                } else if (connection.isRightTurn()) {
                    maneuverType = "Right Turn";
                } else if (connection.isUTurn()) {
                    maneuverType = "U-Turn";
                }
                connectionDetails.put("maneuver", maneuverType);
                
                // Add cyclist information
                connectionDetails.put("isCyclistRightTurn", connection.isCyclistRightTurn());
                
                // Add validation result
                connectionDetails.put("valid", result.isValid());
                
                // Add reasons for failure if invalid
                if (!result.isValid()) {
                    connectionDetails.put("reasons", result.getReasons());
                }
                
                // Add to approach group
                approachGroups.get(approachDirection).add(connectionDetails);
            }
        }
        
        // Add approach groups to response
        response.put("approaches", approachGroups);
        
        return response;
    }
}
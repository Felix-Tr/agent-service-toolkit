package de.trafficvalidator.controller;

import de.trafficvalidator.service.ValidationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST controller for traffic light validation requests
 */
@RestController
@RequestMapping("/api/validate")
@Tag(name = "Validation", description = "API for validating traffic light configurations")
public class ValidationController {
    private static final Logger logger = LoggerFactory.getLogger(ValidationController.class);
    
    private final ValidationService validationService;
    
    @Autowired
    public ValidationController(ValidationService validationService) {
        this.validationService = validationService;
    }
    
    /**
     * Validates an intersection configuration using the specified ruleset
     * 
     * @param id The ID of the intersection configuration to validate
     * @param ruleset The ruleset to validate against (default: 'cyclist-arrow')
     * @return Validation results for the intersection
     */
    @Operation(
        summary = "Validate an intersection configuration",
        description = "Validates a traffic light intersection configuration against a specified ruleset to identify safety issues"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful validation"),
        @ApiResponse(responseCode = "404", description = "Intersection configuration not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error during validation")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> validateIntersection(
            @Parameter(description = "Intersection configuration ID") @PathVariable String id,
            @Parameter(description = "Ruleset to validate against") @RequestParam(defaultValue = "cyclist-arrow") String ruleset) {
        
        logger.info("Validating intersection {} with ruleset {}", id, ruleset);
        
        Map<String, Object> results = validationService.validateIntersection(id, ruleset);
        
        // Check if there was an error loading the intersection
        if (results.containsKey("error") && results.get("error") != null) {
            String errorMessage = (String) results.get("error");
            if (errorMessage.contains("Failed to load MAPEM file for ID")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(results);
            }
            // For other errors, return 500 or appropriate status
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(results);
        }
        
        // For cyclist-arrow ruleset, add a summary of the relevant connections
        if ("cyclist-arrow".equals(ruleset)) {
            enrichCyclistArrowResponse(results);
        }
        
        return ResponseEntity.ok(results);
    }
    
    /**
     * Enriches the response with a cyclist-arrow-specific summary
     * 
     * @param results The validation results to enrich
     */
    private void enrichCyclistArrowResponse(Map<String, Object> results) {
        // Extract statistics if available
        if (results.containsKey("results") && results.get("results") instanceof Map) {
            Map<String, Object> formattedResults = (Map<String, Object>) results.get("results");
            
            if (formattedResults.containsKey("statistics") && formattedResults.containsKey("approaches")) {
                // Prepare a text summary for GPT agent consumption
                StringBuilder summary = new StringBuilder();
                Map<String, Object> statistics = (Map<String, Object>) formattedResults.get("statistics");
                
                // Add summary statistics
                summary.append("Summary:\n");
                summary.append("Total connections: ").append(statistics.get("totalConnections")).append("\n");
                summary.append("Cyclist right turns: ").append(statistics.get("cyclistRightTurns")).append("\n");
                summary.append("Valid cyclist right turns: ").append(statistics.get("validCyclistRightTurns")).append("\n");
                summary.append("Invalid cyclist right turns: ").append(statistics.get("invalidCyclistRightTurns")).append("\n\n");
                
                // Add details by approach
                summary.append("Results:\n\n");
                
                Map<String, List<Map<String, Object>>> approaches = 
                        (Map<String, List<Map<String, Object>>>) formattedResults.get("approaches");
                
                for (Map.Entry<String, List<Map<String, Object>>> entry : approaches.entrySet()) {
                    String approachDirection = entry.getKey();
                    List<Map<String, Object>> connections = entry.getValue();
                    
                    summary.append("Approach from ").append(approachDirection).append(":\n");
                    
                    // Group connections by whether they're cyclist right turns
                    List<Map<String, Object>> cyclistRightTurns = connections.stream()
                            .filter(conn -> Boolean.TRUE.equals(conn.get("isCyclistRightTurn")))
                            .collect(Collectors.toList());
                    
                    List<Map<String, Object>> otherConnections = connections.stream()
                            .filter(conn -> !Boolean.TRUE.equals(conn.get("isCyclistRightTurn")))
                            .collect(Collectors.toList());
                    
                    // Output cyclist right turns
                    if (!cyclistRightTurns.isEmpty()) {
                        summary.append("  Cyclist right turns:\n");
                        for (Map<String, Object> conn : cyclistRightTurns) {
                            String direction = (String) conn.get("direction");
                            boolean isValid = (boolean) conn.get("valid");
                            
                            summary.append("  - Connection ").append(conn.get("connectionId"))
                                  .append(": ").append(direction)
                                  .append(" (").append(isValid ? "VALID" : "INVALID").append(")\n");
                            
                            // Add reasons if invalid
                            if (!isValid && conn.containsKey("reasons")) {
                                List<String> reasons = (List<String>) conn.get("reasons");
                                for (String reason : reasons) {
                                    summary.append("    * ").append(reason).append("\n");
                                }
                            }
                        }
                    } else {
                        summary.append("  No cyclist right turns from this approach\n");
                    }
                    
                    // Output other connections (brief summary)
                    if (!otherConnections.isEmpty()) {
                        summary.append("  Other connections: ").append(otherConnections.size())
                               .append(" (not applicable for cyclist right turn signs)\n");
                    }
                    
                    summary.append("\n");
                }
                
                // Add this text summary to the results
                results.put("textSummary", summary.toString());
            }
        }
    }
    
    /**
     * Returns a summary of the intersection configuration without validation
     * 
     * @param id The ID of the intersection configuration
     * @return Summary information about the intersection
     */
    @Operation(
        summary = "Get intersection summary",
        description = "Returns a summary of the intersection configuration without performing validation"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved intersection summary"),
        @ApiResponse(responseCode = "404", description = "Intersection configuration not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{id}/summary")
    public ResponseEntity<Map<String, Object>> getIntersectionSummary(
            @Parameter(description = "Intersection configuration ID") @PathVariable String id) {
        logger.info("Getting summary for intersection {}", id);
        
        Map<String, Object> summary = validationService.getIntersectionSummary(id);
        
        // Check if there was an error loading the intersection
        if (summary.containsKey("error") && summary.get("error") != null) {
            String errorMessage = (String) summary.get("error");
            if (errorMessage.contains("Failed to load MAPEM file for ID")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(summary);
            }
            // For other errors, return 500 or appropriate status
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(summary);
        }
        
        return ResponseEntity.ok(summary);
    }
    
    /**
     * Lists available intersection IDs and rulesets
     * 
     * @return Available configurations and rulesets
     */
    @Operation(
        summary = "Get available configurations",
        description = "Lists all available intersection IDs and validation rulesets"
    )
    @ApiResponse(responseCode = "200", description = "Successfully retrieved available configurations")
    @GetMapping("/available")
    public ResponseEntity<Map<String, Object>> getAvailableConfigurations() {
        logger.info("Getting available configurations");
        
        Map<String, Object> available = validationService.getAvailableConfigurations();
        
        return ResponseEntity.ok(available);
    }
}
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

import java.util.Map;

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
        
        return ResponseEntity.ok(results);
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
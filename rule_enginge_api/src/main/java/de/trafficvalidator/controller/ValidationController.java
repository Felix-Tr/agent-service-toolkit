package de.trafficvalidator.controller;

import de.trafficvalidator.model.ValidationResult;
import de.trafficvalidator.service.ValidationService;
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

/**
 * REST controller for traffic light validation requests
 */
@RestController
@RequestMapping("/api/validate")
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
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> validateIntersection(
            @PathVariable String id,
            @RequestParam(defaultValue = "cyclist-arrow") String ruleset) {
        
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
    @GetMapping("/{id}/summary")
    public ResponseEntity<Map<String, Object>> getIntersectionSummary(@PathVariable String id) {
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
    @GetMapping("/available")
    public ResponseEntity<Map<String, Object>> getAvailableConfigurations() {
        logger.info("Getting available configurations");
        
        Map<String, Object> available = validationService.getAvailableConfigurations();
        
        return ResponseEntity.ok(available);
    }
}
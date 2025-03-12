package de.trafficvalidator.service;

import java.io.InputStream;
import java.util.List;

/**
 * Service interface for loading and retrieving configuration data.
 * This interface provides abstraction for different storage implementations (file, database, etc.)
 */
public interface StorageService {
    
    /**
     * Gets the MAPEM XML file for the specified intersection ID
     * 
     * @param id The intersection ID
     * @return InputStream for the MAPEM XML file
     */
    InputStream getMapemFile(String id);
    
    /**
     * Gets the STG file for the specified intersection ID
     * 
     * @param id The intersection ID
     * @return InputStream for the STG file
     */
    InputStream getStgFile(String id);
    
    /**
     * Gets a ruleset file by name
     * 
     * @param rulesetName The name of the ruleset
     * @return InputStream for the ruleset file
     */
    InputStream getRulesetFile(String rulesetName);
    
    /**
     * Lists all available intersection IDs
     * 
     * @return List of available intersection IDs
     */
    List<String> getAvailableIntersectionIds();
    
    /**
     * Lists all available rulesets
     * 
     * @return List of available ruleset names
     */
    List<String> getAvailableRulesets();
}
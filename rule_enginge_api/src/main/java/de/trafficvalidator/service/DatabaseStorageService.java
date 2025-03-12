package de.trafficvalidator.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Database implementation of StorageService.
 * This is a placeholder implementation that can be expanded
 * when database storage is implemented.
 * 
 * Note: This service is not active by default. To activate it,
 * uncomment the @Primary annotation and implement the actual
 * database access logic.
 */
//@Primary
@Service
public class DatabaseStorageService implements StorageService {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseStorageService.class);
    
    // This would be your repository/DAO for database access
    // @Autowired
    // private ConfigurationRepository configRepository;
    
    private final ResourceLoader resourceLoader;
    
    @Autowired
    public DatabaseStorageService(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }
    
    @Override
    public InputStream getMapemFile(String id) {
        logger.info("Loading MAPEM from database for ID: {}", id);
        
        // This is where you would retrieve the MAPEM XML from the database
        // Example pseudocode:
        // ConfigurationEntity config = configRepository.findById(id);
        // return new ByteArrayInputStream(config.getMapemXml().getBytes());
        
        // For now, we'll throw an exception since this is not implemented
        throw new UnsupportedOperationException("Database storage not yet implemented");
    }
    
    @Override
    public InputStream getStgFile(String id) {
        logger.info("Loading STG from database for ID: {}", id);
        
        // This is where you would retrieve the STG content from the database
        // Example pseudocode:
        // ConfigurationEntity config = configRepository.findById(id);
        // return new ByteArrayInputStream(config.getStgContent().getBytes());
        
        // For now, we'll throw an exception since this is not implemented
        throw new UnsupportedOperationException("Database storage not yet implemented");
    }
    
    @Override
    public InputStream getRulesetFile(String rulesetName) {
        // For rulesets, we'll still use the file system since these are 
        // usually part of the application rather than user data
        try {
            String filePath = "rules/" + rulesetName + ".drl";
            logger.info("Loading ruleset file from classpath: {}", filePath);
            return resourceLoader.getResource("classpath:" + filePath).getInputStream();
        } catch (Exception e) {
            logger.error("Failed to load ruleset file: {}", rulesetName, e);
            throw new RuntimeException("Failed to load ruleset file: " + rulesetName, e);
        }
    }
    
    @Override
    public List<String> getAvailableIntersectionIds() {
        logger.info("Listing available intersection IDs from database");
        
        // This is where you would query the database for all available IDs
        // Example pseudocode:
        // return configRepository.findAllIds();
        
        // For now, return an empty list
        return new ArrayList<>();
    }
    
    @Override
    public List<String> getAvailableRulesets() {
        // For rulesets, we'll use the same implementation as FileStorageService
        // since these are typically not stored in the database
        try {
            List<String> rulesets = new ArrayList<>();
            // Add known rulesets here, or scan the classpath
            rulesets.add("cyclist-arrow");
            return rulesets;
        } catch (Exception e) {
            logger.error("Failed to list available rulesets", e);
            return new ArrayList<>();
        }
    }
}
package de.trafficvalidator.service;

import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.trafficvalidator.model.Intersection;
import de.trafficvalidator.parser.MapemParser;
import de.trafficvalidator.parser.StgParser;

/**
 * Service for loading intersection data.
 */
@Service
public class IntersectionService {
    private static final Logger logger = LoggerFactory.getLogger(IntersectionService.class);
    
    private final StorageService storageService;
    private final MapemParser mapemParser;
    
    @Autowired
    public IntersectionService(StorageService storageService) {
        this.storageService = storageService;
        this.mapemParser = new MapemParser();
    }
    
    /**
     * Loads and parses an intersection configuration
     *
     * @param id The ID of the intersection configuration
     * @return Parsed Intersection object
     * @throws Exception If parsing fails
     */
    public Intersection loadIntersection(String id) throws Exception {
        try {
            // Get MAPEM file
            InputStream mapemStream = storageService.getMapemFile(id);
            
            // Parse MAPEM
            Intersection intersection = mapemParser.parse(mapemStream);
            
            // Get STG file
            InputStream stgStream = storageService.getStgFile(id);
            
            // Parse STG and update intersection
            StgParser stgParser = new StgParser();
            stgParser.parse(stgStream);
            stgParser.updateIntersection(intersection);
            
            return intersection;
        } catch (Exception e) {
            logger.error("Failed to load intersection {}", id, e);
            throw new Exception("Failed to load intersection " + id + ": " + e.getMessage(), e);
        }
    }
} 
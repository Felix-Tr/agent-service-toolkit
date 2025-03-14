package de.trafficvalidator.parser;

import de.trafficvalidator.model.Intersection;
import de.trafficvalidator.model.SignalGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser for STG files that extracts signal group information.
 */
public class StgParser {
    private static final Logger logger = LoggerFactory.getLogger(StgParser.class);
    
    // Regex pattern for signal group data line
    private static final Pattern SIGNAL_GROUP_PATTERN = Pattern.compile(
            "\\s*(\\d+),\\s*'([^']+)',\\s*'([^']+)'.*");
    
    private Map<Integer, SignalGroup> signalGroups = new HashMap<>();
    
    /**
     * Parses an STG file and returns a map of signal groups by ID
     */
    public Map<Integer, SignalGroup> parse(File stgFile) throws Exception {
        logger.info("Parsing STG file: {}", stgFile.getName());
        
        try (BufferedReader reader = new BufferedReader(new FileReader(stgFile))) {
            parseReader(reader);
        }
        
        return signalGroups;
    }
    
    /**
     * Parses an STG file from input stream
     */
    public Map<Integer, SignalGroup> parse(InputStream stgStream) throws Exception {
        logger.info("Parsing STG from input stream");
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stgStream))) {
            parseReader(reader);
        }
        
        return signalGroups;
    }
    
    /**
     * Updates an intersection with signal group data
     */
    public void updateIntersection(Intersection intersection) {
        logger.info("Updating intersection with {} signal groups", signalGroups.size());
        
        for (SignalGroup signalGroup : signalGroups.values()) {
            // Update existing signal group or add new one
            SignalGroup existingGroup = intersection.getSignalGroup(signalGroup.getPhysicalSignalGroupId());
            if (existingGroup != null) {
                // Update existing group name and type
                existingGroup.setName(signalGroup.getName());
                existingGroup.setType(signalGroup.getType());
            } else {
                // Add new signal group
                intersection.addSignalGroup(signalGroup);
            }
        }
    }
    
    /**
     * Internal method to parse the file content
     */
    private void parseReader(BufferedReader reader) throws Exception {
        String line;
        boolean inSignalGroupSection = false;
        
        while ((line = reader.readLine()) != null) {
            // Check for section headers
            if (line.startsWith("#SIGNALGRUPPENDATEN")) {
                inSignalGroupSection = true;
                logger.debug("Found SIGNALGRUPPENDATEN section");
                continue;
            } else if (line.startsWith("#")) {
                inSignalGroupSection = false;
            }
            
            // Parse signal group data if in the correct section
            if (inSignalGroupSection) {
                parseSignalGroupLine(line);
            }
        }
        
        logger.info("Parsed {} signal groups", signalGroups.size());
    }
    
    /**
     * Parses a signal group line from the STG file
     */
    private void parseSignalGroupLine(String line) {
        // Skip empty lines or section headers
        if (line.trim().isEmpty() || line.contains("::")) {
            return;
        }
        
        // Match against regex pattern
        Matcher matcher = SIGNAL_GROUP_PATTERN.matcher(line);
        if (matcher.matches()) {
            int id = Integer.parseInt(matcher.group(1));
            String name = matcher.group(2);
            String typeStr = matcher.group(3);
            
            try {
                SignalGroup.SignalGroupType type = SignalGroup.parseType(typeStr);
                SignalGroup signalGroup = new SignalGroup(id, name, type);
                signalGroups.put(id, signalGroup);
                
                logger.debug("Parsed signal group: {} - {} ({})", id, name, type);
            } catch (IllegalArgumentException e) {
                logger.warn("Unknown signal group type: {} for ID: {}", typeStr, id);
            }
        }
    }
}
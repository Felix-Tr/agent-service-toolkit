package de.trafficvalidator.service;

import de.trafficvalidator.config.StorageConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * File system implementation of StorageService.
 * Reads configuration files from the file system or classpath resources.
 */
@Primary
@Service
public class FileStorageService implements StorageService {
    private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);
    
    private final StorageConfig storageConfig;
    private final ResourceLoader resourceLoader;
    
    @Autowired
    public FileStorageService(StorageConfig storageConfig, ResourceLoader resourceLoader) {
        this.storageConfig = storageConfig;
        this.resourceLoader = resourceLoader;
    }
    
    @Override
    public InputStream getMapemFile(String id) {
        try {
            String filePath = storageConfig.getBasePath() + "/" + id + "/mapem.xml";
            logger.info("Loading MAPEM file from: {}", filePath);
            Resource resource = resourceLoader.getResource("classpath:" + filePath);
            return resource.getInputStream();
        } catch (Exception e) {
            logger.error("Failed to load MAPEM file for ID: {}", id, e);
            throw new RuntimeException("Failed to load MAPEM file for ID: " + id, e);
        }
    }
    
    @Override
    public InputStream getStgFile(String id) {
        try {
            String filePath = storageConfig.getBasePath() + "/" + id + "/configuration.stg";
            logger.info("Loading STG file from: {}", filePath);
            Resource resource = resourceLoader.getResource("classpath:" + filePath);
            return resource.getInputStream();
        } catch (Exception e) {
            logger.error("Failed to load STG file for ID: {}", id, e);
            throw new RuntimeException("Failed to load STG file for ID: " + id, e);
        }
    }
    
    @Override
    public InputStream getRulesetFile(String rulesetName) {
        try {
            String filePath = storageConfig.getRulesPath() + "/" + rulesetName + ".drl";
            logger.info("Loading ruleset file from: {}", filePath);
            Resource resource = resourceLoader.getResource("classpath:" + filePath);
            return resource.getInputStream();
        } catch (Exception e) {
            logger.error("Failed to load ruleset file: {}", rulesetName, e);
            throw new RuntimeException("Failed to load ruleset file: " + rulesetName, e);
        }
    }
    
    @Override
    public List<String> getAvailableIntersectionIds() {
        try {
            // Use ResourceLoader to get the resource from classpath
            Resource baseResource = resourceLoader.getResource("classpath:" + storageConfig.getBasePath());
            
            // Check if the resource exists and is a directory
            if (baseResource.exists()) {
                File baseDir;
                try {
                    baseDir = baseResource.getFile();
                } catch (FileNotFoundException e) {
                    logger.warn("Resource path not found as a file: {}", storageConfig.getBasePath());
                    return new ArrayList<>();
                }
                
                if (baseDir.isDirectory()) {
                    File[] subDirs = baseDir.listFiles(File::isDirectory);
                    if (subDirs != null) {
                        List<String> ids = new ArrayList<>();
                        for (File dir : subDirs) {
                            File mapemFile = new File(dir, "mapem.xml");
                            File stgFile = new File(dir, "configuration.stg");
                            
                            if (mapemFile.exists() && stgFile.exists()) {
                                ids.add(dir.getName());
                            } else {
                                logger.debug("Directory {} does not contain required files (mapem.xml and configuration.stg)", dir.getName());
                            }
                        }
                        return ids;
                    }
                }
            }
            
            // If we can't access the directory as a file, try to list resources using the class path
            try {
                ClassPathResource resource = new ClassPathResource(storageConfig.getBasePath());
                if (resource.exists()) {
                    Path path = Paths.get(resource.getURI());
                    try (Stream<Path> paths = Files.list(path)) {
                        List<String> ids = new ArrayList<>();
                        List<Path> directories = paths.filter(Files::isDirectory).toList();
                        
                        for (Path dir : directories) {
                            Path mapemPath = dir.resolve("mapem.xml");
                            Path stgPath = dir.resolve("configuration.stg");
                            
                            if (Files.exists(mapemPath) && Files.exists(stgPath)) {
                                ids.add(dir.getFileName().toString());
                            }
                        }
                        return ids;
                    }
                }
            } catch (IOException e) {
                logger.warn("Failed to list resources using ClassPathResource: {}", e.getMessage());
            }
            
            logger.warn("No intersection configurations found in {}", storageConfig.getBasePath());
            return new ArrayList<>();
        } catch (Exception e) {
            logger.error("Failed to list available intersection IDs", e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public List<String> getAvailableRulesets() {
        try {
            Resource rulesResource = resourceLoader.getResource("classpath:" + storageConfig.getRulesPath());
            File rulesDir = rulesResource.getFile();
            
            if (rulesDir.exists() && rulesDir.isDirectory()) {
                File[] ruleFiles = rulesDir.listFiles(file -> 
                        file.isFile() && file.getName().endsWith(".drl"));
                
                if (ruleFiles != null) {
                    List<String> rulesets = new ArrayList<>();
                    for (File file : ruleFiles) {
                        String name = file.getName();
                        // Remove .drl extension
                        rulesets.add(name.substring(0, name.length() - 4));
                    }
                    return rulesets;
                }
            }
            
            logger.warn("No rulesets found in {}", storageConfig.getRulesPath());
            return new ArrayList<>();
        } catch (Exception e) {
            logger.error("Failed to list available rulesets", e);
            return new ArrayList<>();
        }
    }
}
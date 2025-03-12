package de.trafficvalidator.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for storage locations and settings
 */
@Configuration
@ConfigurationProperties(prefix = "storage")
public class StorageConfig {
    
    /**
     * Storage type: 'file' or 'database'
     */
    private String type = "file";
    
    /**
     * Base path for file storage
     */
    private String basePath = "configurations";
    
    /**
     * Rules directory path
     */
    private String rulesPath = "rules";
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getBasePath() {
        return basePath;
    }
    
    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }
    
    public String getRulesPath() {
        return rulesPath;
    }
    
    public void setRulesPath(String rulesPath) {
        this.rulesPath = rulesPath;
    }
}
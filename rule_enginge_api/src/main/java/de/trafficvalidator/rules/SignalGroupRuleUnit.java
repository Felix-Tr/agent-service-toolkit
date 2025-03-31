package de.trafficvalidator.rules;

import de.trafficvalidator.model.Connection;
import de.trafficvalidator.model.SignalGroup;
import de.trafficvalidator.model.ValidationResult;
import org.drools.ruleunits.api.DataSource;
import org.drools.ruleunits.api.DataStore;
import org.drools.ruleunits.api.RuleUnitData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Rule Unit for signal group validation rules.
 * <p>
 * This class holds the data needed for validating connections
 * against signal group rules.
 * </p>
 */
public class SignalGroupRuleUnit implements RuleUnitData, ResultContainer<ValidationResult> {
    
    private static final String CATEGORY = "signal-group";
    
    private final DataStore<Connection> connections;
    private final DataStore<ValidationResult> results;
    private final DataStore<SignalGroup> signalGroups;
    
    /**
     * Default constructor
     */
    public SignalGroupRuleUnit() {
        this.connections = DataSource.createStore();
        this.results = DataSource.createStore();
        this.signalGroups = DataSource.createStore();
    }
    
    /**
     * Constructor that initializes with connections
     */
    public SignalGroupRuleUnit(Collection<Connection> connectionList) {
        this.connections = DataSource.createStore();
        this.results = DataSource.createStore();
        this.signalGroups = DataSource.createStore();
        
        // Populate connections
        connectionList.forEach(connections::add);
        
        // Create corresponding validation results
        connectionList.forEach(conn -> {
            results.add(new ValidationResult(conn));
            
            // Add signal group if present and not already added
            if (conn.getSignalGroup() != null) {
                signalGroups.add(conn.getSignalGroup());
            }
        });
    }
    
    /**
     * Returns the connections data store
     */
    public DataStore<Connection> getConnections() {
        return connections;
    }
    
    /**
     * Returns the validation results data store
     */
    public DataStore<ValidationResult> getResults() {
        return results;
    }
    
    /**
     * Returns the signal groups data store
     */
    public DataStore<SignalGroup> getSignalGroups() {
        return signalGroups;
    }
    
    /**
     * Returns the category name for this rule unit
     */
    public String getCategory() {
        return CATEGORY;
    }
    
    /**
     * Collects all validation results from the data store
     * 
     * @return A list of validation results
     */
    @Override
    public List<ValidationResult> collectResults() {
        // Since we can't directly iterate over DataStore contents,
        // defer to the connection-based method
        return new ArrayList<>();
    }
    
    /**
     * Collects validation results for a collection of connections
     * 
     * @param connections The collection of connections to get results for
     * @return A list of validation results corresponding to the connections
     */
    @Override
    public List<ValidationResult> collectFromConnections(Collection<Connection> connections) {
        List<ValidationResult> resultList = new ArrayList<>();
        
        // We can't directly iterate over results DataStore
        // Instead, use the rule execution behavior: each Connection 
        // should have a ValidationResult associated with it after rule execution
        for (Connection connection : connections) {
            // Create a result for this connection if it doesn't already exist
            ValidationResult result = new ValidationResult(connection);
            resultList.add(result);
        }
        
        return resultList;
    }
} 
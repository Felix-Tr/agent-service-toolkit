package de.trafficvalidator.rules;

import de.trafficvalidator.model.Connection;
import de.trafficvalidator.model.ValidationResult;
import de.trafficvalidator.model.RuleExecution;
import org.drools.ruleunits.api.DataSource;
import org.drools.ruleunits.api.DataStore;
import org.drools.ruleunits.api.RuleUnitData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * Rule Unit for cyclist arrow validation rules.
 * <p>
 * This class holds the data for validating connections against
 * the green cyclist arrow sign (Verkehrszeichen 721) rules.
 * </p>
 */
public class CyclistArrowRuleUnit implements RuleUnitData {
    
    private static final String CATEGORY = "cyclist-arrow";
    
    private final DataStore<Connection> connections;
    private final DataStore<ValidationResult> results;
    private final DataStore<RuleExecution> executions;
    private final Map<Integer, ValidationResult> resultMap;
    
    /**
     * Default constructor
     */
    public CyclistArrowRuleUnit() {
        this.connections = DataSource.createStore();
        this.results = DataSource.createStore();
        this.executions = DataSource.createStore();
        this.resultMap = new HashMap<>();
    }
    
    /**
     * Constructor that initializes with connections
     */
    public CyclistArrowRuleUnit(Collection<Connection> connectionList) {
        this.connections = DataSource.createStore();
        this.results = DataSource.createStore();
        this.executions = DataSource.createStore();
        this.resultMap = new HashMap<>();
        
        // Populate connections and create corresponding validation results
        for (Connection connection : connectionList) {
            connections.add(connection);
            ValidationResult result = new ValidationResult(connection);
            results.add(result);
            resultMap.put(connection.getId(), result);
        }
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
     * Returns the executions data store
     */
    public DataStore<RuleExecution> getExecutions() {
        return executions;
    }
    
    /**
     * Adds a new connection and creates a validation result for it
     */
    public ValidationResult addConnection(Connection connection) {
        connections.add(connection);
        ValidationResult result = new ValidationResult(connection);
        results.add(result);
        resultMap.put(connection.getId(), result);
        return result;
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
    public List<ValidationResult> collectResults() {
        return new ArrayList<>(resultMap.values());
    }
    
    /**
     * Collects validation results for a collection of connections
     * 
     * @param connections The collection of connections to get results for
     * @return A list of validation results corresponding to the connections
     */
    public List<ValidationResult> collectFromConnections(Collection<Connection> connections) {
        List<ValidationResult> resultList = new ArrayList<>();
        for (Connection connection : connections) {
            ValidationResult result = resultMap.get(connection.getId());
            if (result != null) {
                resultList.add(result);
            }
        }
        return resultList;
    }

    public ValidationResult getResultForConnection(Connection connection) {
        return resultMap.get(connection.getId());
    }

    public String getExecutionSummary() {
        List<RuleExecution> executionList = new ArrayList<>();
        // Since we can't directly iterate over DataStore, we'll use the rule execution behavior
        // Each rule execution will be added to the executions DataStore during rule firing
        // The results will be available after rule execution
        
        Map<String, Long> ruleCountMap = executionList.stream()
                .collect(Collectors.groupingBy(
                    RuleExecution::getRuleName,
                    Collectors.counting()
                ));
        
        StringBuilder summary = new StringBuilder("Rules executed:\n");
        ruleCountMap.forEach((rule, count) -> 
            summary.append(String.format("- %s (executed %d times)\n", rule, count)));
            
        return summary.toString();
    }
} 
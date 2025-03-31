package de.trafficvalidator.rules;

import de.trafficvalidator.model.Connection;
import de.trafficvalidator.model.ValidationResult;

import java.util.Collection;
import java.util.List;

/**
 * Interface for rule units that provide access to validation results.
 * This interface allows for polymorphic access to results without using reflection.
 * 
 * @param <T> The type of result stored in the container
 */
public interface ResultContainer<T> {
    
    /**
     * Collects and returns all results from the rule unit's data store
     * 
     * @return A list containing all the results
     */
    List<T> collectResults();
    
    /**
     * Collects validation results for a collection of connections
     * This is particularly useful for DataStore implementation which may not 
     * provide standard iteration methods
     * 
     * @param connections The collection of connections to get results for
     * @return A list of validation results corresponding to the connections
     */
    List<ValidationResult> collectFromConnections(Collection<Connection> connections);
} 
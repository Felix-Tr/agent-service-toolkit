# Best Practices for Rule Engine vs. Domain Logic

Here's how to better separate responsibilities:

## What Should Be in the Rule Engine (DRL)
1. **Business rules and decision logic**: The actual conditions that determine whether a green cyclist arrow can be installed
2. **Pattern matching**: Finding relationships between facts (like conflicting connections)
3. **Rule-specific constraints**: Conditions specific to a particular rule

## What Should Be in the Domain Model

1. **Data structure and relationships**: How objects relate to each other
2. **Basic properties and accessors**: Simple getters/setters
3. **Fundamental domain behaviors**: Methods that represent core capabilities of domain objects

## What Should Be in the Service Layer
1. **Orchestration**: Setting up the rule engine, managing sessions
2. **Data preparation**: Loading and transforming data before rule execution
3. **Result handling**: Processing the outcomes of rule execution
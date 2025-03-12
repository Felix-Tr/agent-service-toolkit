package de.trafficvalidator.model;

/**
 * Represents the cardinal and intercardinal directions for connections
 * between lanes at an intersection.
 * 
 * TODO: May be better to have right, left straight in addition here and then add the cardinal directions on top. 
 */
public enum Direction {
    N,   // North
    NE,  // Northeast
    E,   // East
    SE,  // Southeast
    S,   // South
    SW,  // Southwest
    W,   // West
    NW;  // Northwest
    
    /**
     * Calculates the opposite direction (useful for finding opposite lanes)
     * @return the opposite direction
     */
    public Direction getOpposite() {
        switch (this) {
            case N: return S;
            case NE: return SW;
            case E: return W;
            case SE: return NW;
            case S: return N;
            case SW: return NE;
            case W: return E;
            case NW: return SE;
            default: throw new IllegalStateException("Unknown direction");
        }
    }
    
    /**
     * Returns the adjacent right direction (90 degrees clockwise)
     */
    public Direction getRightTurn() {
        switch (this) {
            case N: return E;
            case NE: return SE;
            case E: return S;
            case SE: return SW;
            case S: return W;
            case SW: return NW;
            case W: return N;
            case NW: return NE;
            default: throw new IllegalStateException("Unknown direction");
        }
    }
    
    /**
     * Returns the adjacent left direction (90 degrees counter-clockwise)
     */
    public Direction getLeftTurn() {
        switch (this) {
            case N: return W;
            case NE: return NW;
            case E: return N;
            case SE: return NE;
            case S: return E;
            case SW: return SE;
            case W: return S;
            case NW: return SW;
            default: throw new IllegalStateException("Unknown direction");
        }
    }
}
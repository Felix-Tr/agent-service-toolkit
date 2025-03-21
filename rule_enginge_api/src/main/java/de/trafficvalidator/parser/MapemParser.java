package de.trafficvalidator.parser;

import de.trafficvalidator.model.*;
import de.trafficvalidator.util.DirectionCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Parser for MAPEM XML files that extracts intersection data.
 */
public class MapemParser {
    private static final Logger logger = LoggerFactory.getLogger(MapemParser.class);

    private Intersection intersection;
    private Map<String, Connection> connectionsMap = new HashMap<>();
    private final Map<String, Connection> connectionKeyMap = new HashMap<>();
    // Map to store physical signal groups by ID
    private Map<Integer, SignalGroup> physicalSignalGroups = new HashMap<>();
    // Set to store ingress lane IDs that have traffic streams
    private Set<Integer> ingressLanesWithTrafficStreams = new HashSet<>();
    // Map to store signal group types from STG file
    private Map<Integer, SignalGroup> stgSignalGroups = new HashMap<>();

    /**
     * Parses a MAPEM XML from input stream
     */
    public Intersection parse(InputStream mapemStream) throws Exception {
        logger.info("Parsing MAPEM from input stream");

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(mapemStream);
        document.getDocumentElement().normalize();

        return parseDocument(document);
    }

    /**
     * Internal method to parse the XML document
     */
    private Intersection parseDocument(Document document) {
        try {
            // Parse intersection information
            parseIntersection(document);

            // Parse lanes
            parseLanes(document);

            // Parse connections
            parseConnections(document);

            // Parse traffic streams
            parseTrafficStreamsAddPhysicalSignalGroups(document);

            // Validate that all ingress lanes have signal groups (not all connections)
            validateIngressLaneSignalGroups();

            // Calculate directions for lanes
            DirectionCalculator calculator = new DirectionCalculator(intersection);
            calculator.calculateDirectionsForApproaches();

            logger.info("Parsed intersection: {}", intersection);
            return intersection;
        } catch (Exception e) {
            logger.error("Error parsing MAPEM document: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to parse MAPEM document: " + e.getMessage(), e);
        }
    }

    /**
     * Updates signal group types from STG data
     */
    private void updateSignalGroupTypesFromStg() {
        if (stgSignalGroups.isEmpty()) {
            logger.warn("No STG signal group data available, using fallback type determination");
            return;
        }

        for (SignalGroup signalGroup : intersection.getPhysicalSignalGroups().values()) {
            int id = signalGroup.getPhysicalSignalGroupId();
            SignalGroup stgSignalGroup = stgSignalGroups.get(id);
            
            if (stgSignalGroup != null) {
                // Update type and name from STG data
                signalGroup.setType(stgSignalGroup.getType());
                signalGroup.setName(stgSignalGroup.getName());
                logger.debug("Updated signal group {} type to {} from STG data", 
                    id, stgSignalGroup.getType());
            } else {
                // If not found in STG, use fallback determination
                SignalGroup.SignalGroupType fallbackType = determineSignalGroupTypeFallback(id);
                signalGroup.setType(fallbackType);
                logger.warn("Signal group {} not found in STG data, using fallback type {}", 
                    id, fallbackType);
            }
        }
    }

    /**
     * Parses basic intersection information
     */
    private void parseIntersection(Document document) {
        // Extract intersection ID and region
        NodeList idNodes = document.getElementsByTagName("DSRC:id");
        if (idNodes.getLength() > 0) {
            Element idElement = (Element) idNodes.item(0);

            int region = Integer.parseInt(getTagContent(idElement, "DSRC:region"));
            int id = Integer.parseInt(getTagContent(idElement, "DSRC:id"));

            intersection = new Intersection(id, region);
            logger.info("Found intersection with ID: {} in region: {}", id, region);
        } else {
            // Create a default intersection if no ID found
            intersection = new Intersection(1, 1);
            logger.warn("No intersection ID found, using default");
        }

        // Extract intersection name
        NodeList nameNodes = document.getElementsByTagName("DSRC:name");
        if (nameNodes.getLength() > 0) {
            String name = nameNodes.item(0).getTextContent().trim();
            intersection.setName(name);
        }

        // Extract revision number
        NodeList revisionNodes = document.getElementsByTagName("DSRC:revision");
        if (revisionNodes.getLength() > 0) {
            int revision = Integer.parseInt(revisionNodes.item(0).getTextContent().trim());
            intersection.setRevision(revision);
        }

        // Extract reference point
        NodeList refPointNodes = document.getElementsByTagName("DSRC:refPoint");
        if (refPointNodes.getLength() > 0) {
            Element refPointElement = (Element) refPointNodes.item(0);

            double lat = Double.parseDouble(getTagContent(refPointElement, "DSRC:lat"));
            double lon = Double.parseDouble(getTagContent(refPointElement, "DSRC:long"));

            intersection.setRefLat(lat);
            intersection.setRefLong(lon);
        }
    }

    /**
     * Parses lanes information
     */
    private void parseLanes(Document document) {
        NodeList laneNodes = document.getElementsByTagName("DSRC:GenericLane");
        logger.info("Found {} lanes", laneNodes.getLength());

        for (int i = 0; i < laneNodes.getLength(); i++) {
            Element laneElement = (Element) laneNodes.item(i);

            // Extract lane ID
            int laneId = Integer.parseInt(getTagContent(laneElement, "DSRC:laneID"));
            Lane lane = new Lane(laneId);

            // Extract lane name
            String laneName = getTagContent(laneElement, "DSRC:name");
            if (laneName != null && !laneName.isEmpty()) {
                lane.setName(laneName);
            }

            // Extract ingress/egress information
            String ingressApproach = getTagContent(laneElement, "DSRC:ingressApproach");
            String egressApproach = getTagContent(laneElement, "DSRC:egressApproach");

            if (ingressApproach != null && !ingressApproach.isEmpty()) {
                lane.setIngress(true);
                lane.setApproachId(Integer.parseInt(ingressApproach));
            }

            if (egressApproach != null && !egressApproach.isEmpty()) {
                lane.setEgress(true);
                lane.setApproachId(Integer.parseInt(egressApproach));
            }

            // Extract lane attributes
            Element laneAttributesElement = (Element) laneElement.getElementsByTagName("DSRC:laneAttributes").item(0);
            if (laneAttributesElement != null) {
                // Extract shared with flags
                String sharedWith = getTagContent(laneAttributesElement, "DSRC:sharedWith");
                parseSharedWith(lane, sharedWith);

                // Extract lane type
                Element laneTypeElement = (Element) laneAttributesElement.getElementsByTagName("DSRC:laneType").item(0);
                if (laneTypeElement != null) {
                    parseLaneType(lane, laneTypeElement);
                }
            }

            // Extract node coordinates with stop line information
            extractNodeCoordinates(laneElement, lane);

            intersection.addLane(lane);
            logger.debug("Added lane: {}", lane);
        }
    }

    /**
     * Parses the sharedWith attribute for a lane
     */
    private void parseSharedWith(Lane lane, String sharedWith) {
        if (sharedWith == null || sharedWith.isEmpty()) {
            return;
        }

        // Remove any spaces or underscores
        sharedWith = sharedWith.replaceAll("[\\s_]", "");

        // In ASN.1 notation, bits are numbered from 0 (leftmost) to n-1 (rightmost)
        // We need to check specific bit positions based on the ASN.1 definition
        
        // Convert to char array for easier bit access
        char[] bits = sharedWith.toCharArray();
        
        // Check for individualMotorizedVehicleTraffic (bit 3)
        if (bits.length > 3 && bits[3] == '1') {
            lane.setAllowsIndividualMotorizedVehicles(true);
        }
        
        // Check for busVehicleTraffic (bit 4)
        if (bits.length > 4 && bits[4] == '1') {
            lane.setAllowsPublicTransport(true);
        }
        
        // Check for pedestriansTraffic (bit 6)
        if (bits.length > 6 && bits[6] == '1') {
            lane.setAllowsPedestrians(true);
        }
        
        // Check for cyclistVehicleTraffic (bit 7)
        if (bits.length > 7 && bits[7] == '1') {
            lane.setAllowsCyclists(true);
        }
    }

    /**
     * Parses the lane type information
     */
    private void parseLaneType(Lane lane, Element laneTypeElement) {
        // Check for each possible lane type
        if (laneTypeElement.getElementsByTagName("DSRC:vehicle").getLength() > 0) {
            lane.setVehicleLane(true);
        }

        if (laneTypeElement.getElementsByTagName("DSRC:bikeLane").getLength() > 0) {
            lane.setBikeLane(true);
            lane.setAllowsCyclists(true);
        }

        if (laneTypeElement.getElementsByTagName("DSRC:crosswalk").getLength() > 0) {
            lane.setCrosswalk(true);
            lane.setAllowsPedestrians(true);
        }
    }

    /**
     * Parses connections between lanes
     */
    private void parseConnections(Document document) {
        // Find all connecting lanes
        NodeList connectsToNodes = document.getElementsByTagName("DSRC:connectsTo");
        logger.info("Found {} potential connection groups", connectsToNodes.getLength());

        for (int i = 0; i < connectsToNodes.getLength(); i++) {
            Element connectsToElement = (Element) connectsToNodes.item(i);
            Element parentLaneElement = (Element) connectsToElement.getParentNode();

            // Get parent lane ID
            int laneId = Integer.parseInt(getTagContent(parentLaneElement, "DSRC:laneID"));
            Lane ingressLane = intersection.getLane(laneId);

            if (ingressLane == null) {
                logger.warn("Could not find ingress lane with ID: {}", laneId);
                continue;
            }

            // Process each connection
            NodeList connectionNodes = connectsToElement.getElementsByTagName("DSRC:Connection");
            for (int j = 0; j < connectionNodes.getLength(); j++) {
                Element connectionElement = (Element) connectionNodes.item(j);

                // Get connection details
                Element connectingLaneElement = (Element) connectionElement.getElementsByTagName("DSRC:connectingLane").item(0);
                int targetLaneId = Integer.parseInt(getTagContent(connectingLaneElement, "DSRC:lane"));
                String maneuvers = getTagContent(connectingLaneElement, "DSRC:maneuver");

                Lane egressLane = intersection.getLane(targetLaneId);
                if (egressLane == null) {
                    logger.warn("Could not find egress lane with ID: {}", targetLaneId);
                    continue;
                }

                // Create connection
                Connection connection = new Connection(ingressLane, egressLane);

                // Set maneuvers from binary string
                setManeuversFromBinary(connection, maneuvers);

                // Set connection ID
                String connectionIdStr = getTagContent(connectionElement, "DSRC:connectionID");
                if (connectionIdStr != null && !connectionIdStr.isEmpty()) {
                    int connectionId = Integer.parseInt(connectionIdStr);
                    
                    // Ensure unique connection IDs for pedestrian crossings
                    if (ingressLane.isCrosswalk() || egressLane.isCrosswalk()) {
                        // Generate a unique ID for pedestrian crossings by adding 1000 to the original ID
                        int pedestrianConnectionId = 1000 + connectionId;
                        connection.setConnectionId(pedestrianConnectionId);
                        connection.setId(pedestrianConnectionId);
                        logger.debug("Generated unique ID {} for pedestrian crossing connection (original ID: {})",
                                pedestrianConnectionId, connectionId);
                    } else {
                        // For vehicle lanes, use the original connection ID
                        connection.setConnectionId(connectionId);
                        connection.setId(connectionId);
                        int logicalSignalGroupId = Integer.parseInt(getTagContent(connectionElement, "DSRC:signalGroup"));
                        connection.setLogicalSignalGroupId(logicalSignalGroupId);
                    }
                } else {
                    throw new RuntimeException("MAP corrupted as it has connection with missing ID");
                }

                intersection.addConnection(connection);
                
                // Store connection in both maps for fast lookup
                String connectionKey = ingressLane.getId() + "-" + egressLane.getId();
                connectionsMap.put(connectionKey, connection);
                
                String lookupKey = ingressLane.getId() + ":" + egressLane.getId();
                connectionKeyMap.put(lookupKey, connection);
                
                logger.debug("Added connection: {}", connection);
            }
        }
    }

    /**
     * Sets maneuver flags from binary string
     */
    private void setManeuversFromBinary(Connection connection, String maneuvers) {
        if (maneuvers == null || maneuvers.isEmpty()) {
            return;
        }

        // Use the non-deprecated method to set maneuvers
        connection.setManeuversFromBinary(maneuvers);
    }

    /**
     * Parses traffic streams from the MAPEM file
     * Traffic streams contain physical signal group information (<vt> tags)
     */
    private void parseTrafficStreamsAddPhysicalSignalGroups(Document document) {
        NodeList trafficStreamNodes = document.getElementsByTagName("MapExtension:TrafficStreamConfigData");
        logger.info("Found {} traffic streams", trafficStreamNodes.getLength());

        for (int i = 0; i < trafficStreamNodes.getLength(); i++) {
            Element streamElement = (Element) trafficStreamNodes.item(i);

            try {
                TrafficStream trafficStream = new TrafficStream();

                // Parse reference lane and connection
                int refLaneId = Integer.parseInt(getTagContent(streamElement, "MapExtension:refLaneId"));
                int refConnectTo = Integer.parseInt(getTagContent(streamElement, "MapExtension:refConnectTo"));

                trafficStream.setRefLaneId(refLaneId);
                trafficStream.setRefConnectTo(refConnectTo);

                // Add this lane to our set of ingress lanes with traffic streams
                ingressLanesWithTrafficStreams.add(refLaneId);

                // Set lane references
                Lane refLane = intersection.getLane(refLaneId);
                Lane connectToLane = intersection.getLane(refConnectTo);

                if (refLane != null && connectToLane != null) {
                    trafficStream.setRefLane(refLane);
                    trafficStream.setConnectToLane(connectToLane);
                } else {
                    throw new RuntimeException(String.format("Could not find lanes for traffic stream: %d -> %d", refLaneId, refConnectTo));
                }

                // Parse intersection part
                String intersectionPart = getTagContent(streamElement, "MapExtension:intersectionPart");
                if (intersectionPart != null && !intersectionPart.isEmpty()) {
                    trafficStream.setIntersectionPart(Integer.parseInt(intersectionPart));
                }

                // Parse signal groups - focus on both primary and secondary <vt> IDs and map to physicalsignalgroups for all connections
                parseSignalGroupsForTrafficStreamAndAddToConnection(streamElement, trafficStream, refLaneId, refConnectTo);

                intersection.addTrafficStream(trafficStream);
                logger.debug("Added traffic stream: {}", trafficStream);
            } catch (Exception e) {
                throw new RuntimeException("Error parsing traffic stream element: {}", e);
            }
        }
    }

    /**
     * Parses signal groups for a traffic stream
     */
    private void parseSignalGroupsForTrafficStreamAndAddToConnection(Element streamElement, TrafficStream trafficStream,
                                                                     int refLaneId, int refConnectTo) {
        Element signalGroupsElement = (Element) streamElement.getElementsByTagName("MapExtension:signalGroups").item(0);
        if (signalGroupsElement != null) {
            // First try to find primary signal group
            int physicalSignalGroupId = parseSignalGroupId(signalGroupsElement, "MapExtension:primary");
            boolean isPrimary = physicalSignalGroupId > 0;

            // If no primary found, look for secondary
            if (physicalSignalGroupId == 0) {
                physicalSignalGroupId = parseSignalGroupId(signalGroupsElement, "MapExtension:secondary");
                isPrimary = false;
            }

            if (physicalSignalGroupId > 0) {
                trafficStream.setPhysicalSignalGroupId(physicalSignalGroupId);
                trafficStream.setPrimary(isPrimary);
                
                // Create or get the physical signal group
                SignalGroup signalGroup = getOrCreatePhysicalSignalGroup(physicalSignalGroupId);
                
                // Find the immediate connection for this traffic stream
                Connection connection = findConnection(refLaneId, refConnectTo);
                
                if (connection != null) {
                    // Get the logical signal group ID of this connection
                    int logicalSignalGroupId = connection.getLogicalSignalGroupId();
                    
                    // Find all connections with the same logical signal group ID
                    List<Connection> connWithSameLogicalGroup = intersection.getConnectionsByLogicalSignalGroupId(logicalSignalGroupId);
                    
                    // Apply the physical signal group ID to all these connections
                    // and add them all to the traffic stream
                    for (Connection conn : connWithSameLogicalGroup) {
                        // Add physical signal group ID to each connection
                        conn.addPhysicalSignalGroupId(physicalSignalGroupId);
                        
                        // Link each connection to the signal group
                        signalGroup.addControlledConnection(conn);
                        
                        // Add each connection to the traffic stream
                        trafficStream.addConnection(conn);
                        
                        logger.debug("Linked connection {} to physical signal group {} via logical group {}", 
                                conn.getId(), physicalSignalGroupId, logicalSignalGroupId);
                    }
                    
                    // Link the signal group to traffic stream
                    trafficStream.linkToSignalGroup(signalGroup);
                    
                    logger.debug("Linked traffic stream with signal group {} for connections with logical group {}", 
                            physicalSignalGroupId, logicalSignalGroupId);
                } else {
                    logger.warn("Could not find connection for traffic stream: {} -> {}", refLaneId, refConnectTo);
                }
            } else {
                logger.warn("No physical signal group ID found for traffic stream: {} -> {}", refLaneId, refConnectTo);
            }
        }
    }

    /**
     * Parse a signal group ID from element
     */
    private int parseSignalGroupId(Element signalGroupsElement, String tagName) {
        Element element = (Element) signalGroupsElement.getElementsByTagName(tagName).item(0);
        if (element != null) {
            String physicalSignalGroupIdStr = getTagContent(element, "MapExtension:vt");
            if (physicalSignalGroupIdStr != null && !physicalSignalGroupIdStr.isEmpty()) {
                try {
                    return Integer.parseInt(physicalSignalGroupIdStr);
                } catch (NumberFormatException e) {
                    logger.warn("Invalid physical signal group ID: {}", physicalSignalGroupIdStr);
                }
            }
        }
        return 0;
    }

    /**
     * Establishes all linkages between connections, traffic streams, and physical signal groups
     */
    private void establishSignalGroupLinkages() {
        logger.info("Establishing signal group linkages");
        
        // For each traffic stream, link the physical signal group to all connected connections
        for (TrafficStream stream : intersection.getTrafficStreams()) {
            int physicalSignalGroupId = stream.getPhysicalSignalGroupId();
            
            if (physicalSignalGroupId > 0) {
                // Get the signal group
                SignalGroup signalGroup = intersection.getPhysicalSignalGroup(physicalSignalGroupId);
                
                if (signalGroup == null) {
                    // Create and add if not found
                    signalGroup = getOrCreatePhysicalSignalGroup(physicalSignalGroupId);
                }
                
                // Find the initial connection for this stream
                Connection connection = findConnection(stream.getRefLaneId(), stream.getRefConnectTo());
                
                if (connection != null) {
                    // Get logical signal group ID
                    int logicalSignalGroupId = connection.getLogicalSignalGroupId();
                    
                    // Find all connections with the same logical signal group ID
                    List<Connection> connectionsWithSameLogicalGroup = 
                            intersection.getConnectionsByLogicalSignalGroupId(logicalSignalGroupId);
                    
                    for (Connection conn : connectionsWithSameLogicalGroup) {
                        // Link each connection to the signal group
                        conn.addSignalGroup(signalGroup);
                        signalGroup.addControlledConnection(conn);
                        
                        // Add each connection to the traffic stream
                        stream.addConnection(conn);
                        
                        logger.debug("Linked connection {} to physical signal group {} via logical group {}", 
                            conn.getId(), physicalSignalGroupId, logicalSignalGroupId);
                    }
                }
            }
        }
    }

    /**
     * Validates that all ingress lanes with traffic streams have at least one connection with a signal group
     * @throws RuntimeException if any ingress lane with traffic streams has no connections with signal groups
     */
    private void validateIngressLaneSignalGroups() {
        List<Integer> lanesWithoutSignalGroups = new ArrayList<>();
        
        for (Integer laneId : ingressLanesWithTrafficStreams) {
            Lane lane = intersection.getLane(laneId);
            if (lane == null) continue;
            
            boolean hasSignalGroup = false;
            
            // Check if any traffic stream for this lane has connections with signal groups
            for (TrafficStream stream : intersection.getTrafficStreams()) {
                if (stream.getRefLaneId() == laneId && !stream.getConnections().isEmpty()) {
                    hasSignalGroup = true;
                    break;
                }
            }
            
            // If no traffic stream with connections found, check all connections from this lane
            if (!hasSignalGroup) {
                for (Connection connection : intersection.getConnections()) {
                    if (connection.getIngressLane() != null && 
                        connection.getIngressLane().getId() == laneId &&
                        connection.hasSignalGroups()) {
                        hasSignalGroup = true;
                        break;
                    }
                }
            }
            
            if (!hasSignalGroup) {
                lanesWithoutSignalGroups.add(laneId);
                logger.error("Ingress lane {} has no connections with physical signal groups", laneId);
            }
        }
        
        if (!lanesWithoutSignalGroups.isEmpty()) {
            StringBuilder errorMsg = new StringBuilder("The following ingress lanes have no connections with physical signal groups: ");
            for (int i = 0; i < lanesWithoutSignalGroups.size(); i++) {
                if (i > 0) errorMsg.append(", ");
                errorMsg.append(lanesWithoutSignalGroups.get(i));
            }
            throw new RuntimeException(errorMsg.toString());
        }
        
        // Log connections without signal groups, but don't fail validation
        logConnectionsWithoutSignalGroups();
    }
    
    /**
     * Logs connections that don't have signal groups (for informational purposes)
     */
    private void logConnectionsWithoutSignalGroups() {
        List<String> connectionsWithoutSignalGroups = new ArrayList<>();
        
        for (Connection connection : intersection.getConnections()) {
            if (!connection.hasSignalGroups()) {
                String connInfo = connection.getId() + " (lane " + 
                    (connection.getIngressLane() != null ? connection.getIngressLane().getId() : "unknown") + 
                    " → " + 
                    (connection.getEgressLane() != null ? connection.getEgressLane().getId() : "unknown") + ")";
                connectionsWithoutSignalGroups.add(connInfo);
            }
        }
        
        if (!connectionsWithoutSignalGroups.isEmpty()) {
            logger.info("The following connections have no physical signal groups (this is informational only): {}", 
                String.join(", ", connectionsWithoutSignalGroups));
        }
    }

    /**
     * Gets or creates a physical signal group with the given ID
     */
    private SignalGroup getOrCreatePhysicalSignalGroup(int physicalSignalGroupId) {
        // Check if already created
        SignalGroup signalGroup = intersection.getPhysicalSignalGroup(physicalSignalGroupId);
        
        if (signalGroup == null) {
            // First check if we have type information from STG file
            SignalGroup stgSignalGroup = stgSignalGroups.get(physicalSignalGroupId);
            
            if (stgSignalGroup != null) {
                // If found in STG, use name and type from there
                signalGroup = new SignalGroup(
                    physicalSignalGroupId, 
                    stgSignalGroup.getName(), 
                    stgSignalGroup.getType()
                );
                logger.debug("Created signal group {} from STG data with type {}", 
                    physicalSignalGroupId, stgSignalGroup.getType());
            } else {
                // Otherwise determine type based on ID pattern
                SignalGroup.SignalGroupType type = determineSignalGroupTypeFallback(physicalSignalGroupId);
                String name = "SG" + physicalSignalGroupId;
                
                signalGroup = new SignalGroup(physicalSignalGroupId, name, type);
                logger.debug("Created signal group {} with fallback type {}", physicalSignalGroupId, type);
            }
            
            // Add to intersection and to our local map
            intersection.addPhysicalSignalGroup(signalGroup);
            physicalSignalGroups.put(physicalSignalGroupId, signalGroup);
        }
        
        return signalGroup;
    }

    /**
     * Fallback method to determine signal group type based on ID when STG data is not available
     */
    private SignalGroup.SignalGroupType determineSignalGroupTypeFallback(int physicalSignalGroupId) {
        // Simple fallback logic when STG data is not available
        if (physicalSignalGroupId >= 14 && physicalSignalGroupId <= 28) {
            // IDs 14-28 are typically pedestrian crossings (FG)
            return SignalGroup.SignalGroupType.FG;
        } else if (physicalSignalGroupId >= 10 && physicalSignalGroupId <= 13) {
            // IDs 10-13 are typically for cyclists (RD)
            return SignalGroup.SignalGroupType.RD;
        } else {
            // Default to vehicle traffic (FV)
            return SignalGroup.SignalGroupType.FV;
        }
    }

    /**
     * Finds a connection based on ingress and egress lane IDs
     */
    private Connection findConnection(int ingressLaneId, int egressLaneId) {
        // First try the conventional format from connectionsMap
        String connectionKey = ingressLaneId + "-" + egressLaneId;
        Connection connection = connectionsMap.get(connectionKey);
        
        if (connection != null) {
            return connection;
        }
        
        // Try the alternate format from connectionKeyMap
        String lookupKey = ingressLaneId + ":" + egressLaneId;
        connection = connectionKeyMap.get(lookupKey);
        
        if (connection != null) {
            return connection;
        }
        
        // Fallback to linear search if not found in maps
        for (Connection conn : intersection.getConnections()) {
            if (conn.getIngressLane() != null && conn.getEgressLane() != null &&
                conn.getIngressLane().getId() == ingressLaneId &&
                conn.getEgressLane().getId() == egressLaneId) {
                // Cache it for future lookups in both formats
                connectionsMap.put(connectionKey, conn);
                connectionKeyMap.put(lookupKey, conn);
                return conn;
            }
        }
        
        return null;
    }

    /**
     * Extract node coordinates
     */
    private void extractNodeCoordinates(Element laneElement, Lane lane) {
        NodeList nodeXYNodes = laneElement.getElementsByTagName("DSRC:NodeXY");
        for (int j = 0; j < nodeXYNodes.getLength(); j++) {
            Element nodeXYElement = (Element) nodeXYNodes.item(j);
            Element deltaElement = (Element) nodeXYElement.getElementsByTagName("DSRC:delta").item(0);

            if (deltaElement != null) {
                // The node coordinates can be in different formats (node-XY1, node-XY2, etc.)
                NodeList deltaChildren = deltaElement.getChildNodes();
                for (int k = 0; k < deltaChildren.getLength(); k++) {
                    Node childNode = deltaChildren.item(k);
                    if (childNode.getNodeType() == Node.ELEMENT_NODE &&
                            childNode.getNodeName().startsWith("DSRC:node-XY")) {

                        Element xyElement = (Element) childNode;
                        int x = Integer.parseInt(getTagContent(xyElement, "DSRC:x"));
                        int y = Integer.parseInt(getTagContent(xyElement, "DSRC:y"));

                        // Check if this node is a stop line
                        boolean isStopLine = false;
                        Element attributesElement = (Element) nodeXYElement.getElementsByTagName("DSRC:attributes").item(0);
                        if (attributesElement != null) {
                            NodeList localNodeList = attributesElement.getElementsByTagName("DSRC:localNode");
                            if (localNodeList.getLength() > 0) {
                                Element localNodeElement = (Element) localNodeList.item(0);
                                isStopLine = localNodeElement.getElementsByTagName("DSRC:stopLine").getLength() > 0;
                            }
                        }

                        lane.addNode(x, y, isStopLine);
                        
                        if (isStopLine) {
                            logger.debug("Found stop line for lane {}: ({}, {})", lane.getId(), x, y);
                        }
                    }
                }
            }
        }
    }

    /**
     * Utility method to get tag content from an element
     */
    private String getTagContent(Element element, String tagName) {
        NodeList nodes = element.getElementsByTagName(tagName);
        if (nodes.getLength() > 0) {
            return nodes.item(0).getTextContent().trim();
        }
        return null;
    }
}
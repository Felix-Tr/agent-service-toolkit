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
import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

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

    /**
     * Parses a MAPEM XML file and returns an Intersection object
     */
    public Intersection parse(File mapemFile) throws Exception {
        logger.info("Parsing MAPEM file: {}", mapemFile.getName());

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(mapemFile);
        document.getDocumentElement().normalize();

        return parseDocument(document);
    }

    /**
     * Parses a MAPEM XML from input stream
     */
    public Intersection parse(InputStream mapemStream) throws Exception {
        logger.info("Parsing MAPEM from input stream");

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(mapemStream);
        document.getDocumentElement().normalize();

        return parseDocument(document);
    }

    /**
     * Internal method to parse the XML document
     */
    private Intersection parseDocument(Document document) {
        // Parse intersection information
        parseIntersection(document);

        // Parse lanes
        parseLanes(document);

        // Parse connections
        parseConnections(document);

        // Parse traffic streams
        parseTrafficStreams(document);

        // Establish all physical signal group linkages
        establishSignalGroupLinkages();

        // Calculate directions for lanes
        DirectionCalculator calculator = new DirectionCalculator(intersection);
        calculator.calculateDirectionsForApproaches();

        logger.info("Parsed intersection: {}", intersection);
        return intersection;
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

        // The sharedWith attribute is a binary string where each bit represents a permission
        // Example: "0001000100" means individualMotorizedVehicleTraffic (bit 3) and cyclistVehicleTraffic (bit 7)

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

                // Set maneuvers from binary string (focus on first 4 bits as per specification)
                setManeuversFromBinary(connection, maneuvers);

                // Set connection ID
                String connectionIdStr = getTagContent(connectionElement, "DSRC:connectionID");
                if (connectionIdStr != null && !connectionIdStr.isEmpty()) {
                    int connectionId = Integer.parseInt(connectionIdStr);
                    connection.setConnectionId(connectionId);
                    connection.setId(connectionId); // Use connectionID as the main ID
                }

                // Handle user class for cyclists if present
                String userClassStr = getTagContent(connectionElement, "DSRC:userClass");
                if (userClassStr != null && !userClassStr.isEmpty()) {
                    int userClass = Integer.parseInt(userClassStr);
                    // UserClass 0 is for all users
                    if (userClass == 0) {
                        connection.setAllowsCyclists(ingressLane.allowsCyclists() || egressLane.allowsCyclists());
                    }
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
     * Focuses on the first 4 bits as specified in the DSRC:maneuver field documentation
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
    private void parseTrafficStreams(Document document) {
        NodeList trafficStreamNodes = document.getElementsByTagName("MapExtension:TrafficStreamConfigData");
        logger.info("Found {} traffic streams", trafficStreamNodes.getLength());

        for (int i = 0; i < trafficStreamNodes.getLength(); i++) {
            Element streamElement = (Element) trafficStreamNodes.item(i);

            TrafficStream trafficStream = new TrafficStream();

            // Parse reference lane and connection
            int refLaneId = Integer.parseInt(getTagContent(streamElement, "MapExtension:refLaneId"));
            int refConnectTo = Integer.parseInt(getTagContent(streamElement, "MapExtension:refConnectTo"));

            trafficStream.setRefLaneId(refLaneId);
            trafficStream.setRefConnectTo(refConnectTo);

            // Set lane references
            Lane refLane = intersection.getLane(refLaneId);
            Lane connectToLane = intersection.getLane(refConnectTo);

            if (refLane != null && connectToLane != null) {
                trafficStream.setRefLane(refLane);
                trafficStream.setConnectToLane(connectToLane);
            } else {
                logger.warn("Could not find lanes for traffic stream: {} -> {}", refLaneId, refConnectTo);
                continue; // Skip this traffic stream if lanes not found
            }

            // Parse intersection part
            String intersectionPart = getTagContent(streamElement, "MapExtension:intersectionPart");
            if (intersectionPart != null && !intersectionPart.isEmpty()) {
                trafficStream.setIntersectionPart(Integer.parseInt(intersectionPart));
            }

            // Parse signal groups - focus only on the <vt> IDs (physical signal groups)
            int physicalSignalGroupId = 0;
            boolean isPrimary = false;
            
            Element signalGroupsElement = (Element) streamElement.getElementsByTagName("MapExtension:signalGroups").item(0);
            if (signalGroupsElement != null) {
                // Check for primary physical signal groups
                Element primaryElement = (Element) signalGroupsElement.getElementsByTagName("MapExtension:primary").item(0);
                if (primaryElement != null) {
                    String physicalSignalGroupIdStr = getTagContent(primaryElement, "MapExtension:vt");
                    if (physicalSignalGroupIdStr != null && !physicalSignalGroupIdStr.isEmpty()) {
                        physicalSignalGroupId = Integer.parseInt(physicalSignalGroupIdStr);
                        isPrimary = true;
                    }
                }

                // Check for secondary physical signal groups (if no primary found)
                if (physicalSignalGroupId == 0) {
                    Element secondaryElement = (Element) signalGroupsElement.getElementsByTagName("MapExtension:secondary").item(0);
                    if (secondaryElement != null) {
                        String physicalSignalGroupIdStr = getTagContent(secondaryElement, "MapExtension:vt");
                        if (physicalSignalGroupIdStr != null && !physicalSignalGroupIdStr.isEmpty()) {
                            physicalSignalGroupId = Integer.parseInt(physicalSignalGroupIdStr);
                            isPrimary = false;
                        }
                    }
                }
            }

            if (physicalSignalGroupId > 0) {
                trafficStream.setPhysicalSignalGroupId(physicalSignalGroupId);
                trafficStream.setPrimary(isPrimary);
                
                // Create or get the physical signal group
                SignalGroup signalGroup = getOrCreatePhysicalSignalGroup(physicalSignalGroupId);
                
                // Find connection for this traffic stream
                Connection connection = findConnection(refLaneId, refConnectTo);
                
                if (connection != null) {
                    // Set physical signal group ID on the connection
                    connection.setPhysicalSignalGroupId(physicalSignalGroupId);
                    
                    // Link the connection to traffic stream
                    trafficStream.setConnection(connection);
                    
                    // Link the signal group to traffic stream
                    trafficStream.setSignalGroup(signalGroup);
                } else {
                    logger.warn("Could not find connection for traffic stream: {} -> {}", refLaneId, refConnectTo);
                }
            } else {
                logger.warn("No physical signal group ID found for traffic stream: {} -> {}", refLaneId, refConnectTo);
            }

            intersection.addTrafficStream(trafficStream);
            logger.debug("Added traffic stream: {}", trafficStream);
        }
    }

    /**
     * Establishes all linkages between connections, traffic streams, and physical signal groups
     */
    private void establishSignalGroupLinkages() {
        logger.info("Establishing signal group linkages");
        
        // For each traffic stream, link the physical signal group to the connection
        for (TrafficStream stream : intersection.getTrafficStreams()) {
            int physicalSignalGroupId = stream.getPhysicalSignalGroupId();
            
            if (physicalSignalGroupId > 0) {
                // Get the signal group
                SignalGroup signalGroup = intersection.getPhysicalSignalGroup(physicalSignalGroupId);
                
                if (signalGroup == null) {
                    // Create and add if not found
                    signalGroup = getOrCreatePhysicalSignalGroup(physicalSignalGroupId);
                }
                
                // Find the connection for this stream
                Connection connection = findConnection(stream.getRefLaneId(), stream.getRefConnectTo());
                
                if (connection != null) {
                    // Link the connection to the signal group
                    connection.setSignalGroup(signalGroup);
                    signalGroup.addControlledConnection(connection);
                    
                    // Set physical signal group ID on connection again for clarity
                    connection.setPhysicalSignalGroupId(physicalSignalGroupId);
                    
                    logger.debug("Linked connection {} to physical signal group {}", 
                        connection.getId(), physicalSignalGroupId);
                }
            }
        }
        
        // Validate and log results
        for (Connection connection : intersection.getConnections()) {
            if (connection.getSignalGroup() == null) {
                logger.warn("Connection {} has no signal group", connection.getId());
            }
        }
    }

    /**
     * Gets or creates a physical signal group with the given ID
     */
    private SignalGroup getOrCreatePhysicalSignalGroup(int physicalSignalGroupId) {
        // Check if already created
        SignalGroup signalGroup = intersection.getPhysicalSignalGroup(physicalSignalGroupId);
        
        if (signalGroup == null) {
            // Determine the type based on ID pattern (could be enhanced with actual STG data)
            SignalGroup.SignalGroupType type = determineSignalGroupType(physicalSignalGroupId);
            String name = "SG" + physicalSignalGroupId;
            
            signalGroup = new SignalGroup(physicalSignalGroupId, name, type);
            
            // Add to intersection and to our local map
            intersection.addPhysicalSignalGroup(signalGroup);
            physicalSignalGroups.put(physicalSignalGroupId, signalGroup);
            
            logger.debug("Created physical signal group: {}", signalGroup);
        }
        
        return signalGroup;
    }

    /**
     * Determines signal group type based on ID (this is a placeholder)
     * In a real system, this would come from the STG file
     */
    private SignalGroup.SignalGroupType determineSignalGroupType(int physicalSignalGroupId) {
        // Simple placeholder logic - this would be replaced with actual type from STG
        if (physicalSignalGroupId >= 7 && physicalSignalGroupId <= 10) {
            // IDs 7-10 are pedestrian crossings in our sample data
            return SignalGroup.SignalGroupType.FG;
        } else if (physicalSignalGroupId == 6) {
            // ID 6 is for cyclists in our sample
            return SignalGroup.SignalGroupType.RD;
        } else {
            // Default to vehicle traffic
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
     * Extract stopline position for an ingress lane
     */
    private void extractStopLinePosition(Element laneElement, Lane lane) {
        if (!lane.isIngress()) {
            return; // Only process ingress lanes
        }
        
        NodeList nodeXYNodes = laneElement.getElementsByTagName("DSRC:NodeXY");
        for (int j = 0; j < nodeXYNodes.getLength(); j++) {
            Element nodeXYElement = (Element) nodeXYNodes.item(j);
            
            // Check if this node is a stop line
            Element attributesElement = (Element) nodeXYElement.getElementsByTagName("DSRC:attributes").item(0);
            if (attributesElement != null) {
                NodeList localNodeList = attributesElement.getElementsByTagName("DSRC:localNode");
                if (localNodeList.getLength() > 0) {
                    Element localNodeElement = (Element) localNodeList.item(0);
                    if (localNodeElement.getElementsByTagName("DSRC:stopLine").getLength() > 0) {
                        // Found a stopline node, extract coordinates
                        Element deltaElement = (Element) nodeXYElement.getElementsByTagName("DSRC:delta").item(0);
                        if (deltaElement != null) {
                            NodeList deltaChildren = deltaElement.getChildNodes();
                            for (int k = 0; k < deltaChildren.getLength(); k++) {
                                Node childNode = deltaChildren.item(k);
                                if (childNode.getNodeType() == Node.ELEMENT_NODE &&
                                        childNode.getNodeName().startsWith("DSRC:node-XY")) {
                                    Element xyElement = (Element) childNode;
                                    int x = Integer.parseInt(getTagContent(xyElement, "DSRC:x"));
                                    int y = Integer.parseInt(getTagContent(xyElement, "DSRC:y"));
                                    
                                    // Set stopline position on the lane
                                    lane.setStopLinePosition(x, y);
                                    logger.debug("Found stop line for lane {}: ({}, {})", lane.getId(), x, y);
                                    return; // Stop after finding the first stopline
                                }
                            }
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
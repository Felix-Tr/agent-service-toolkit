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
        
        // Calculate directions for lanes
        calculateDirections();
        
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
                // Extract directional use
                String directionalUse = getTagContent(laneAttributesElement, "DSRC:directionalUse");
                
                // Extract shared with flags
                String sharedWith = getTagContent(laneAttributesElement, "DSRC:sharedWith");
                parseSharedWith(lane, sharedWith);
                
                // Extract lane type
                Element laneTypeElement = (Element) laneAttributesElement.getElementsByTagName("DSRC:laneType").item(0);
                if (laneTypeElement != null) {
                    parseLaneType(lane, laneTypeElement);
                }
            }
            
            // Extract node coordinates
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
                            
                            lane.addNode(x, y);
                        }
                    }
                }
            }
            
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
        
        // Check each flag position
        if (sharedWith.length() >= 4 && sharedWith.charAt(sharedWith.length() - 4) == '1') {
            lane.setAllowsIndividualMotorizedVehicles(true);
        }
        
        if (sharedWith.length() >= 8 && sharedWith.charAt(sharedWith.length() - 8) == '1') {
            lane.setAllowsCyclists(true);
        }
        
        if (sharedWith.length() >= 7 && sharedWith.charAt(sharedWith.length() - 7) == '1') {
            lane.setAllowsPedestrians(true);
        }
        
        if (sharedWith.length() >= 5 && sharedWith.charAt(sharedWith.length() - 5) == '1') {
            lane.setAllowsPublicTransport(true);
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
                    connection.setConnectionId(connectionId);
                    
                    // Store in map for later reference
                    String connectionKey = ingressLane.getId() + "-" + egressLane.getId();
                    connectionsMap.put(connectionKey, connection);
                }
                
                // Set signal group
                String signalGroupStr = getTagContent(connectionElement, "DSRC:signalGroup");
                if (signalGroupStr != null && !signalGroupStr.isEmpty()) {
                    int signalGroupId = Integer.parseInt(signalGroupStr);
                    
                    // Create signal group if it doesn't exist yet (will be properly populated later)
                    SignalGroup signalGroup = intersection.getSignalGroup(signalGroupId);
                    if (signalGroup == null) {
                        signalGroup = new SignalGroup(signalGroupId, "SG-" + signalGroupId, SignalGroup.SignalGroupType.FV);
                        intersection.addSignalGroup(signalGroup);
                    }
                    
                    connection.setSignalGroup(signalGroup);
                    signalGroup.addControlledConnection(connection);
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
        
        // Remove any spaces or underscores
        maneuvers = maneuvers.replaceAll("[\\s_]", "");
        
        // Check each flag position
        if (maneuvers.length() >= 1 && maneuvers.charAt(0) == '1') {
            connection.setLeftTurn(true);
        }
        
        if (maneuvers.length() >= 2 && maneuvers.charAt(1) == '1') {
            connection.setRightTurn(true);
        }
        
        if (maneuvers.length() >= 3 && maneuvers.charAt(2) == '1') {
            connection.setStraight(true);
        }
        
        if (maneuvers.length() >= 4 && maneuvers.charAt(3) == '1') {
            connection.setUTurn(true);
        }
    }
    
    /**
     * Parses traffic streams from the MAPEM file
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
                
                // Find the connection
                String connectionKey = refLaneId + "-" + refConnectTo;
                Connection connection = connectionsMap.get(connectionKey);
                
                if (connection != null) {
                    trafficStream.linkToConnection(connection);
                } else {
                    logger.warn("Could not find connection for traffic stream: {} -> {}", refLaneId, refConnectTo);
                }
            } else {
                logger.warn("Could not find lanes for traffic stream: {} -> {}", refLaneId, refConnectTo);
            }
            
            // Parse intersection part
            String intersectionPart = getTagContent(streamElement, "MapExtension:intersectionPart");
            if (intersectionPart != null && !intersectionPart.isEmpty()) {
                trafficStream.setIntersectionPart(Integer.parseInt(intersectionPart));
            }
            
            // Parse signal groups
            Element signalGroupsElement = (Element) streamElement.getElementsByTagName("MapExtension:signalGroups").item(0);
            if (signalGroupsElement != null) {
                // Check for primary groups
                Element primaryElement = (Element) signalGroupsElement.getElementsByTagName("MapExtension:primary").item(0);
                if (primaryElement != null) {
                    String vtId = getTagContent(primaryElement, "MapExtension:vt");
                    if (vtId != null && !vtId.isEmpty()) {
                        trafficStream.setVtId(Integer.parseInt(vtId));
                        trafficStream.setPrimary(true);
                    }
                }
                
                // Check for secondary groups (if no primary found)
                if (trafficStream.getVtId() == 0) {
                    Element secondaryElement = (Element) signalGroupsElement.getElementsByTagName("MapExtension:secondary").item(0);
                    if (secondaryElement != null) {
                        String vtId = getTagContent(secondaryElement, "MapExtension:vt");
                        if (vtId != null && !vtId.isEmpty()) {
                            trafficStream.setVtId(Integer.parseInt(vtId));
                            trafficStream.setPrimary(false);
                        }
                    }
                }
            }
            
            intersection.addTrafficStream(trafficStream);
            logger.debug("Added traffic stream: {}", trafficStream);
        }
    }
    
    /**
     * Calculates directions for all lanes based on their node coordinates
     */
    private void calculateDirections() {
        DirectionCalculator calculator = new DirectionCalculator(intersection);
        calculator.calculateDirections();
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
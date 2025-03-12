## Summary for Generating Example MAP Files with Bicycle Lanes

To generate a complete MAPEM (MAP Extended Message) for the given intersection, we include vehicle, pedestrian, and bicycle lanes along with their mandatory or optional statuses based on the standards. The following approach outlines the process:

<p align="center">
  <img src="https://github.com/Felix-Tr/tum-hackathon-challenge-2025/blob/edcac46eeeaa171651f8294f049ee36e2ca7548c/anlagen/Screenshot_20250125-121307.png" alt="Cross Section" width="30%">
</p>

---

## Steps to Generate the MAPEM

1. Extract Key Intersection Data:

Intersection ID: Unique identifier for the intersection (e.g., 155).

Reference Point: Latitude, longitude, and elevation for the center of the intersection.

Lanes:

- **Vehicle Lanes**: Include ingress and egress lanes for vehicles.

- **Bicycle Lanes**: Marked as bikeLane in the MAPEM profile; optional but should be included when available.

- **Pedestrian Lanes**: Represent crosswalks or sidewalks.

- **Signal Group IDs**: Link traffic signals to specific lanes.



2. Define Lane Attributes:

**Ingress/Egress**: Lanes leading into or out of the intersection.

**Lane Types**: Vehicle, bicycle, or pedestrian.

**Width**: Lane width in centimeters.

**Node List**: Define lane geometry using NodeSetXY.



3. Generate Python Code:

Using pyasn1, implement MAPEM structures and populate fields with extracted data.

Ensure optional fields like bicycle lanes are conditionally included based on input data.



4. Export in a Readable Format:

Serialize the MAPEM message using an ASN.1 encoder.

Optionally, export the generated MAPEM data into JSON or XML for easier handling by an LLM or ITS systems.





---

## Python Implementation

Here is the Python code to generate the MAPEM with bicycle lanes:

```
from pyasn1.type import univ, namedtype, namedval, constraint


# Define ASN.1-like structures for MAPEM

class Position(univ.Sequence):
    componentType = namedtype.NamedTypes(
        namedtype.NamedType('lat', univ.Integer()),     # Latitude in WGS-84
        namedtype.NamedType('long', univ.Integer()),    # Longitude in WGS-84
        namedtype.NamedType('elevation', univ.Integer().subtype(
            subtypeSpec=constraint.ValueRangeConstraint(-4096, 61439)
        ))  # Elevation in decimeters
    )


class Lane(univ.Sequence):
    componentType = namedtype.NamedTypes(
        namedtype.NamedType('laneID', univ.Integer()),        # Unique lane ID
        namedtype.NamedType('laneAttributes', univ.OctetString()),  # Attributes of the lane
        namedtype.NamedType('laneWidth', univ.Integer()),     # Lane width in cm
        namedtype.NamedType('nodes', univ.SequenceOf(componentType=Position()))  # Lane geometry
    )


class IntersectionGeometry(univ.Sequence):
    componentType = namedtype.NamedTypes(
        namedtype.NamedType('id', univ.Integer()),              # Intersection ID
        namedtype.NamedType('revision', univ.Integer()),        # Revision number
        namedtype.NamedType('refPoint', Position()),            # Reference point of the intersection
        namedtype.NamedType('laneWidth', univ.Integer()),       # Lane width in centimeters
        namedtype.NamedType('laneSet', univ.SequenceOf(componentType=Lane()))  # Set of lanes
    )


class MAPEM(univ.Sequence):
    componentType = namedtype.NamedTypes(
        namedtype.NamedType('timeStamp', univ.Integer()),         # Current time in seconds since epoch
        namedtype.NamedType('msgIssueRevision', univ.Integer()),  # Message revision
        namedtype.NamedType('intersections', univ.SequenceOf(componentType=IntersectionGeometry()))  # List of intersections
    )


# Populate MAPEM with example data including bicycle lanes
def create_mapem_with_bike_lanes():
    # Define position
    ref_position = Position()
    ref_position.setComponentByName('lat', 523456789)   # Example latitude
    ref_position.setComponentByName('long', 134567891)  # Example longitude
    ref_position.setComponentByName('elevation', 100)   # Example elevation

    # Define lanes
    vehicle_lane = Lane()
    vehicle_lane.setComponentByName('laneID', 1)
    vehicle_lane.setComponentByName('laneAttributes', b'\x01')  # Vehicle lane
    vehicle_lane.setComponentByName('laneWidth', 350)
    vehicle_lane.setComponentByName('nodes', [ref_position])

    bike_lane = Lane()
    bike_lane.setComponentByName('laneID', 2)
    bike_lane.setComponentByName('laneAttributes', b'\x02')  # Bicycle lane
    bike_lane.setComponentByName('laneWidth', 150)
    bike_lane.setComponentByName('nodes', [ref_position])

    # Intersection geometry
    intersection_geometry = IntersectionGeometry()
    intersection_geometry.setComponentByName('id', 155)
    intersection_geometry.setComponentByName('revision', 1)
    intersection_geometry.setComponentByName('refPoint', ref_position)
    intersection_geometry.setComponentByName('laneWidth', 300)  # Default lane width
    intersection_geometry.setComponentByName('laneSet', [vehicle_lane, bike_lane])

    # Create MAPEM
    mapem = MAPEM()
    mapem.setComponentByName('timeStamp', 1617181920)  # Example timestamp
    mapem.setComponentByName('msgIssueRevision', 1)
    mapem.setComponentByName('intersections', [intersection_geometry])

    return mapem


# Export MAPEM to a JSON-like readable format
def export_mapem_to_json(mapem):
    import json

    # Convert MAPEM to a dictionary
    mapem_dict = {
        "timeStamp": mapem.getComponentByName('timeStamp'),
        "msgIssueRevision": mapem.getComponentByName('msgIssueRevision'),
        "intersections": [{
            "id": intersection.getComponentByName('id'),
            "revision": intersection.getComponentByName('revision'),
            "refPoint": {
                "lat": intersection.getComponentByName('refPoint').getComponentByName('lat'),
                "long": intersection.getComponentByName('refPoint').getComponentByName('long'),
                "elevation": intersection.getComponentByName('refPoint').getComponentByName('elevation')
            },
            "laneSet": [{
                "laneID": lane.getComponentByName('laneID'),
                "laneAttributes": lane.getComponentByName('laneAttributes').asOctets(),
                "laneWidth": lane.getComponentByName('laneWidth'),
            } for lane in intersection.getComponentByName('laneSet')]
        } for intersection in mapem.getComponentByName('intersections')]
    }

    # Serialize to JSON
    return json.dumps(mapem_dict, indent=4)


if __name__ == "__main__":
    mapem = create_mapem_with_bike_lanes()
    mapem_json = export_mapem_to_json(mapem)
    print(mapem_json)

```

---

## Key Features

1. Mandatory vs. Optional Bicycle Lanes:

Bicycle lanes (bikeLane) are optional but included if present in the intersection diagram.



2. Serialization:

The MAPEM is serialized into a JSON-like format for LLM readability, making it easy to verify and integrate.



3. Lane Attributes:

Attributes (laneAttributes) distinguish between vehicle and bicycle lanes.





---

## Final Output Example

The resulting JSON-like output might look like this:
```
{
    "timeStamp": 1617181920,
    "msgIssueRevision": 1,
    "intersections": [
        {
            "id": 155,
            "revision": 1,
            "refPoint": {
                "lat": 523456789,
                "long": 134567891,
                "elevation": 100
            },
            "laneSet": [
                {
                    "laneID": 1,
                    "laneAttributes": "01",
                    "laneWidth": 350
                },
                {
                    "laneID": 2,
                    "laneAttributes": "02",
                    "laneWidth": 150
                }
            ]
        }
    ]
}

```
---

It's just an idea to get a sound data model to query on. 




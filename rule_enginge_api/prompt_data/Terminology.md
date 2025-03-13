## Clarification: Signal Head, Signal group, Signalgeber, Signalgruppe

Here’s a concise summary regarding the `TrafficStreamConfigData` and the `<vt>` elements compared to `DSRC:signalGroup` from the provided MAPEM message:

---

## Clarification of `<vt>` vs. `signalGroup`

### 1. Semantics according to the Specification:

- **`DSRC:signalGroup`:**  
  Represents a logical identifier used in MAPEM/SPATEM messages to indicate a set of lanes or traffic movements (connections) at intersections. It is used at a **logical level**, reflecting combined signals applicable to particular **connections** or traffic movements.

- **`<vt>` (MapExtension:signalGroups primary/secondary)**:
  - The `<vt>` id is linked to physical or infrastructural **signal group IDs**, as known in local German traffic engineering systems.
  - The primary/secondary distinction indicates prioritization or an alternate grouping logic (e.g., supplementary signaling or pedestrian signaling attached to a vehicle movement).

In short:
- `signalGroup` in DSRC/MAPEM/SPATEM is logical and connection-focused (movement-centric).
- `<vt>` id in `TrafficStreamConfigData` explicitly relates to a German local **physical/infrastructure signal group** and can map to one or multiple DSRC:signalGroups.

---

### 2. Analysis of the Provided Example:

- The DSRC `signalGroup` ids range higher (up to 22 in the provided XML), and the `<vt>` ids range up to 10 only.
- This discrepancy arises naturally because multiple logical signal groups (C-ITS standard) can map to a single German physical signal group (as represented by `<vt>`).
- The TrafficStreams `<vt>` defines a stable identifier for physically installed traffic-light groups at intersections (`Signalgruppe` in German traffic engineering). In contrast, the DSRC-defined `signalGroup` identifiers express each separate logical connection from a specific lane to another lane or road segment.

Thus, one physical `<vt>` (German signal group) can correlate with **multiple logical DSRC `signalGroups`**, which handle different connections controlled by the same physical signals.

Example from your XML:
- Physical Signal Group (German terminology): `<vt>1` can logically control multiple different connections (`DSRC:signalGroup` 1, 3, etc.).

---

### 3. Legal and Practical Background in Germany:

In Germany, legal implications of traffic signaling require clarity regarding permitted movements. The SPATEM/MAPEM framework transmits:
- A **logical** signaling (`signalGroup`) describing **permitted movements** (left, right, straight, pedestrian) clearly defined in MAPEM.
- Legal relevance: vehicles must obey signals according to StVO (Straßenverkehrsordnung). The transmitted `signalGroup` states (event states such as "stop-And-Remain", "protected-Movement-Allowed") convey the current legal obligations applicable to vehicles for the defined connection.
- The physical grouping `<vt>` (German signal group) represents how the roadside signals are controlled physically at the intersection controller. Each `signalGroup` in the SPATEM is tied clearly back to MAPEM-defined connections, which link it to the correct physical German signal group (`<vt>`).

For instance, at an intersection:
- Physical German signal group (`vt=1`) = Left-turn signals on ingress lane 1.
- C-ITS may define multiple logical signalGroups for different movements (e.g., left-turn into lane A or left-turn into lane B), each receiving a separate DSRC signalGroup ID in SPATEM.
- The logical DSRC `signalGroup` would be responsible for conveying the current state (e.g., permissive, protected, or stop) of these specific movements, thus linking them back to the single German physical group id (`vt=1`).

---

### 4. Practical Recommendation (to Avoid Confusion):

- When documenting and mapping intersections:
  - Clearly distinguish between physical German IDs (`<vt>`) and logical DSRC `signalGroups`.
  - Document clearly which DSRC signal groups (logical) map onto each German physical signal group (`vt`).
  - Note explicitly that one German physical signal group can control multiple logical DSRC signalGroups (connections).

---

## Summary (Brief):
- `<vt>`: corresponds to the **physical German signal groups** (infrastructure-side).
- `DSRC:signalGroup`: a logical grouping per defined traffic movements, controlled by one or more physical `<vt>` signal groups.

This distinction is key for a proper understanding of MAPEM/SPATEM messages, the precise mapping to real-world signal heads (Signalgeber), and ultimately, compliance with traffic rules according to German law (StVO).
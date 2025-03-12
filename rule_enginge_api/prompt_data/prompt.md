# Goal

I want a drools rule engine applied to a specific data object for cross sections. This object is a MAPEM file written in an xml combined with another configuration file with .stg ending where I want to apply legal proofing rules / tasks.

# Return Format

I want you to give me the code for the following steps:

Write out a java module implementing

generate a xml Object parser

Domain Object initializer extracting all the relevant information for the drools rule engine to apply  

Drools Rule Language (DRL) implementation for the green_cyclist_arrow_exclusion rule check if any of the below described rules apply

output the list of connections checked with their respective direction N, NO, O, SO, S, SW, W, NW, this should be inferred from the ingress / engress axis present in the MAPEM file

Write test cases for the given example xml and stg

## Context Dump

The specification for the file format is given in the pdf. What I want, is to create a domain object from it. The object should be directly from the xml file at first for ease of parsing. Then I want the domain object to carry all the relevant information to apply a rule engine on the following rule / logic:

A cross section defined by a mapem has a list of ingress and egress lanes from a direction and ingress lanes can connect to egress lanes. There is also a generic lane, which can receive and start a traffic stream. Traffic streams, meaning those connections are connected to a signal group. The MAPEM has a connection to an incrementing id with a <vt> tag, this is mapped from another file, the stg file to the signal group.

The Signal Group is saved in the mentioned .stg file. 

Here we need to extract the mapping from the id to the signal group name between:

...

#SIGNALGRUPPENDATEN

...

 1,      'FV01', 'FV', 1, '101101',

...

#ZWISCHENZEITENMATRIX 

...

In this example I need the mapping id 1 (<vt> tag in xml) to FV01 (signal group) FV (signal group type).  It is important to get this mapping as the signal group type is a legal entity with respective rules that apply. 

```

#LSA-IDENTIFIZIERUNG

644::

*GRUNDVERSORGUNG

*GERAETETECHNIK (fix)

#SIGNALGRUPPENDATEN

10::

 1,      'FV01', 'FV', 1, '101101',

            '*', ' ', 3, 3, 1, 'ro', 2, 'ge', 3, 'gr', '', '11|31', 2, 99, 99, 3,

              g, 1, 50, g1, -32020, '-', 1, 1, 'e0-10', 

              f, 1, 100, f1, -32020, '=', 1, 3, 'e0-10', 

              a, 1, 10, a1, -32020, ' ', 0, 

           u, 2, u1, f1, g1, 1, 30, '*', 1, 2, 'e0-10', u2, g1, f1, 1, 10, '+', 2, 1, 'e0-10', 2, 'e0-10', 

           z, 2, g1, f1::

 2,      'FV02', 'FV', 1, '101101',

            '-', ' ', 3, 3, 1, 'ro', 2, 'ge', 3, 'gr', '', '11|31', 2, 99, 99, 3,

              g, 1, 50, g1, -32020, '-', 1, 1, 'e0-10', 

              f, 1, 80, f1, -32020, '=', 1, 3, 'e0-10', 

              a, 1, 10, a1, -32020, 'X', 1, 2, 'e0-5a5-10', 

           u, 3, u1, f1, g1, 1, 30, '*', 1, 2, 'e0-10', u2, g1, f1, 1, 10, '+', 2, 1, 'e0-10', 2, 'e0-10', u3, a1, g1, 1, 30, 'X', 1, 2, 'e0-10', 

           z, 2, g1, f1::

 3,      'FV03', 'FV', 1, '101101',

            '*', ' ', 3, 3, 1, 'ro', 2, 'ge', 3, 'gr', '', '11|31', 2, 99, 99, 3,

              g, 1, 50, g1, -32020, '-', 1, 1, 'e0-10', 

              f, 1, 100, f1, -32020, '=', 1, 3, 'e0-10', 

              a, 1, 10, a1, -32020, ' ', 0, 

           u, 2, u1, f1, g1, 1, 30, '*', 1, 2, 'e0-10', u2, g1, f1, 1, 10, '+', 2, 1, 'e0-10', 2, 'e0-10', 

           z, 2, g1, f1::

 4,      'FV04', 'FV', 1, '101101',

            '-', ' ', 3, 3, 1, 'ro', 2, 'ge', 3, 'gr', '', '11|31', 2, 99, 99, 3,

              g, 1, 50, g1, -32020, '-', 1, 1, 'e0-10', 

              f, 1, 80, f1, -32020, '=', 1, 3, 'e0-10', 

              a, 1, 10, a1, -32020, 'X', 1, 2, 'e0-5a5-10', 

           u, 3, u1, f1, g1, 1, 30, '*', 1, 2, 'e0-10', u2, g1, f1, 1, 10, '+', 2, 1, 'e0-10', 2, 'e0-10', u3, a1, g1, 1, 30, 'X', 1, 2, 'e0-10', 

           z, 2, g1, f1::

 5,      'DN05', 'DN', 1, '000100',

            '-', ' ', 1, 1, 1, 'gr', '', '', 2, 99, 99, 3,

              g, 1, 50, g1, -32020, ' ', 0, 

              f, 1, 30, f1, -32020, '=', 1, 1, 'e0-10', 

              a, 1, 10, a1, -32020, ' ', 0, 

           u, 0, 

           z, 2, g1, f1::

 6,      'RD21', 'RD', 1, '101101',

            '-', ' ', 1, 3, 1, 'ro', 2, 'ge', 3, 'gr', '', '11', 2, 99, 99, 3,

              g, 1, 50, g1, -32020, '-', 1, 1, 'e0-10', 

              f, 1, 50, f1, -32020, '=', 1, 3, 'e0-10', 

              a, 1, 10, a1, -32020, ' ', 0, 

           u, 3, u1, f1, g1, 1, 20, '*', 1, 2, 'e0-10', u2, g1, f1, 1, 10, '+', 2, 1, 'e0-10', 2, 'e0-10', u3, a1, g1, 1, 20, 'X', 1, 2, 'e0-10', 

           z, 2, g1, f1::

 7,      'FG51', 'FG', 1, '100100',

            '-', ' ', 2, 2, 1, 'ro', 2, 'gr', '', '11|21', 2, 99, 99, 3,

              g, 1, 50, g1, -32020, '-', 1, 1, 'e0-10', 

              f, 1, 50, f1, -32020, '=', 1, 2, 'e0-10', 

              a, 1, 10, a1, -32020, ' ', 0, 

           u, 0, 

           z, 2, g1, f1::

 8,      'FG52', 'FG', 1, '100100',

            '*', ' ', 2, 2, 1, 'ro', 2, 'gr', '', '', 2, 99, 99, 3,

              g, 1, 50, g1, -32020, '-', 1, 1, 'e0-10', 

              f, 1, 70, f1, -32020, '=', 1, 2, 'e0-10', 

              a, 1, 10, a1, -32020, ' ', 0, 

           u, 0, 

           z, 2, g1, f1::

 9,      'FG53', 'FG', 1, '100100',

            '-', ' ', 2, 2, 1, 'ro', 2, 'gr', '', '', 2, 99, 99, 3,

              g, 1, 50, g1, -32020, '-', 1, 1, 'e0-10', 

              f, 1, 50, f1, -32020, '=', 1, 2, 'e0-10', 

              a, 1, 10, a1, -32020, ' ', 0, 

           u, 0, 

           z, 2, g1, f1::

10,      'FG54', 'FG', 1, '100100',

            '*', ' ', 2, 2, 1, 'ro', 2, 'gr', '', '', 2, 99, 99, 3,

              g, 1, 50, g1, -32020, '-', 1, 1, 'e0-10', 

              f, 1, 70, f1, -32020, '=', 1, 2, 'e0-10', 

              a, 1, 10, a1, -32020, ' ', 0, 

           u, 0, 

           z, 2, g1, f1::

#ZWISCHENZEITENMATRIX 

```

The signal groups are related to signal group types, they are now explained in german for better terminology:

### Signalgeber

Klassische Ampeln haben Signalgeber mit 3-feldigen roter, gelber und grüner Vollscheibe, sie können aber auch 2 oder einfeldig sein. Ein oder mehrere Signalgeber bilden Signalgruppen, diese sind immer aus einer dreistelligen Nummer zusammengesetzt und befinden sich an Masten. Die ersten beiden Ziffern beschreiben (vorgenullt) die Signalgruppe, die letzte Ziffer die Nummer des Signalgebers innerhalb der Signalgruppe. 

Ein Mast ist als kleiner Kreis oder Punkt gekennzeichnet, je nach Typ, Signalgeber werden in unterschiedlicher Größe als kleine Dreiecke und mit einem Pfeil falls sie im Signalbild einen Pfeil beinhalten dargestellt, alternativ haben sie eine oder mehrere Vollscheiben. In einem der beigefügten Bilder sind die Symbole für die Signalgeber zu sehen, der zugehörige Name wird im folgenden der jeweilgen Kategorie zugeordnet. Die Kategorien unterscheiden sich wie folgt:

- Individualverkehr (iV): 

Dieser Typ bezeichnet Signale für den Individualverkehr. Der reservierte Bereich für die Signalgruppennummerierung ist 01 bis 19 (Es sind zur zweistelligen Darstellung führende Nullen zu verwenden). Die dritte Zahl gibt den Signalgeber dieser Gruppe an.

Beispiel: 012 wird interpretiert als "fv012: Individualverkehr, Signalgruppe 1, Signalgeber 2". 

Signalgeber Symbol:

    - Kfz-Signalgeber 3-feldig

    - Kfz-Signalgeber 3-feldig mit Pfeilsymbol

    - Kfz-Signalgeber 2-feldig

    - Kfz-Signalgeber 1-feldig

- Diagonalgrünpfeile für Linksabbieger (dn):

Dieser Typ bezeichnet diagonale Linksabbiegesignale für den Individualverkehr (iV), ein solches Diagonalgrün ist immer bezogen auf die Linksabbiegebeziehung einer iV Fahrverkehrs. Der Diagonalgrünpfeil stellt eine eigene Signalgruppe dar, Nummern werden aber im selben Bereich wie beim iV von 01 bis 19 vergeben. Sie startet immer mit der 9 und füllt dann die freien Zahlen auf abhängig von der Anzahl an Fahrverkehr Signalgruppen (fv).

Beispiel: 051 wird interpretiert als "dn051: Diagonalgrünpfeile für Linksabbieger, Individualverkehr (iV).Signalgeber Symbol:

    - Dia-Grün

- Radverkehrssignalgeber (rd):

Beschreibung: Dieser Typ bezeichnet Signale für den Radverkehr. Die Zahl nach dem Präfix '2' gibt die spezifische Signalgruppe an, und die dritte Zahl gibt den Signalgeber an, der Nummernbereich geht von 21 bis 29. Es ist nicht immer ein Radverkehrssignalgeber vorhanden, der Radverkehr fährt meist mit dem Individualverkehr oder dem Fußverkehr mit.

Beispiel: 231 wird interpretiert als "rd231: Radverkehrssignalgeber, Signalgruppe 3, Signalgeber 1".

Signalgeber Symbol:

    - Radfahrer-Signalgeber 3-feldig

    - Radfahrer-Signalgeber 2-feldig (mit Legende)

- Fußverkehrsignalgeber (fg): 

Dieser Typ bezeichnet Signale für Fußgänger. Der Signalnummerbereich ist von 51 bis 69 und die dritte Zahl gibt den Signalgeber an. Aus Gründen der Barrierefreiheit haben viele Signalgeber für Fußgänger taktile und akustische Signale. Es gibt sogenannte "FG/RD-Kombisignale", diese sind in einem der beigefügten Bilder zu sehen.

Beispiel: 512-TA wird interpretiert als fg512, Fußverkehrsignalgeber, Signalgruppe 1, Signalgeber 2, mit taktil akkustischem Signal.

Signalgeber Symbol:

    - FG-Signalgeber 2-feldig

    - FG-Signalgeber 1-feldig

- Schutzblinker mit Symbolen:

Dieser Signalgeber hat die Funktion an Konfliktbehafteten Stellen über einen Schutzblinker auf einen bestimmten Verkehrsteilnehmer aufmerksam zu machen.

Signalgeber Symbol:

    - Gelbblinker

- ÖPNV:

Für den ÖPNV gibt es eine Reihe weiterer Signalgeber, welche hier erstmal nicht von Bedeutung sind. 

alright. So the signal groups can have specific traffic in german "verkehrsrechtlichen Kontext", meaning it is relevant for proofing semantics. In this regard, I have the following legal semantic entities or properties that the domain object should represent:

- Attribute list of sharedWith (Level 5.5.2): Which traffic type is allowed for that lane. This also defines the respective allowed traffic type but always overwritten by the respective connected egress lane. For example: I have an ingress lane which is sharedWith individualMotorizedVehicleTraffic & cyclistVehicleTraffic, and it is connected to two engress lanes, one with each type, the respective engress lane defines the allowed traffic for this traffic stream.

- This attribute is more difficult, or it is always relative to the ingress lane, where I want to pick the right connection for the ingress lane which is allowed for cyclists.

- I want the attribute or information for a connection for an egress lane, specifically to the one the right connection for the ingress lane connects to.

- I need the information if the two connections have conflict free signaling. if there would be a sign, meaning there is no conditional hostile situation between the right turning cyclist and a lane from the opposite sides left turn which has a free conflict free signal. This is the case for dn signal type and also for left "Vollscheibe", so an extra signal group for the left turn on a cross section, this can only be known when the signal group only has connections to the left turn ingress engress connection and the other directions have a seperate id in the vt tag

So with this in mind, we want to prove rules that define, wheather there is an exclusion criteria for putting up a specific traffic sign (Verkehrszeichen 721) that allows cyclist from an ingress lane to always turn right. The problem with buerocracy is its indivdual prooving paradigm, which per definition increases complexity. How to target this? With modularization or compartmentalization down to clear & unambiguously proovable rules. This is the design principle for the domain object. Every cross section for a traffic light implementation can be unique in its configuration, but submodules or a subspace exists in which a legal requirement is proovable.

The domain object should contain the previously mentioned information or the information in a way, that i can apply the check on the level of a right turn connection for a cross section which can have several of those, in the classic case 4. For each ingress egress approach pair that has a right turn. Their may be more than one per pair, as a cyclist can have revocableBikeLane (Level 5.5.3.3) where the first binary entry is one:

<DSRC:laneType>

    <DSRC:bikeLane>1000000000000000</DSRC:bikeLane>

</DSRC:laneType>

Then the sharedWith attribute can have cyclistTraffic on the laneType bikeLane and the ingress lane with laneType vehicle and the right turn connection / trafficStream.

### The rules

If for a right connection any of the following criteria applies, the check fails:

- There is an only right conflict free signal for an ingress lane which is sharedWith cyclistTraffic

- If there is a conflict free signal for a left turn from the opposite ingress lane for the engress lane of the right turn connection for cyclists. This left turn can have two different sources, the connection is signaled with a dn signal type or it is signaled with a unique signal group for the left turn.

All mentioned example files are attached. If they are not readable they can be prompted (xml with 1000+ lines). Also the respective legal context is attached.

## Warnings

Explain the code in detail an really provide a full implementation guide. Incorporate error and info logs for debugging. Strongly rely on the MAPEM specification given, if more information or clarification is needed ask for it.
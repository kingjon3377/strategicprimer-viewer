import lovelace.util.common {
    matchingValue
}
"An enumeration of directions of possible travel."
shared class Direction
        of north|northeast|east|southeast|south|southwest|west|northwest|nowhere
        satisfies Comparable<Direction> {
    shared static Direction? parse(String direction) =>
        `Direction`.caseValues.find(matchingValue(direction, Object.string));
    "A representation of the direction for debugging purposes."
    shared actual String string;
    "An index, for getting a consistent sort order for UI purposes."
    shared Integer ordinal;
    "North."
    shared new north { string = "north"; ordinal = 1; }
    "Northeast."
    shared new northeast { string = "northeast"; ordinal = 2; }
    "East."
    shared new east { string = "east"; ordinal = 5; }
    "Southeast."
    shared new southeast { string = "southeast"; ordinal = 8; }
    "South."
    shared new south { string = "south"; ordinal = 7; }
    "Southwest."
    shared new southwest { string = "southwest"; ordinal = 6; }
    "West."
    shared new west { string = "west"; ordinal = 3; }
    "Northwest."
    shared new northwest { string = "northwest"; ordinal = 0; }
    "Stand still."
    shared new nowhere { string = "nowhere"; ordinal = 4; }

    shared actual Comparison compare(Direction other) => ordinal <=> other.ordinal;
}

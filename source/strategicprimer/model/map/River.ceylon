import lovelace.util.common {
    todo
}
"If a tile has a river, it could be in any one of several directions. This class
 enumerates those directions. Tiles should have a *set* of these.

 At present we just cover the four cardinal directions."
todo("Extend to cover other directions?")
shared class River of north|east|south|west|lake satisfies Comparable<River> {
    "Get the river matching the given description."
    shared static River|ParseException parse(String description) =>
            parseRiver(description);
    "A descriptive string representing the direction, suitable for use in XML as well."
    shared String description;
    "A unique-per-case number for consistent sorting."
    Integer ordinal;
    "North."
    shared new north {
        description = "north";
        ordinal = 0;
    }
    "East."
    shared new east {
        description = "east";
        ordinal = 1;
    }
    "South."
    shared new south {
        description = "south";
        ordinal = 2;
    }
    "West."
    shared new west {
        description = "west";
        ordinal = 3;
    }
    "A lake (to be depicted as being in the center of the tile)."
    shared new lake {
        description = "lake";
        ordinal = 4;
    }
    "The description with the first letter capitalized."
    shared actual String string = String {
        *{ description.first?.uppercased, *description.rest}.coalesced };
    shared actual Comparison compare(River other) => ordinal <=> other.ordinal;

}
River|ParseException parseRiver(String description) =>
        `River`.caseValues.find((river) => river.description == description) else
            ParseException("Failed to parse River from '``description``'");

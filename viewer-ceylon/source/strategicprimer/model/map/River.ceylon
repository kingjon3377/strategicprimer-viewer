import lovelace.util.common {
    todo
}
"If a tile has a river, it could be in any one of several directions. This class
 enumerates those directions. Tiles should have a *set* of these.

 At present we just cover the four cardinal directions."
todo("Extend to cover other directions?")
shared class River of north|east|south|west|lake {
	"Get the river matching the given description."
	shared static River|ParseException parse(String description) =>
			parseRiver(description);
	"A descriptive string representing the direction, suitable for use in XML as well."
	shared String description;
	"North."
	shared new north { description = "north"; }
	"East."
	shared new east { description = "east"; }
	"South."
	shared new south { description = "south"; }
	"West."
	shared new west { description = "west"; }
	"A lake (to be depicted as being in the center of the tile)."
	shared new lake { description = "lake"; }
	"The description with the first letter capitalized."
	shared actual String string = String {
		*{ description.first?.uppercased, *description.rest}.coalesced };
}
River|ParseException parseRiver(String description) =>
		`River`.caseValues.find((river) => river.description == description) else
			ParseException("Failed to parse River from '``description``'");
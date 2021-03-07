import strategicprimer.model.common.map {
    TileType,
    Point,
    MapDimensions,
    TileFixture
}
import strategicprimer.model.common.map.fixtures.terrain {
    Forest
}

"An [[EncounterTable]] that gives its result based on the terrain type of the tile in
 question."
class TerrainTable(<String->String>* items) satisfies EncounterTable {
    for (key->item in items) {
        assert (`TileType`.caseValues.map(TileType.xml)
            .chain(["mountain", "boreal_forest", "temperate_forest"]).contains(key));
    }
    Map<String, String> mapping = map(items);

    shared actual String generateEvent(Point point, TileType? terrain,
            Boolean mountainous, {TileFixture*} fixtures, MapDimensions mapDimensions) {
        "Terrain table can only account for visible terrain"
        assert (exists terrain);
        String actual;
        Boolean forested = !fixtures.narrow<Forest>().empty;
        if (mountainous) {
            actual = "mountain";
        } else if (terrain == TileType.plains, forested) {
            actual = "temperate_forest";
        } else if (terrain == TileType.steppe, forested) {
            actual = "boreal_forest";
        } else {
            actual = terrain.xml;
        }
        if (exists retval = mapping[actual]) {
            return retval;
        } else if (exists retval = mapping[terrain.xml]) {
            return retval;
        } else {
            throw AssertionError("Table does not account for terrain type ``terrain``");
        }
    }

    shared actual Set<String> allEvents => set(mapping.items);

    shared actual String string =
            "TerrainTable covering ``mapping.size`` terrain types";
}

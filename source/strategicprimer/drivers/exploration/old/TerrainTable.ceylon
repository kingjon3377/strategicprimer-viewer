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
class TerrainTable(<TileType->String>* items) satisfies EncounterTable {
    Map<TileType, String> mapping = map(items);
    suppressWarnings("deprecation")
    shared actual String generateEvent(Point point, TileType? terrain,
            Boolean mountainous, {TileFixture*} fixtures, MapDimensions mapDimensions) {
        "Terrain table can only account for visible terrain"
        assert (exists terrain);
        TileType actual;
        Boolean forested = !fixtures.narrow<Forest>().empty;
        if (mountainous) {
            actual = TileType.mountain;
        } else if (terrain == TileType.plains, forested) {
            actual = TileType.temperateForest;
        } else if (terrain == TileType.steppe, forested) {
            actual = TileType.borealForest;
        } else {
            actual = terrain;
        }
        if (exists retval = mapping[actual]) {
            return retval;
        } else if (exists retval = mapping[terrain]) {
            return retval;
        } else {
            throw AssertionError("Table does not account for terrain type ``terrain``");
        }
    }

    shared actual Set<String> allEvents => set(mapping.items);

    shared actual String string =
            "TerrainTable covering ``mapping.size`` terrain types";
}

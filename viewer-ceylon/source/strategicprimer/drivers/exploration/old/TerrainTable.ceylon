import java.lang {
    IllegalArgumentException
}
import strategicprimer.model.map {
    TileFixture,
    TileType,
    MapDimensions,
    Point
}
import lovelace.util.common {
    todo
}
import strategicprimer.model.map.fixtures.terrain {
    Forest
}
"An [[EncounterTable]] that gives its result based on the terrain type of the tile in
 question."
class TerrainTable(<TileType->String>* items) satisfies EncounterTable {
    Map<TileType, String> mapping = map { *items };
    suppressWarnings("deprecation")
    todo("Add a Boolean parameter to interface for whether the tile is mountainous")
    shared actual String generateEvent(Point point, TileType terrain,
            {TileFixture*} fixtures, MapDimensions mapDimensions) {
        TileType actual;
        // TODO: Once we can, set it to `mountain` if tile is mountainous
        Boolean forested = !fixtures.narrow<Forest>().empty;
        if (terrain == TileType.plains, forested) {
            actual = TileType.temperateForest;
        } else if (terrain == TileType.steppe, forested) {
            actual = TileType.borealForest;
        } else {
            actual = terrain;
        }
        if (exists retval = mapping.get(actual)) {
            return retval;
        } else if (exists retval = mapping.get(terrain)) {
            return retval;
        } else {
            throw IllegalArgumentException(
                "Table does not account for terrain type ``terrain``");
        }
    }
    shared actual Set<String> allEvents =>
            set { *mapping.items };
    shared actual String string =
            "TerrainTable covering ``mapping.size`` terrain types";
}
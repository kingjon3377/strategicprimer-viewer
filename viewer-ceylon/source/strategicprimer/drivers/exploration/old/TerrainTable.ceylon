import java.lang {
    IllegalArgumentException
}
import strategicprimer.model.map {
    TileFixture,
    TileType,
    MapDimensions,
    Point
}
"An [[EncounterTable]] that gives its result based on the terrain type of the tile in
 question."
class TerrainTable(<TileType->String>* items) satisfies EncounterTable {
    Map<TileType, String> mapping = map { *items };
    shared actual String generateEvent(Point point, TileType terrain,
            {TileFixture*} fixtures, MapDimensions mapDimensions) {
        if (exists retval = mapping.get(terrain)) {
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
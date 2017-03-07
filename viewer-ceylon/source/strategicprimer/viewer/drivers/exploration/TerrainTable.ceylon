import model.exploration.old {
    EncounterTable
}
import model.map {
    TileType,
    TileFixture,
    Point,
    MapDimensions
}
import java.util.stream {
    Stream
}
import java.lang {
    IllegalArgumentException,
    JString=String
}
import java.util {
    JSet=Set
}
import ceylon.interop.java {
    JavaSet,
    javaString
}
"An [[EncounterTable]] that gives its result based on the terrain type of the tile in
 question."
class TerrainTable(<TileType->String>* items) satisfies EncounterTable {
    Map<TileType, String> mapping = map { *items };
    shared actual String generateEvent(Point point, TileType terrain,
            Stream<TileFixture> fixtures, MapDimensions mapDimensions) {
        if (exists retval = mapping.get(terrain)) {
            return retval;
        } else {
            throw IllegalArgumentException(
                "Table does not account for terrain type ``terrain``");
        }
    }
    shared actual JSet<JString> allEvents() =>
            JavaSet(set { *mapping.items.map(javaString) });
    shared actual String string =
            "TerrainTable covering ``mapping.size`` terrain types";
}
import model.exploration.old {
    EncounterTable
}
import java.lang {
    JString=String
}
import java.util {
    JSet=Set
}
import model.map {
    Point,
    TileType,
    TileFixture,
    MapDimensions
}
import java.util.stream {
    Stream
}
import ceylon.interop.java {
    JavaSet,
    javaString
}
"An [[EncounterTable]] that always returns the same value."
class ConstantTable(String constant) satisfies EncounterTable {
    shared actual String generateEvent(Point point, TileType terrain,
            Stream<TileFixture> fixtures, MapDimensions dimensions) => constant;
    shared actual JSet<JString> allEvents() => JavaSet(set { javaString(constant) });
    shared actual String string => "ConstantTable: ``constant``";
}
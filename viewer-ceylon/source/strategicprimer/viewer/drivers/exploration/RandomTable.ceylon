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
import util {
    SingletonRandom
}
import ceylon.interop.java {
    JavaSet,
    javaString
}
"An [[EncounterTable]] where the event is selected at random."
class RandomTable([Integer, String]* items) satisfies EncounterTable {
    [Integer, String][] table = items.sort(byIncreasing(
                ([Integer, String] tuple) => tuple.first));
    "Get the first item in the table whose numeric value is above the given value."
    String lowestMatch(Integer val) {
        for ([num, string] in table) {
            if (val >= num) {
                return string;
            }
        }
        assert (exists retval = table.last);
        return retval.rest.first;
    }
    shared actual String generateEvent(Point point, TileType terrain,
            Stream<TileFixture> fixtures, MapDimensions dimensions) =>
                lowestMatch(SingletonRandom.random.nextInt(100));
    shared actual JSet<JString> allEvents() =>
            JavaSet(set {
                *table.map((tuple) => javaString(tuple.rest.first))});
    shared actual String string = "RandomTable of ``table.size`` items";
}
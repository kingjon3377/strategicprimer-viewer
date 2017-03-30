import lovelace.util.jvm {
    singletonRandom
}
import strategicprimer.viewer.model.map {
    TileFixture,
    TileType
}
import model.map {
    Point,
    MapDimensions
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
            {TileFixture*} fixtures, MapDimensions dimensions) =>
                lowestMatch(singletonRandom.nextInt(100));
    shared actual Set<String> allEvents =>
            set {*table.map((tuple) => tuple.rest.first)};
    shared actual String string = "RandomTable of ``table.size`` items";
}
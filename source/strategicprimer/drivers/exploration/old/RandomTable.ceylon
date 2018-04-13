import lovelace.util.jvm {
    singletonRandom
}
import strategicprimer.model.map {
    TileFixture,
    TileType,
    MapDimensions,
    Point
}

"An [[EncounterTable]] where the event is selected at random."
class RandomTable([Integer, String]* items) satisfies EncounterTable {
    [Integer, String][] table = items.sort(byIncreasing(
                ([Integer, String] tuple) => tuple.first)); // FIXME: Replace lambda with Tuple.first method reference
    "Get the first item in the table whose numeric value is above the given value."
    String lowestMatch(Integer val) {
        for ([num, string] in table) {
            if (val >= num) {
                return string;
            }
        }
        assert (exists retval = table.last);
        return retval.rest.first; // TODO: Replace with retval.last
    }
    shared actual String generateEvent(Point point, TileType? terrain,
            Boolean mountainous, {TileFixture*} fixtures, MapDimensions dimensions) =>
                lowestMatch(singletonRandom.nextInteger(100));
    shared actual Set<String> allEvents => set {*table.map(([num, item]) => item)}; // FIXME: Avoid spurious spreading // TODO: Replace lambda with Tuple.last, if that will compile
    shared actual String string = "RandomTable of ``table.size`` items";
}

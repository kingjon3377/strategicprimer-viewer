import ceylon.test {
    test,
    assertEquals
}

import java.nio.file {
    JPath=Path
}
import java.util {
    JOptional=Optional
}

import model.map {
    SPMapNG,
    MapDimensionsImpl,
    PlayerCollection,
    PointFactory,
    Point
}
// This file is tests that the movement code gets its most basic functionality, finding
// adjacent tiles, right.

void directionAssert(IExplorationModel model, Direction direction,
        Point source, Point destination, String extraMessage = "") {
    assertEquals(model.getDestination(source, direction), destination,
        "``direction`` of ``source````extraMessage`` is ``destination``");
}
"Test that wrapping to the east works properly."
test
void testEast() {
    IExplorationModel model = ExplorationModel(SPMapNG(MapDimensionsImpl(5, 5, 2),
        PlayerCollection(), 0), JOptional.empty<JPath>());
    void localAssert(Point source, Point destination, String extra = "") =>
            directionAssert(model, Direction.east, source, destination, extra);
    localAssert(PointFactory.point(0, 0), PointFactory.point(0, 1));
    localAssert(PointFactory.point(1, 1), PointFactory.point(1, 2));
    localAssert(PointFactory.point(3, 4), PointFactory.point(3, 0),
        " in a 5x5 map");
    localAssert(PointFactory.point(4, 3), PointFactory.point(4, 4));
}

"Test that wrapping to the north works properly."
void testNorth() {
    IExplorationModel model = ExplorationModel(SPMapNG(MapDimensionsImpl(5, 5, 2),
        PlayerCollection(), 0), JOptional.empty<JPath>());
    void localAssert(Point source, Point destination, String extra = "") =>
            directionAssert(model, Direction.north, source, destination,
               extra);
    localAssert(PointFactory.point(0, 0), PointFactory.point(4, 0), " in a 5x5 map");
    localAssert(PointFactory.point(1, 1), PointFactory.point(0, 1));
    localAssert(PointFactory.point(3, 4), PointFactory.point(2, 4));
    localAssert(PointFactory.point(4, 3), PointFactory.point(3, 3));
}

"Test that wrapping to the south works properly."
void testSouth() {
    IExplorationModel model = ExplorationModel(SPMapNG(MapDimensionsImpl(5, 5, 2),
        PlayerCollection(), 0), JOptional.empty<JPath>());
    void localAssert(Point source, Point destination, String extra = "") =>
            directionAssert(model, Direction.south, source, destination,
                extra);
    localAssert(PointFactory.point(0, 0), PointFactory.point(1, 0));
    localAssert(PointFactory.point(1, 1), PointFactory.point(2, 1));
    localAssert(PointFactory.point(3, 4), PointFactory.point(4, 4));
    localAssert(PointFactory.point(4, 3), PointFactory.point(0, 3),
        " in a 5x5 map");
}

"Test that wrapping to the west works properly."
void testWest() {
    IExplorationModel model = ExplorationModel(SPMapNG(MapDimensionsImpl(5, 5, 2),
        PlayerCollection(), 0), JOptional.empty<JPath>());
    void localAssert(Point source, Point destination, String extra = "") =>
            directionAssert(model, Direction.west, source, destination,
                extra);
    localAssert(PointFactory.point(0, 0), PointFactory.point(0, 4),
        " in a 5x5 map");
    localAssert(PointFactory.point(1, 1), PointFactory.point(1, 0));
    localAssert(PointFactory.point(3, 4), PointFactory.point(3, 3));
    localAssert(PointFactory.point(4, 3), PointFactory.point(4, 2));
}

// TODO: add tests covering other directions
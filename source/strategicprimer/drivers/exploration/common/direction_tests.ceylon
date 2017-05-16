import ceylon.test {
    test,
    assertEquals
}

import strategicprimer.model.map {
    Point,
    MapDimensionsImpl,
    PlayerCollection,
    SPMapNG,
    pointFactory
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
        PlayerCollection(), 0), null);
    void localAssert(Point source, Point destination, String extra = "") =>
            directionAssert(model, Direction.east, source, destination, extra);
    localAssert(pointFactory(0, 0), pointFactory(0, 1));
    localAssert(pointFactory(1, 1), pointFactory(1, 2));
    localAssert(pointFactory(3, 4), pointFactory(3, 0),
        " in a 5x5 map");
    localAssert(pointFactory(4, 3), pointFactory(4, 4));
}

"Test that wrapping to the north works properly."
test
void testNorth() {
    IExplorationModel model = ExplorationModel(SPMapNG(MapDimensionsImpl(5, 5, 2),
        PlayerCollection(), 0), null);
    void localAssert(Point source, Point destination, String extra = "") =>
            directionAssert(model, Direction.north, source, destination,
               extra);
    localAssert(pointFactory(0, 0), pointFactory(4, 0), " in a 5x5 map");
    localAssert(pointFactory(1, 1), pointFactory(0, 1));
    localAssert(pointFactory(3, 4), pointFactory(2, 4));
    localAssert(pointFactory(4, 3), pointFactory(3, 3));
}

"Test that wrapping to the south works properly."
test
void testSouth() {
    IExplorationModel model = ExplorationModel(SPMapNG(MapDimensionsImpl(5, 5, 2),
        PlayerCollection(), 0), null);
    void localAssert(Point source, Point destination, String extra = "") =>
            directionAssert(model, Direction.south, source, destination,
                extra);
    localAssert(pointFactory(0, 0), pointFactory(1, 0));
    localAssert(pointFactory(1, 1), pointFactory(2, 1));
    localAssert(pointFactory(3, 4), pointFactory(4, 4));
    localAssert(pointFactory(4, 3), pointFactory(0, 3),
        " in a 5x5 map");
}

"Test that wrapping to the west works properly."
test
void testWest() {
    IExplorationModel model = ExplorationModel(SPMapNG(MapDimensionsImpl(5, 5, 2),
        PlayerCollection(), 0), null);
    void localAssert(Point source, Point destination, String extra = "") =>
            directionAssert(model, Direction.west, source, destination,
                extra);
    localAssert(pointFactory(0, 0), pointFactory(0, 4),
        " in a 5x5 map");
    localAssert(pointFactory(1, 1), pointFactory(1, 0));
    localAssert(pointFactory(3, 4), pointFactory(3, 3));
    localAssert(pointFactory(4, 3), pointFactory(4, 2));
}

"Test that wrapping to the northeast works properly."
test
void testNortheast() {
    IExplorationModel model = ExplorationModel(SPMapNG(MapDimensionsImpl(5, 5, 2),
        PlayerCollection(), 0), null);
    void localAssert(Point source, Point destination, String extra = "") =>
            directionAssert(model, Direction.northeast, source, destination,
                extra);
    localAssert(pointFactory(0, 0), pointFactory(4, 1),
        " in a 5x5 map");
    localAssert(pointFactory(1, 1), pointFactory(0, 2));
    localAssert(pointFactory(3, 4), pointFactory(2, 0));
    localAssert(pointFactory(4, 3), pointFactory(3, 4));
}

"Test that wrapping to the northwest works properly."
test
void testNorthwest() {
    IExplorationModel model = ExplorationModel(SPMapNG(MapDimensionsImpl(5, 5, 2),
        PlayerCollection(), 0), null);
    void localAssert(Point source, Point destination, String extra = "") =>
            directionAssert(model, Direction.northwest, source, destination,
                extra);
    localAssert(pointFactory(0, 0), pointFactory(4, 4),
        " in a 5x5 map");
    localAssert(pointFactory(1, 1), pointFactory(0, 0));
    localAssert(pointFactory(3, 4), pointFactory(2, 3));
    localAssert(pointFactory(4, 3), pointFactory(3, 2));
}

"Test that wrapping to the southeast works properly."
test
void testSoutheast() {
    IExplorationModel model = ExplorationModel(SPMapNG(MapDimensionsImpl(5, 5, 2),
        PlayerCollection(), 0), null);
    void localAssert(Point source, Point destination, String extra = "") =>
            directionAssert(model, Direction.southeast, source, destination,
                extra);
    localAssert(pointFactory(0, 0), pointFactory(1, 1));
    localAssert(pointFactory(1, 1), pointFactory(2, 2));
    localAssert(pointFactory(3, 4), pointFactory(4, 0), "in a 5x5 map");
    localAssert(pointFactory(4, 3), pointFactory(0, 4),
        " in a 5x5 map");
}

"Test that wrapping to the south works properly."
test
void testSouthwest() {
    IExplorationModel model = ExplorationModel(SPMapNG(MapDimensionsImpl(5, 5, 2),
        PlayerCollection(), 0), null);
    void localAssert(Point source, Point destination, String extra = "") =>
            directionAssert(model, Direction.southwest, source, destination,
                extra);
    localAssert(pointFactory(0, 0), pointFactory(1, 4), "in a 5x5 map");
    localAssert(pointFactory(1, 1), pointFactory(2, 0));
    localAssert(pointFactory(3, 4), pointFactory(4, 3));
    localAssert(pointFactory(4, 3), pointFactory(0, 2),
        " in a 5x5 map");
}

"Test that wrapping to the south works properly."
test
void testNowhere() {
    IExplorationModel model = ExplorationModel(SPMapNG(MapDimensionsImpl(5, 5, 2),
        PlayerCollection(), 0), null);
    void localAssert(Point source, Point destination, String extra = "") =>
            directionAssert(model, Direction.nowhere, source, destination,
                extra);
    localAssert(pointFactory(0, 0), pointFactory(0, 0));
    localAssert(pointFactory(1, 1), pointFactory(1, 1));
    localAssert(pointFactory(3, 4), pointFactory(3, 4));
    localAssert(pointFactory(4, 3), pointFactory(4, 3));
}

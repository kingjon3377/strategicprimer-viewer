import ceylon.test {
    test,
    assertEquals
}

import strategicprimer.model.common.map {
    Point,
    PlayerCollection,
    MapDimensionsImpl,
    SPMapNG
}

"Tests that the movement code gets its most basic functionality, namely finding adjacent
 tiles, right."
object directionTests {
    "A custom assertion for these tests."
    void directionAssert(IExplorationModel model, Direction direction, // TODO: Have two parameter lists to avoid lambdas in test methods.
            Point source, Point destination, String extraMessage = "") =>
        assertEquals(model.getDestination(source, direction), destination,
            "``direction`` of ``source````extraMessage`` is ``destination``");

    "Test that wrapping to the east works properly."
    test
    shared void testEast() {
        IExplorationModel model = ExplorationModel(SPMapNG(MapDimensionsImpl(5, 5, 2),
            PlayerCollection(), 0), null);
        void localAssert(Point source, Point destination, String extra = "") =>
                directionAssert(model, Direction.east, source, destination, extra);
        localAssert(Point(0, 0), Point(0, 1));
        localAssert(Point(1, 1), Point(1, 2));
        localAssert(Point(3, 4), Point(3, 0),
            " in a 5x5 map");
        localAssert(Point(4, 3), Point(4, 4));
    }

    "Test that wrapping to the north works properly."
    test
    shared void testNorth() {
        IExplorationModel model = ExplorationModel(SPMapNG(MapDimensionsImpl(5, 5, 2),
            PlayerCollection(), 0), null);
        void localAssert(Point source, Point destination, String extra = "") =>
                directionAssert(model, Direction.north, source, destination,
                   extra);
        localAssert(Point(0, 0), Point(4, 0), " in a 5x5 map");
        localAssert(Point(1, 1), Point(0, 1));
        localAssert(Point(3, 4), Point(2, 4));
        localAssert(Point(4, 3), Point(3, 3));
    }

    "Test that wrapping to the south works properly."
    test
    shared void testSouth() {
        IExplorationModel model = ExplorationModel(SPMapNG(MapDimensionsImpl(5, 5, 2),
            PlayerCollection(), 0), null);
        void localAssert(Point source, Point destination, String extra = "") =>
                directionAssert(model, Direction.south, source, destination,
                    extra);
        localAssert(Point(0, 0), Point(1, 0));
        localAssert(Point(1, 1), Point(2, 1));
        localAssert(Point(3, 4), Point(4, 4));
        localAssert(Point(4, 3), Point(0, 3),
            " in a 5x5 map");
    }

    "Test that wrapping to the west works properly."
    test
    shared void testWest() {
        IExplorationModel model = ExplorationModel(SPMapNG(MapDimensionsImpl(5, 5, 2),
            PlayerCollection(), 0), null);
        void localAssert(Point source, Point destination, String extra = "") =>
                directionAssert(model, Direction.west, source, destination,
                    extra);
        localAssert(Point(0, 0), Point(0, 4),
            " in a 5x5 map");
        localAssert(Point(1, 1), Point(1, 0));
        localAssert(Point(3, 4), Point(3, 3));
        localAssert(Point(4, 3), Point(4, 2));
    }

    "Test that wrapping to the northeast works properly."
    test
    shared void testNortheast() {
        IExplorationModel model = ExplorationModel(SPMapNG(MapDimensionsImpl(5, 5, 2),
            PlayerCollection(), 0), null);
        void localAssert(Point source, Point destination, String extra = "") =>
                directionAssert(model, Direction.northeast, source, destination,
                    extra);
        localAssert(Point(0, 0), Point(4, 1),
            " in a 5x5 map");
        localAssert(Point(1, 1), Point(0, 2));
        localAssert(Point(3, 4), Point(2, 0));
        localAssert(Point(4, 3), Point(3, 4));
    }

    "Test that wrapping to the northwest works properly."
    test
    shared void testNorthwest() {
        IExplorationModel model = ExplorationModel(SPMapNG(MapDimensionsImpl(5, 5, 2),
            PlayerCollection(), 0), null);
        void localAssert(Point source, Point destination, String extra = "") =>
                directionAssert(model, Direction.northwest, source, destination,
                    extra);
        localAssert(Point(0, 0), Point(4, 4),
            " in a 5x5 map");
        localAssert(Point(1, 1), Point(0, 0));
        localAssert(Point(3, 4), Point(2, 3));
        localAssert(Point(4, 3), Point(3, 2));
    }

    "Test that wrapping to the southeast works properly."
    test
    shared void testSoutheast() {
        IExplorationModel model = ExplorationModel(SPMapNG(MapDimensionsImpl(5, 5, 2),
            PlayerCollection(), 0), null);
        void localAssert(Point source, Point destination, String extra = "") =>
                directionAssert(model, Direction.southeast, source, destination,
                    extra);
        localAssert(Point(0, 0), Point(1, 1));
        localAssert(Point(1, 1), Point(2, 2));
        localAssert(Point(3, 4), Point(4, 0), "in a 5x5 map");
        localAssert(Point(4, 3), Point(0, 4),
            " in a 5x5 map");
    }

    "Test that wrapping to the south works properly."
    test
    shared void testSouthwest() {
        IExplorationModel model = ExplorationModel(SPMapNG(MapDimensionsImpl(5, 5, 2),
            PlayerCollection(), 0), null);
        void localAssert(Point source, Point destination, String extra = "") =>
                directionAssert(model, Direction.southwest, source, destination,
                    extra);
        localAssert(Point(0, 0), Point(1, 4), "in a 5x5 map");
        localAssert(Point(1, 1), Point(2, 0));
        localAssert(Point(3, 4), Point(4, 3));
        localAssert(Point(4, 3), Point(0, 2),
            " in a 5x5 map");
    }

    "Test that wrapping to the south works properly."
    test
    shared void testNowhere() {
        IExplorationModel model = ExplorationModel(SPMapNG(MapDimensionsImpl(5, 5, 2),
            PlayerCollection(), 0), null);
        void localAssert(Point source, Point destination, String extra = "") =>
                directionAssert(model, Direction.nowhere, source, destination,
                    extra);
        localAssert(Point(0, 0), Point(0, 0));
        localAssert(Point(1, 1), Point(1, 1));
        localAssert(Point(3, 4), Point(3, 4));
        localAssert(Point(4, 3), Point(4, 3));
    }
}

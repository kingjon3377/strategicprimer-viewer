import ceylon.interop.java {
    CeylonIterable
}
import ceylon.test {
    assertEquals,
    test
}

import model.map {
    PointFactory,
    MapDimensionsImpl,
    Point
}
import model.viewer {
    PointIterator
}

import util {
    IteratorWrapper
}
// the PointIterator class will go here
"Test iteration forwards, horizontally, from the beginning of the map."
test
void testFromBeginning() {
    Point[] expected = [PointFactory.point(0, 0), PointFactory.point(0, 1),
        PointFactory.point(0, 2), PointFactory.point(1, 0), PointFactory.point(1, 1),
        PointFactory.point(1, 2), PointFactory.point(2, 0), PointFactory.point(2, 1),
        PointFactory.point(2, 2)];
    Point[] actual = [*CeylonIterable(IteratorWrapper(PointIterator(MapDimensionsImpl(3, 3, 1), null,
        true, true)))];
    assertEquals(actual, expected, "Iterator produced points in expected order");
}

"Test iteration forwards, horizontally, from a selected point."
test
void testFromSelection() {
    Point[] expected = [PointFactory.point(1, 2), PointFactory.point(2, 0),
        PointFactory.point(2, 1), PointFactory.point(2, 2), PointFactory.point(0, 0),
        PointFactory.point(0, 1), PointFactory.point(0, 2), PointFactory.point(1, 0),
        PointFactory.point(1, 1)];
    Point[] actual = [*CeylonIterable(IteratorWrapper(PointIterator(MapDimensionsImpl(3, 3, 1),
        PointFactory.point(1, 1), true, true)))];
    assertEquals(actual, expected, "Iterator produced points in expected order");
}

"""Test searching forwards, vertically, from the "selection" the viewer starts with."""
test
void testInitialSelection() {
    Point[] expected = [PointFactory.point(0, 0), PointFactory.point(1, 0),
        PointFactory.point(2, 0), PointFactory.point(0, 1), PointFactory.point(1, 1),
        PointFactory.point(2, 1), PointFactory.point(0, 2), PointFactory.point(1, 2),
        PointFactory.point(2, 2)];
    Point[] actual = [*CeylonIterable(IteratorWrapper(PointIterator(MapDimensionsImpl(3, 3, 1),
        PointFactory.invalidPoint, true, false)))];
    assertEquals(actual, expected, "Iterator produced points in expected order");
}

"Test searching backwards, horizontally."
test
void testReverse() {
    Point[] expected = [PointFactory.point(2, 2), PointFactory.point(2, 1),
        PointFactory.point(2, 0), PointFactory.point(1, 2),
        PointFactory.point(1, 1), PointFactory.point(1, 0),
        PointFactory.point(0, 2), PointFactory.point(0, 1),
        PointFactory.point(0, 0)];
    Point[] actual = [*CeylonIterable(IteratorWrapper(PointIterator(MapDimensionsImpl(3, 3, 1), null,
        false, true)))];
    assertEquals(actual, expected, "Iterator produced points in expected order");
}

"Test searching vertically, backwards."
test
void testVerticalReverse() {
    Point[] expected = [PointFactory.point(2, 2), PointFactory.point(1, 2),
        PointFactory.point(0, 2), PointFactory.point(2, 1), PointFactory.point(1, 1),
        PointFactory.point(0, 1), PointFactory.point(2, 0), PointFactory.point(1, 0),
        PointFactory.point(0, 0)];
    Point[] actual = [*CeylonIterable(IteratorWrapper(PointIterator(MapDimensionsImpl(3, 3, 1), null,
        false, false)))];
    assertEquals(actual, expected, "Iterator produced points in expected order");
}
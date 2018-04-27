import ceylon.test {
    assertEquals,
    test
}

import strategicprimer.model.map {
    MapDimensions,
    pointFactory,
    invalidPoint,
    Point
}
"A view of locations on the map in order, starting at a given point."
shared class PointIterator(dimensions, forwards, horizontal,
        selection = null) satisfies {Point*} {
    "The dimensions of the map we're a view of."
    MapDimensions dimensions;
    "Whether we should search forwards (if true) or backwards (if false)."
    Boolean forwards;
    "Whether we should search horizontally (if true) or vertically (if false)"
    Boolean horizontal;
    "The selected point; we start from (just before) (0, 0) if omitted."
    Point? selection;
    shared actual Iterator<Point> iterator() {
        object retval satisfies Iterator<Point> { // TODO: Convert to static inner class
            variable Integer remainingCount = dimensions.rows * dimensions.columns;
            "The maximum row in the map."
            Integer maxRow = dimensions.rows - 1;
            "The maximum column in the map."
            Integer maxColumn = dimensions.columns - 1;
            "The row where we started."
            Integer startRow;
            "The column where we started."
            Integer startColumn;
            """Wrap negative values to the provided "wrap" value, but return positive
               values."""
            Integer wrap(Integer item, Integer wrap) => if (item<0) then wrap else item;
            if (exists selection) {
                startRow = wrap(selection.row, maxRow);
                startColumn = wrap(selection.column, maxColumn);
            } else if (forwards) {
                startRow = maxRow;
                startColumn = maxColumn;
            } else {
                startRow = 0;
                startColumn = 0;
            }
            "The current row."
            variable Integer row = startRow;
            "The current column."
            variable Integer column = startColumn;
            "Whether we've started iterating."
            variable Boolean started = false;
            "A diagnostic String."
            shared actual String string =>
                    "PointIterator: Started at (``startRow``, ``startColumn
                    ``), currently at (``row``, ``column``), searching ``(horizontal) then
                    "horizontal" else "vertical"``ly ``(forwards) then "for" else
                    "back"``wards and no farther than (``maxRow``, ``maxColumn``)";

            shared actual Point|Finished next() {
                if (started, row == startRow, column == startColumn) {
                    return finished;
                } else {
                    remainingCount--;
                    started = true;
                    if (horizontal) {
                        if (forwards) {
                            if (column == maxColumn) {
                                if (row == maxRow) {
                                    row = 0;
                                } else {
                                    row++;
                                }
                                column = 0;
                            } else {
                                column++;
                            }
                        } else {
                            if (column == 0) {
                                if (row == 0) {
                                    row = maxRow;
                                } else {
                                    row--;
                                }
                                column = maxColumn;
                            } else {
                                column--;
                            }
                        }
                    } else {
                        if (forwards) {
                            if (row == maxRow) {
                                if (column == maxColumn) {
                                    column = 0;
                                } else {
                                    column++;
                                }
                                row = 0;
                            } else {
                                row++;
                            }
                        } else {
                            if (row == 0) {
                                if (column == 0) {
                                    column = maxColumn;
                                } else {
                                    column--;
                                }
                                row = maxRow;
                            } else {
                                row--;
                            }
                        }
                    }
                    return pointFactory(row, column);
                }
            }
        }
        return retval;
    }
}
object pointIterationTests {
	"Test iteration forwards, horizontally, from the beginning of the map."
	test
	shared void testFromBeginning() {
	    Point[] expected = [pointFactory(0, 0), pointFactory(0, 1),
	        pointFactory(0, 2), pointFactory(1, 0), pointFactory(1, 1),
	        pointFactory(1, 2), pointFactory(2, 0), pointFactory(2, 1),
	        pointFactory(2, 2)];
	    Point[] actual = PointIterator(MapDimensionsImpl(3, 3, 1), true, true).sequence();
	    assertEquals(actual, expected, "Iterator produced points in expected order");
	}

	"Test iteration forwards, horizontally, from a selected point."
	test
	shared void testFromSelection() {
	    Point[] expected = [pointFactory(1, 2), pointFactory(2, 0),
	        pointFactory(2, 1), pointFactory(2, 2), pointFactory(0, 0),
	        pointFactory(0, 1), pointFactory(0, 2), pointFactory(1, 0),
	        pointFactory(1, 1)];
	    Point[] actual = PointIterator(MapDimensionsImpl(3, 3, 1), true, true,
	        pointFactory(1, 1)).sequence(); // Have to have .sequence() to meet declared type
	    assertEquals(actual, expected, "Iterator produced points in expected order");
	}

	"""Test searching forwards, vertically, from the "selection" the viewer starts with."""
	test
	shared void testInitialSelection() {
	    Point[] expected = [pointFactory(0, 0), pointFactory(1, 0),
	        pointFactory(2, 0), pointFactory(0, 1), pointFactory(1, 1),
	        pointFactory(2, 1), pointFactory(0, 2), pointFactory(1, 2),
	        pointFactory(2, 2)];
	    Point[] actual = PointIterator(MapDimensionsImpl(3, 3, 1), true, false,
	        invalidPoint).sequence();
	    assertEquals(actual, expected, "Iterator produced points in expected order");
	}

	"Test searching backwards, horizontally."
	test
	shared void testReverse() {
	    Point[] expected = [pointFactory(2, 2), pointFactory(2, 1),
	        pointFactory(2, 0), pointFactory(1, 2),
	        pointFactory(1, 1), pointFactory(1, 0),
	        pointFactory(0, 2), pointFactory(0, 1),
	        pointFactory(0, 0)];
	    Point[] actual = PointIterator(MapDimensionsImpl(3, 3, 1), false, true).sequence();
	    assertEquals(actual, expected, "Iterator produced points in expected order");
	}

	"Test searching vertically, backwards."
	test
	shared void testVerticalReverse() {
	    Point[] expected = [pointFactory(2, 2), pointFactory(1, 2),
	        pointFactory(0, 2), pointFactory(2, 1), pointFactory(1, 1),
	        pointFactory(0, 1), pointFactory(2, 0), pointFactory(1, 0),
	        pointFactory(0, 0)];
	    Point[] actual = PointIterator(MapDimensionsImpl(3, 3, 1), false, false).sequence();
	    assertEquals(actual, expected, "Iterator produced points in expected order");
	}
}
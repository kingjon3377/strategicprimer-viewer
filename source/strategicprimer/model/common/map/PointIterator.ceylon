import ceylon.test {
    test
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

    class PointIteratorImpl() satisfies Iterator<Point> {
        variable Integer remainingCount = dimensions.rows * dimensions.columns;

        "The maximum row in the map."
        Integer maxRow = dimensions.rows - 1;

        "The maximum column in the map."
        Integer maxColumn = dimensions.columns - 1;

        "The row where we started."
        Integer startRow;

        "The column where we started."
        Integer startColumn;

        "If [[item]] is zero or positive, return it; otherwise, return
         [[wrap]]."
        Integer wrap(Integer item, Integer wrap) =>
                if (item.negative) then wrap else item;

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
                        column++;
                        if (column > maxColumn) {
                            column = 0;
                            row++;
                            if (row > maxRow) {
                                row = 0;
                            }
                        }
                    } else {
                        column--;
                        if (column.negative) {
                            column = maxColumn;
                            row--;
                            if (row.negative) {
                                row = maxRow;
                            }
                        }
                    }
                } else {
                    if (forwards) {
                        row++;
                        if (row > maxRow) {
                            row = 0;
                            column++;
                            if (column > maxColumn) {
                                column = 0;
                            }
                        }
                    } else {
                        row--;
                        if (row.negative) {
                            row = maxRow;
                            column--;
                            if (column.negative) {
                                column = maxColumn;
                            }
                        }
                    }
                }
                return Point(row, column);
            }
        }
    }

    shared actual Iterator<Point> iterator() => PointIteratorImpl();
}

"Tests that [[PointIterator]] works properly for each possible configuration."
// Using => for these one-line methods causes compilation errors.
object pointIterationTests {
    "Test iteration forwards, horizontally, from the beginning of the map."
    test
    shared void testFromBeginning() {
        "Iterator should produce points in the expected order when given no
         starting point and iterating forwards horizontally."
        assert (corresponding([Point(0, 0), Point(0, 1), Point(0, 2), Point(1, 0),
                Point(1, 1), Point(1, 2), Point(2, 0), Point(2, 1), Point(2, 2)],
            PointIterator(MapDimensionsImpl(3, 3, 1), true, true)));
    }

    "Test iteration forwards, horizontally, from a selected point."
    test
    shared void testFromSelection() {
        "Iterator should produce points in the expected order when given a
         starting point and iterating forwards horizontally."
        assert (corresponding([Point(1, 2), Point(2, 0), Point(2, 1), Point(2, 2),
                Point(0, 0), Point(0, 1), Point(0, 2), Point(1, 0), Point(1, 1)],
            PointIterator(MapDimensionsImpl(3, 3, 1), true, true, Point(1, 1))));
    }

    """Test searching forwards, vertically, from the "selection" the viewer starts
       with."""
    test
    shared void testInitialSelection() {
        "Iterator should produce points in the expected order when starting at
         [[Point.invalidPoint]] and iterating forwards vertically."
        assert (corresponding([Point(0, 0), Point(1, 0), Point(2, 0), Point(0, 1),
                Point(1, 1), Point(2, 1), Point(0, 2), Point(1, 2), Point(2, 2)],
            PointIterator(MapDimensionsImpl(3, 3, 1), true, false, Point.invalidPoint)));
    }

    "Test searching backwards, horizontally."
    test
    shared void testReverse() {
        "Iterator should produce points in the expected order when iterating
         backwards horizontally."
        assert (corresponding([Point(2, 2), Point(2, 1), Point(2, 0), Point(1, 2),
                Point(1, 1), Point(1, 0), Point(0, 2), Point(0, 1), Point(0, 0)],
            PointIterator(MapDimensionsImpl(3, 3, 1), false, true)));
    }

    "Test searching vertically, backwards."
    test
    shared void testVerticalReverse() {
        "Iterator should produce points in the expected order when iterating
         backwards vertically."
        assert (corresponding([Point(2, 2), Point(1, 2), Point(0, 2), Point(2, 1),
                Point(1, 1), Point(0, 1), Point(2, 0), Point(1, 0), Point(0, 0)],
            PointIterator(MapDimensionsImpl(3, 3, 1), false, false)));
    }
}

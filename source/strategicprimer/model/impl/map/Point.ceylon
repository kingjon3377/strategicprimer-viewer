import lovelace.util.common {
    todo
}
"A structure encapsulating two coordinates: a row and column in the map."
shared final class Point(row, column) satisfies Comparable<Point> {
    "The first coordinate, the point's row."
    shared Integer row;
    "The second coordinate, the point's column."
    shared Integer column;
    shared actual Boolean equals(Object obj) {
        if (is Point obj) {
            return obj.row == row && obj.column == column;
        } else {
            return false;
        }
    }
    shared actual Integer hash => row.leftLogicalShift(9) + column;
    shared actual String string => "(``row``, ``column``)";
    "Compare to another point, by first row and then column."
    shared actual Comparison compare(Point point) {
        Comparison rowComparison = row <=> point.row;
        if (rowComparison == equal) {
            return column <=> point.column;
        } else {
            return rowComparison;
        }
    }
    """A point is "valid" if neither row nor column is negative."""
    shared Boolean valid => row >= 0 && column >= 0;
}
"""The standard "invalid point.""""
todo("Replace with [[null]]?")
shared Point invalidPoint = Point(-1, -1);

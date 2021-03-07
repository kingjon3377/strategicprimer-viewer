import ceylon.numeric.float {
    sqrt
}

"An encapsulation of a map's dimensions (and its map version as well)."
// This is an interface so we can make a mock object "satisfying" it and guarantee it is
// never referenced by making all of its attributes evaluate [[nothing]].
shared interface MapDimensions satisfies Category<Point> {
    "The number of rows in the map."
    shared formal Integer rows;

    "The number of columns in the map."
    shared formal Integer columns;

    "The map version."
    shared formal Integer version;

    shared actual default Boolean contains(Point point) =>
        point.row in 0:rows && point.column in 0:columns;

    "The distance between two points in a map with these dimensions."
    shared default Float distance(Point first, Point second) {
        Integer rawXDiff = first.row - second.row;
        Integer rawYDiff = first.column - second.column;
        Integer xDiff;
        if (rawXDiff < (rows / 2)) {
            xDiff = rawXDiff;
        } else {
            xDiff = rows - rawXDiff;
        }
        Integer yDiff;
        if (rawYDiff < (columns / 2)) {
            yDiff = rawYDiff;
        } else {
            yDiff = columns - rawYDiff;
        }
        return sqrt((xDiff * xDiff + yDiff * yDiff).float);
    }
}

import ceylon.math.float {
    sqrt
}

import strategicprimer.model.map {
    Point,
    MapDimensions
}
"A class to compare [[Point]]s based on their distance to a specified point (such as a
 player's HQ)."
shared class DistanceComparator(base, dimensions) {
    "The point we want to measure distance from."
    Point base;
    "The dimensions of the map. May, but shouldn't, be null."
    MapDimensions? dimensions;
    "Returns a value that is proportional to the distance from the base to the given
     point: in fact the *square* of the distance, to avoid taking an expensive square
     root."
    Integer distance(Point point) {
        Integer colDistRaw = (point.column - base.column).magnitude;
        Integer rowDistRaw = (point.row - base.row).magnitude;
        Integer colDist;
        Integer rowDist;
        if (exists dimensions, colDistRaw > dimensions.columns / 2) {
            colDist = dimensions.columns - colDistRaw;
        } else {
            colDist = colDistRaw;
        }
        if (exists dimensions, rowDistRaw > dimensions.rows / 2) {
            rowDist = dimensions.rows - rowDistRaw;
        } else {
            rowDist = rowDistRaw;
        }
        return (colDist * colDist) + (rowDist * rowDist);
    }
    "Compare two points on the basis of distance from the base point."
    shared Comparison compare(Point firstPoint, Point secondPoint) =>
            distance(firstPoint) <=> distance(secondPoint);
    """Returns a String describing how far a point is from "HQ", which the base point is
       presumed to be."""
    shared String distanceString(Point point, String name = "HQ") {
        Integer dist = distance(point);
        assert (dist >= 0);
        if (dist == 0) {
            return " (at ``name``)";
        } else {
            return " (``Float.format(sqrt(dist.float), 0, 1)`` tiles from ``name``)";
        }
    }
}

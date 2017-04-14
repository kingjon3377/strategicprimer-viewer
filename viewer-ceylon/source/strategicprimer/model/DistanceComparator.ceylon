import ceylon.math.float {
    sqrt
}

import lovelace.util.common {
    todo
}

import strategicprimer.model.map {
    Point
}
"A class to compare [[Point]]s based on their distance to a specified point (such as a
 player's HQ)."
shared class DistanceComparator(base) {
    "The point we want to measure distance from."
    Point base;
    "Returns a value that is proportional to the distance from the base to the given
     point: in fact the *square* of the distance, to avoid taking an expensive square
     root."
    Integer distance(Point point) =>
            ((point.column - base.column) * (point.column - base.column)) +
                ((point.row - base.row) * (point.row - base.row));
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
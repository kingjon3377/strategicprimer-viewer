import ceylon.collection {
    MutableList,
    ArrayList
}

import model.map {
    Point
}

import strategicprimer.viewer.model.map {
    MapDimensions,
    pointFactory
}
"A stream of the points in a square surrounding a point, with points that are closer
 appearing multiple times."
shared {Point*} surroundingPointIterable(Point startingPoint, MapDimensions dimensions,
        Integer radius = 2) {
    Integer roundColumn(Integer column) {
        if (column < 0) {
            return dimensions.columns + column;
        } else {
            return column % dimensions.columns;
        }
    }
    Integer roundRow(Integer row) {
        if (row < 0) {
            return dimensions.rows + row;
        } else {
            return row % dimensions.rows;
        }
    }
    MutableList<Point> points = ArrayList<Point>();
    for (inner in (0..(radius + 1)).reversed) {
        Integer lowerBound = 0 - inner;
        Integer upperBound = inner + 1;
        for (row in lowerBound..upperBound) {
            for (column in lowerBound..upperBound) {
                points.add(pointFactory(roundRow(startingPoint.row + row),
                    roundColumn(startingPoint.col + column)));
            }
        }
    }
    return {*points};
}
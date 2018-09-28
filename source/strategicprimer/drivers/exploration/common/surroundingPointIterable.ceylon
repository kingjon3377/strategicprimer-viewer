import ceylon.collection {
    MutableList,
    ArrayList
}

import strategicprimer.model.common.map {
    Point,
    MapDimensions
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
    for (inner in (0..(radius)).reversed) {
        Integer lowerBound = 0 - inner;
        Integer upperBound = inner;
        for (row in lowerBound..upperBound) {
            for (column in lowerBound..upperBound) {
                points.add(Point(roundRow(startingPoint.row + row),
                    roundColumn(startingPoint.column + column)));
            }
        }
    }
    return points.sequence();
}

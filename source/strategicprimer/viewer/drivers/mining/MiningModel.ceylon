import ceylon.collection {
    MutableMap,
    HashMap,
    LinkedList,
    Queue
}

import strategicprimer.model.map {
    Point,
    pointFactory
}
import ceylon.random {
    DefaultRandom,
    Random
}
"Kinds of mines we know how to create."
class MineKind of normal | banded {
    """"Normal," which *tries* to create randomly-branching "veins"."""
    shared new normal {}
    "A mine which emphasizes layers, such as a sand mine."
    shared new banded {}
}
"A class to model the distribution of a mineral to be mined. Note that the constructor
 can be *very* computationally expensive!"
class MiningModel(initial, seed, kind) {
    "The status to give the mine's starting point."
    LodeStatus initial;
    "A number to seed the RNG"
    Integer seed;
    "What kind of mine to model."
    MineKind kind;
    MutableMap<Point, LodeStatus> unnormalized = HashMap<Point, LodeStatus>();
    unnormalized[pointFactory(0, 0)] = initial;
    Queue<Point> queue = LinkedList<Point>();
    queue.offer(pointFactory(0, 0));
    Random rng = DefaultRandom(seed);
    LodeStatus?(LodeStatus) horizontalGenerator;
    switch (kind)
    case (MineKind.normal) {
        horizontalGenerator = (LodeStatus current) => current.adjacent(rng.nextFloat);
    }
    case (MineKind.banded) {
        horizontalGenerator = (LodeStatus current) => current.bandedAdjacent(rng);
    }
    LodeStatus? verticalGenerator(LodeStatus current) => current.adjacent(rng.nextFloat);
    variable Integer counter = 0;
    variable Integer pruneCounter = 0;
    void unnormalizedSet(Point loc, LodeStatus? status) {
        if (exists status) {
            unnormalized[loc] = status;
        } else {
            unnormalized.remove(loc);
        }
    }
    void modelPoint(Point point) {
        Point left = pointFactory(point.row, point.column - 1);
        Point down = pointFactory(point.row + 1, point.column);
        Point right = pointFactory(point.row, point.column + 1);
        value current = unnormalized[point];
        if (!current exists) {
            return;
        }
        assert (exists current);
        if (!unnormalized.defines(right)) {
            unnormalizedSet(right, horizontalGenerator(current));
            queue.offer(right);
        }
        if (!unnormalized.defines(down)) {
            unnormalizedSet(down, verticalGenerator(current));
            queue.offer(down);
        }
        if (!unnormalized.defines(left)) {
            unnormalizedSet(left, horizontalGenerator(current));
            queue.offer(left);
        }
    }
    while (exists point = queue.accept()) {
        counter++;
        if (100000.divides(counter)) {
            process.writeLine(point.string);
        } else if (1000.divides(counter)) {
            process.write(".");
        }
        // Limit the size of the output spreadsheet.
        if (point.row.magnitude > 200 || point.column.magnitude > 100) {
            pruneCounter++;
            continue;
        } else {
            modelPoint(point);
        }
    }
    process.writeLine();
    process.writeLine("Pruned ``pruneCounter`` branches beyond our boundaries");
    for (row->points in unnormalized.keys.group(Point.row).
                sort((numOne->_, numTwo->__) => numTwo<=>numOne)) {
        if (!points.map(unnormalized.get).coalesced.empty) {
            break;
        }
        for (point in points) {
            unnormalized.remove(point);
        }
    }
    for (column->points in unnormalized.keys.group(Point.column).
                sort((numOne->_, numTwo->__) => numOne<=>numTwo)) {
        if (!points.map(unnormalized.get).coalesced.empty) {
            break;
        }
        for (point in points) {
            unnormalized.remove(point);
        }
    }
    for (column->points in unnormalized.keys.group(Point.column).
                sort((numOne->_, numTwo->__) => numTwo<=>numOne)) {
        if (!points.map(unnormalized.get).coalesced.empty) {
            break;
        }
        for (point in points) {
            unnormalized.remove(point);
        }
    }
    Integer minimumColumn = min(unnormalized.keys.map(Point.column)) else 0;
    "A mapping from positions (normalized so they could be spit out into a spreadsheet)
     to [[LodeStatus]]es."
    Map<Point, LodeStatus> data = map {
        *unnormalized
            .map((key->status) => pointFactory(key.row,
                key.column - minimumColumn)->status)
    };
    "The farthest row and column we reached."
    shared Point maximumPoint = pointFactory(max(data.keys.map(Point.row)) else 0,
        max(data.keys.map(Point.column)) else 0);
    shared LodeStatus? statusAt(Point point) => data[point];
}

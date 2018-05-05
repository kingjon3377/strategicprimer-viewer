import ceylon.collection {
    MutableMap,
    HashMap,
    LinkedList,
    Queue
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
native("jvm")
class MiningModel(initial, seed, kind) {
    "The status to give the mine's starting point."
    LodeStatus initial;
    "A number to seed the RNG"
    Integer seed;
    "What kind of mine to model."
    MineKind kind;
    MutableMap<[Integer, Integer], LodeStatus> unnormalized = HashMap<[Integer, Integer], LodeStatus>();
    unnormalized[[0, 0]] = initial;
    Queue<[Integer, Integer]> queue = LinkedList<[Integer, Integer]>();
    queue.offer([0, 0]);
    Random rng = DefaultRandom(seed);
    LodeStatus?(LodeStatus) horizontalGenerator;
    Integer getColumn([Integer, Integer] tuple) => tuple.rest.first;
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
    void unnormalizedSet([Integer, Integer] loc, LodeStatus? status) {
        if (exists status) {
            unnormalized[loc] = status;
        } else {
            unnormalized.remove(loc);
        }
    }
    void modelPoint([Integer, Integer] point) {
        Integer row = point.first;
        Integer column = point.rest.first;
        [Integer, Integer] left = [row, column - 1];
        [Integer, Integer] down = [row + 1, column];
        [Integer, Integer] right = [row, column + 1];
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
        if (point.first.magnitude > 200 || getColumn(point).magnitude > 100) {
            pruneCounter++;
            continue;
        } else {
            modelPoint(point);
        }
    }
    process.writeLine();
    process.writeLine("Pruned ``pruneCounter`` branches beyond our boundaries");
    for (row->points in unnormalized.keys.group(Tuple.first).
                sort((numOne->_, numTwo->__) => numTwo<=>numOne)) { // TODO: Use comparingOn instead of a lambda
        if (!points.map(unnormalized.get).coalesced.empty) {
            break;
        }
        for (point in points) {
            unnormalized.remove(point);
        }
    }
    for (column->points in unnormalized.keys.group(getColumn).
                sort((numOne->_, numTwo->__) => numOne<=>numTwo)) { // TODO: use comparingOn
        if (!points.map(unnormalized.get).coalesced.empty) {
            break;
        }
        for (point in points) {
            unnormalized.remove(point);
        }
    }
    for (column->points in unnormalized.keys.group(getColumn).
                sort((numOne->_, numTwo->__) => numTwo<=>numOne)) { // TODO: Use comparingOn
        if (!points.map(unnormalized.get).coalesced.empty) {
            break;
        }
        for (point in points) {
            unnormalized.remove(point);
        }
    }
    Integer minimumColumn = min(unnormalized.keys.map(getColumn)) else 0;
    "A mapping from positions (normalized so they could be spit out into a spreadsheet)
     to [[LodeStatus]]es."
    Map<[Integer, Integer], LodeStatus> data = map(
        unnormalized
            .map(([row, column]->status) => [row, column - minimumColumn]->status));
    "The farthest row and column we reached."
    shared [Integer, Integer] maximumPoint = [max(data.keys.map(Tuple.first)) else 0,
        max(data.keys.map(getColumn)) else 0];
    shared LodeStatus? statusAt([Integer, Integer] point) => data[point];
}

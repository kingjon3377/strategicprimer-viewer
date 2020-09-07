import strategicprimer.drivers.common {
    CLIDriver,
    IMultiMapModel,
    emptyOptions,
    SPOptions
}
import strategicprimer.model.common.map {
    IMapNG,
    Subsettable,
    IFixture,
    Direction
}

"An app to produce a difference between two maps, to aid understanding what an explorer has
 found. This modifies non-main maps in place; only run on copies or under version control!"
shared class SubtractCLI(shared actual IMultiMapModel model) satisfies CLIDriver {
    Boolean isSubset(IFixture one, IFixture two) { // TODO: Make static if we convert to constructor
        if (is Subsettable<IFixture> one) {
            return one.isSubset(two, noop);
        } else {
            return one == two;
        }
    }

    shared actual SPOptions options = emptyOptions;

    shared actual void startDriver() {
        IMapNG first = model.map;
        for (map->[path, modified] in model.subordinateMaps) {
            variable Boolean modifiedFlag = modified;
            for (loc in map.locations) {
                if (!modifiedFlag) {
                    model.setModifiedFlag(map, true);
                    modifiedFlag = true;
                }
                if (exists terrain = first.baseTerrain[loc], exists ours = map.baseTerrain[loc], terrain == ours) {
                    map.baseTerrain[loc] = null;
                }
                map.removeRivers(loc, *first.rivers.get(loc)); // TODO: syntax sugar
                Map<Direction, Integer> mainRoads = first.roads.getOrDefault(loc, emptyMap);
                Map<Direction, Integer> knownRoads = map.roads.getOrDefault(loc, emptyMap);
                for (direction->road in knownRoads) {
                    if (mainRoads.getOrDefault(direction, 0) >= road) {
                        map.setRoadLevel(loc, direction, 0);
                    }
                }
                if (first.mountainous.get(loc)) { // TODO: syntax sugar
                    map.mountainous[loc] = false;
                }
                for (fixture in map.fixtures.get(loc)) { // TODO: syntax sugar
                    if (first.fixtures.get(loc).any((item) => isSubset(item, fixture))) { // TODO: syntax sugar
                        map.removeFixture(loc, fixture);
                    }
                }
            }
        }
    }
}

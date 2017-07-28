import lovelace.util.common {
    DelayedRemovalMap
}

import strategicprimer.model {
    DistanceComparator
}
import strategicprimer.model.map {
    IFixture,
    Point,
    MapDimensions
}
import strategicprimer.model.map.fixtures.mobile {
    IWorker
}
import strategicprimer.model.map.fixtures.mobile.worker {
    WorkerStats
}
"A report generator for workers. We do not cover Jobs or Skills; see the main report for
 that."
shared class WorkerTabularReportGenerator(Point hq, MapDimensions dimensions)
        satisfies ITableGenerator<IWorker> {
    "The header row of the table."
    shared actual [String+] headerRow = ["Distance", "Location", "HP", "Max HP", "Str",
        "Dex", "Con", "Int", "Wis", "Cha"];
    "The file-name to (by default) write this table to."
    shared actual String tableName = "workers";
    "Create a GUI table row representing a worker."
    shared actual {String+} produce(
            DelayedRemovalMap<Integer, [Point, IFixture]> fixtures, IWorker item,
            Point loc) {
        if (exists stats = item.stats) {
            return {distanceString(loc, hq, dimensions), loc.string, item.name,
                stats.hitPoints.string, stats.maxHitPoints.string,
                for (stat in { stats.strength, stats.dexterity, stats.constitution,
                    stats.intelligence, stats.wisdom, stats.charisma })
                        WorkerStats.getModifierString(stat) };
        } else {
            return {distanceString(loc, hq, dimensions), loc.string, item.name,
                *(0..9).map((num) => "---")};
        }
    }
    "Compare two worker-location pairs."
    shared actual Comparison comparePairs([Point, IWorker] one,
            [Point, IWorker] two) {
        IWorker first = one.rest.first;
        IWorker second = two.rest.first;
        Comparison cmp = DistanceComparator(hq, dimensions).compare(one.first, two.first);
        if (cmp == equal) {
            return (first.name.compare(second.name));
        } else {
            return cmp;
        }
    }
}

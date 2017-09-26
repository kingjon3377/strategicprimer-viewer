import strategicprimer.model.map {
    Point,
    IFixture
}
import strategicprimer.model.map.fixtures.mobile {
    IWorker
}
import lovelace.util.common {
    DelayedRemovalMap
}
import ceylon.collection {
    MutableList,
    ArrayList
}
"A generator to produce a tabular report of workers' skill improvements. Because
 [[WorkerTabularReportGenerator]] handles workers, we don't remove anything from the
 [[DelayedRemovalMap]] we are passed."
shared class SkillTabularReportGenerator()
        satisfies ITableGenerator<IWorker> {
    "For this purpose, compare by worker name only."
    shared actual Comparison comparePairs([Point, IWorker] one, [Point, IWorker] two) =>
            one.rest.first.name <=> two.rest.first.name;
    shared actual [String+] headerRow => ["Worker", "Job", "Skill"];
    shared actual {{String+}*} produce(
            DelayedRemovalMap<Integer,[Point, IFixture]> fixtures, IWorker item, Integer key, Point loc) {
        MutableList<{String+}> retval = ArrayList<{String+}>();
        for (job in item) {
            variable Boolean any = false;
            for (skill in job) {
                if (!skill.empty) {
                    any = true;
                    retval.add([item.name, "``job.name`` ``job.level``",
                        "``skill.name`` ``skill.level``"]);
                }
            }
            if (!any, job.level > 0) {
                retval.add([item.name, "``job.name`` ``job.level``", "---"]);
            }
        }
        // We deliberately do *not* remove the worker from the collection!
        return retval;
    }
    shared actual String tableName => "skills";
}

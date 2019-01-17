import strategicprimer.model.common.map {
    HasName
}
import strategicprimer.model.common.map.fixtures {
    UnitMember
}
import strategicprimer.viewer.drivers.worker_mgmt {
    UnitMemberListener
}
import strategicprimer.model.common.map.fixtures.mobile.worker {
    ISkill
}

"A listener to print a line whenever a worker gains a level."
object levelListener
        satisfies LevelGainListener&UnitMemberListener&SkillSelectionListener {
    "The current worker."
    variable UnitMember? worker = null;

    "The current skill."
    variable ISkill? skill = null;

    shared actual void selectSkill(ISkill? selectedSkill) => skill = selectedSkill;

    shared actual void memberSelected(UnitMember? old, UnitMember? selected) =>
            worker = selected;

    "Wrapper around [[HasName]].getName() that also handles non-HasName objects using
     their `string` attribute."
    String getName(Object named) {
        if (is HasName named) {
            return named.name;
        } else {
            return named.string;
        }
    }

    "Notify the user of a gained level."
    shared actual void level(String workerName, String jobName, String skillName,
            Integer gains, Integer currentLevel) {
        String actualWorkerName;
        String actualSkillName;
        if (!workerName.empty, workerName != "unknown") {
            actualWorkerName = workerName;
        } else if (exists localWorker = worker) {
            actualWorkerName = getName(localWorker);
        } else {
            return;
        }
        if (!skillName.empty, skillName != "unknown") {
            actualSkillName = skillName;
        } else if (exists localSkill = skill) {
            actualSkillName = getName(localSkill);
        } else {
            return;
        }
        String count = (gains == 1) then "a level" else gains.string + " levels";
        process.writeLine("``actualWorkerName`` gained ``count`` in ``actualSkillName``");
    }
}

import model.map {
    HasName
}
import model.map.fixtures {
    UnitMember
}
import model.listeners {
    LevelGainListener,
    UnitMemberListener
}
import model.map.fixtures.mobile.worker {
    ISkill
}
"A listener to print a line whenever a worker gains a level."
object levelListener satisfies LevelGainListener&UnitMemberListener&SkillSelectionListener {
    "The current worker."
    variable UnitMember? worker = null;
    "The current skill."
    variable ISkill? skill = null;
    shared actual void selectSkill(ISkill? selectedSkill) => skill = selectedSkill;
    shared actual void memberSelected(UnitMember? old, UnitMember? selected) =>
            worker = selected;
    "Wrapper around [[HasName]].getName() that also handles non-HasName objects using their
     `string` attribute."
    String getName(Object named) {
        if (is HasName named) {
            return named.name;
        } else {
            return  named.string;
        }
    }
    "Notify the user of a gained level."
    shared actual void level() {
        if (exists localWorker = worker, exists localSkill = skill) {
            process.writeLine("``getName(localWorker)`` gained a level in ``getName(
                localSkill)``");
        }
    }
}

import ceylon.collection {
    ArrayList,
    MutableList
}

import lovelace.util.jvm {
    singletonRandom
}

import strategicprimer.model.map.fixtures.mobile.worker {
    IJob,
    Skill,
    ISkill
}
import strategicprimer.drivers.common {
    ICLIHelper
}
"Let the user add hours to a Skill or Skills in a Job."
void advanceJob(IJob job, ICLIHelper cli) {
    MutableList<ISkill> skills = ArrayList{ *job };
    cli.loopOnMutableList<ISkill>(skills, (clh, List<ISkill> list) => clh.chooseFromList(
            list, "Skills in Job:", "No existing Skills.", "Skill to advance: ", false),
        "Select another Skill in this Job? ",
                (MutableList<ISkill> list, ICLIHelper clh) {
            String skillName = clh.inputString("Name of new Skill: ");
            job.addSkill(Skill(skillName, 0, 0));
            list.clear();
            for (skill in job) {
                list.add(skill);
            }
            return list.find((item) => skillName == item.name);
        }, (ISkill skill, clh) {
            Integer oldLevel = skill.level;
            Integer hours = clh.inputNumber("Hours of experience to add: ");
            // TODO: Make frequency of leveling checks (i.e. size of hour-chunks to add at
            // a time) configurable. This is correct (per documentation before I added
            // support for workers to the map format) for ordinary experience, but workers
            // learning or working under a more experienced mentor can get multiple
            // "hours" per hour, and they should only check for a level with each
            // *actual* hour.
            for (hour in 0:hours) {
                skill.addHours(1, singletonRandom.nextInteger(100));
                if (skill.level != oldLevel) {
                    clh.println("Worker(s) gained a level in ``skill.name``");
                }
            }
        });
}

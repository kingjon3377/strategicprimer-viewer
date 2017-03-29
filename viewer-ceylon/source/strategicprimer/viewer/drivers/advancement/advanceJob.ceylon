import ceylon.collection {
    ArrayList,
    MutableList
}

import lovelace.util.jvm {
    singletonRandom
}

import strategicprimer.viewer.model.map.fixtures.mobile.worker {
    IJob,
    Skill,
    ISkill
}
import strategicprimer.viewer.drivers {
    ICLIHelper
}
"Let the user add hours to a Skill or Skills in a Job."
void advanceJob(IJob job, ICLIHelper cli) {
    MutableList<ISkill> skills = ArrayList{ *job };
    cli.loopOnMutableList<ISkill>(skills, (clh) => clh.chooseFromList(skills,
        "Skills in Job:", "No existing Skills.", "Skill to advance: ", false),
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
            skill.addHours(clh.inputNumber("Hours of experience to add: "),
                singletonRandom.nextInt(100));
            if (skill.level == oldLevel) {
                clh.print("Worker(s) gained a level in ``skill.name``");
            }
        });
}

import lovelace.util.jvm {
    singletonRandom
}

import strategicprimer.model.map.fixtures.mobile {
    IWorker
}
import strategicprimer.model.map.fixtures.mobile.worker {
    IJob,
    ISkill,
    Skill
}
import strategicprimer.drivers.common.cli {
    ICLIHelper
}
import ceylon.collection {
    MutableList,
    ArrayList
}
"Let the user add experience in a single Skill to all of a list of workers."
void advanceWorkersInSkill(String jobName, String skillName, ICLIHelper cli,
        IWorker* workers) {
    Integer hours = cli.inputNumber("Hours of experience to add: ");
    for (worker in workers) {
        IJob job = worker.getJob(jobName);
        ISkill skill = job.getSkill(skillName);
        Integer oldLevel = skill.level;
        skill.addHours(hours, singletonRandom.nextInteger(100));
        if (skill.level != oldLevel) {
            if (oldLevel == 0, skill.name == "miscellaneous",
                    cli.inputBooleanInSeries("``worker.name`` gained ``skill.
                            level`` level(s) in miscellaneous, choose another skill?",
                        "misc-replacement")) {
                MutableList<String> gains = ArrayList<String>();
                for (i in 0:skill.level) {
		    // TODO: Choose from existing instead of always getting a new string
                    String replacementName = cli.inputString("Skill to gain level in: ");
                    ISkill replacement = Skill(replacementName, 1, 0);
                    job.removeSkill(skill);
                    job.addSkill(replacement);
                    gains.add(replacementName);
                }
                for (name->count in gains.frequencies()) {
                    if (count == 1) {
                        cli.println("``worker.name`` gained a level in ``name``");
                    } else {
                        cli.println("``worker.name`` gained ``count`` levels in ``name``");
                    }
                }
            } else {
                cli.println("``worker.name`` gained a level in ``skill.name``");
            }
        }
    }
}

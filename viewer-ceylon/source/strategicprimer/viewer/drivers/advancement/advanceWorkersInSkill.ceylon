import lovelace.util.jvm {
    singletonRandom
}

import strategicprimer.viewer.model.map.fixtures.mobile {
    IWorker
}
import strategicprimer.viewer.model.map.fixtures.mobile.worker {
    IJob,
    Job,
    Skill,
    ISkill
}
import strategicprimer.viewer.drivers {
    ICLIHelper
}
"Let the user add experience in a single Skill to all of a list of workers."
void advanceWorkersInSkill(String jobName, String skillName, ICLIHelper cli,
        IWorker* workers) {
    Integer hours = cli.inputNumber("Hours of experience to add: ");
    for (worker in workers) {
        IJob job = worker.getJob(jobName);
        ISkill skill = job.getSkill(skillName);
        Integer oldLevel = skill.level;
        skill.addHours(hours, singletonRandom.nextInt(100));
        if (skill.level != oldLevel) {
            cli.println("``worker.name`` gained a level in ``skill.name``");
        }
    }
}

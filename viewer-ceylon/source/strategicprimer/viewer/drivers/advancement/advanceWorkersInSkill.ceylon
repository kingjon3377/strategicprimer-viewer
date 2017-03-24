import model.map.fixtures.mobile {
    IWorker
}
import strategicprimer.viewer.drivers {
    ICLIHelper
}
import model.map.fixtures.mobile.worker {
    ISkill,
    IJob,
    Job,
    Skill
}
import util {
    SingletonRandom {
        singletonRandom=random
    }
}
"Let the user add experience in a single Skill to all of a list of workers."
void advanceWorkersInSkill(String jobName, String skillName, ICLIHelper cli,
        IWorker* workers) {
    Integer hours = cli.inputNumber("Hours of experience to add: ");
    for (worker in workers) {
        IJob job;
        if (exists tempJob = worker.getJob(jobName)) {
            job = tempJob;
        } else {
            worker.addJob(Job(jobName, 0));
            assert (exists secondTempJob = worker.getJob(jobName));
            job = secondTempJob;
        }
        ISkill skill;
        if (exists tempSkill = job.getSkill(skillName)) {
            skill = tempSkill;
        } else {
            job.addSkill(Skill(skillName, 0, 0));
            assert (exists secondTempSkill = job.getSkill(skillName));
            skill = secondTempSkill;
        }
        Integer oldLevel = skill.level;
        skill.addHours(hours, singletonRandom.nextInt(100));
        if (skill.level != oldLevel) {
            cli.println("``worker.name`` gained a level in ``skill.name``");
        }
    }
}

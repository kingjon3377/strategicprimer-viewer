import strategicprimer.viewer.model.map.fixtures.mobile {
    IWorker
}
import ceylon.collection {
    ArrayList,
    MutableList
}
import strategicprimer.viewer.drivers {
    ICLIHelper
}
import strategicprimer.viewer.model.map.fixtures.mobile.worker {
    IJob,
    ProxyJob,
    Skill,
    ISkill
}
"Let the user add experience in a given Job to all of a list of workers."
void advanceWorkersInJob(String jobName, ICLIHelper cli, IWorker* workers) {
    {IJob*} jobs = getWorkerJobs(jobName, *workers);
    MutableList<ISkill> skills = ArrayList {
        for (skill in ProxyJob(jobName, false, *workers)) skill
    };
    cli.loopOnMutableList(skills, (clh) => clh.chooseFromList(skills,
        "Skills in Jobs:", "No existing skills.", "Skill to advance: ", false),
        "Select another Skill in this Job? ",
                (MutableList<ISkill> list, clh) {
            String skillName = clh.inputString("Name of new Skill: ");
            for (job in jobs) {
                job.addSkill(Skill(skillName, 0, 0));
            }
            skills.clear();
            for (skill in ProxyJob(jobName, false, *workers)) {
                skills.add(skill);
            }
            return skills.find((item) => skillName == item.name);
        }, (ISkill skill, clh) => advanceWorkersInSkill(jobName, skill.name, clh, *workers));
}

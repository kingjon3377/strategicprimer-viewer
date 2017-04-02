import strategicprimer.model.map.fixtures.mobile.worker {
    IJob,
    Job
}
import strategicprimer.model.map.fixtures.mobile {
    IWorker
}
import ceylon.collection {
    ArrayList,
    MutableList
}
import strategicprimer.viewer.drivers {
    ICLIHelper
}
"Let the user add experience to a worker."
void advanceSingleWorker(IWorker worker, ICLIHelper cli) {
    MutableList<IJob> jobs = ArrayList { *worker };
    cli.loopOnMutableList(jobs, (clh) => clh.chooseFromList(jobs,
        "Jobs in worker:", "No existing Jobs.", "Job to advance: ", false),
        "Select another Job in this worker? ",
                (MutableList<IJob> list, clh) {
            String jobName = clh.inputString("Name of new Job: ");
            worker.addJob(Job(jobName, 0));
            list.clear();
            for (job in worker) {
                list.add(job);
            }
            return list.find((item) => jobName == item.name);
        }, advanceJob);
}

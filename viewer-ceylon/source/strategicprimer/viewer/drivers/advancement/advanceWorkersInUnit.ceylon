import ceylon.collection {
    ArrayList,
    MutableList
}

import strategicprimer.model.map.fixtures.mobile {
    IWorker,
    IUnit
}

import strategicprimer.drivers.common {
    ICLIHelper
}
import strategicprimer.model.map.fixtures.mobile.worker {
    IJob,
    ProxyWorker,
    Job
}
"Let the user add experience to a worker or workers in a unit."
void advanceWorkersInUnit(IUnit unit, ICLIHelper cli) {
    IWorker[] workers = [for (member in unit) if (is IWorker member) member];
    if (cli.inputBoolean("Add experience to workers individually? ")) {
        cli.loopOnList(workers, (clh) => clh.chooseFromList(workers,
            "Workers in unit:", "No unadvanced workers remain.", "Chosen worker: ",
            false).key,
            "Choose another worker? ", advanceSingleWorker);
    } else if (workers.empty) {
        cli.println("No workers in unit.");
    } else {
        MutableList<IJob> jobs = ArrayList { *ProxyWorker.fromUnit(unit) };
        cli.loopOnMutableList(jobs, (ICLIHelper clh) => clh.chooseFromList(
            jobs, "Jobs in workers:", "No existing jobs.",
            "Job to advance: ", false).key,
            "Select another Job in these workers? ",
                    (MutableList<IJob> list, ICLIHelper clh) {
                String jobName = clh.inputString("Name of new Job: ");
                for (worker in workers) {
                    worker.addJob(Job(jobName, 0));
                }
                list.clear();
                for (job in ProxyWorker.fromUnit(unit)) {
                    list.add(job);
                }
                return list.find((item) => jobName == item.name);
            }, (IJob job, clh) => advanceWorkersInJob(job.name, clh, *workers));
    }
}

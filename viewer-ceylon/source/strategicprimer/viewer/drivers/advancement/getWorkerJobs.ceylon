import strategicprimer.viewer.model.map.fixtures.mobile.worker {
    IJob,
    Job
}
import strategicprimer.viewer.model.map.fixtures.mobile {
    IWorker
}
import ceylon.collection {
    MutableMap,
    HashMap
}
"Ensure that there is a Job by the given name in each worker, and return a collection of
 those Jobs."
{IJob*} getWorkerJobs(String jobName, IWorker* workers) {
    MutableMap<String, IJob> jobs = HashMap<String, IJob>();
    // TODO: turn to comprehension and use ceylon.language.set
    for (worker in workers) {
        jobs.put(worker.name, worker.getJob(jobName));
    }
    return jobs.items;
}

import model.map.fixtures.mobile.worker {
    IJob,
    Job
}
import model.map.fixtures.mobile {
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
    for (worker in workers) {
        if (exists job = worker.getJob(jobName)) {
            jobs.put(worker.name, job);
        } else {
            IJob temp = Job(jobName, 0);
            worker.addJob(temp);
            jobs.put(worker.name, temp);
        }
    }
    return jobs.items;
}

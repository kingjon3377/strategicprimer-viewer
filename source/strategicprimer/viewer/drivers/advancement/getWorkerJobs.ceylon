import strategicprimer.model.map.fixtures.mobile {
    IWorker
}
import strategicprimer.model.map.fixtures.mobile.worker {
    IJob
}
"Ensure that there is a Job by the given name in each worker, and return a collection of
 those Jobs."
{IJob*} getWorkerJobs(String jobName, IWorker* workers) {
    return set { for (worker in workers) worker.getJob(jobName) };
}

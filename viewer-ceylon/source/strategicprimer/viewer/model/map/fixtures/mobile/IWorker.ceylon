import lovelace.util.common {
    todo
}

import strategicprimer.viewer.model.map {
    HasMutableImage,
    HasName,
    HasKind
}
import strategicprimer.viewer.model.map.fixtures {
    UnitMember
}
import strategicprimer.viewer.model.map.fixtures.mobile.worker {
    IJob,
    WorkerStats
}
"An interface for Workers."
shared interface IWorker satisfies UnitMember&{IJob*}&HasName&HasKind&HasMutableImage {
    "Add a Job. Returns whether the number of Jobs changed as a result of this.

     Note that this does not guarantee that the worker will contain this Job object, nor
     that any changes made to it will be applied to the Job that the worker already had or
     that is actually added. If levels *need* to be added, callers should geth the Job the
     worker contains after this returns using [[getJob]] and apply changes to that."
    todo("Make sure that pre-applied experience is applied if the worker already had a Job
          by this name", "Make void instead of Boolean?")
    shared formal Boolean addJob(IJob job);
    "The worker's race."
    todo("Make a default `kind` delegating to this here?")
    shared formal String race;
    "The worker's stats."
    shared formal WorkerStats? stats;
    "Clone the object."
    shared actual formal IWorker copy(Boolean zero);
    "Get the Job that the worker has with the given name, or a newly-constructed one if it
     didn't have one before."
    shared formal IJob getJob(String jobName);
}
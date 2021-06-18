import strategicprimer.model.common.map {
    HasImage,
    HasName,
    HasNotes,
    HasKind,
    HasPortrait
}
import strategicprimer.model.common.map.fixtures {
    Implement,
    UnitMember
}
import strategicprimer.model.common.map.fixtures.mobile.worker {
    IJob,
    WorkerStats
}

"An interface for Workers."
shared interface IWorker satisfies UnitMember&{IJob*}&HasName&HasKind&HasImage&HasNotes&HasPortrait {
    "The worker's race."
    shared formal String race;

    "An alias for (alternate method of querying) the worker's race."
    shared actual default String kind => race;

    "The worker's stats."
    shared formal WorkerStats? stats;

    "Clone the object."
    shared actual formal IWorker copy(Boolean zero);

    "Get the Job that the worker has with the given name, or a newly-constructed one if it
     didn't have one before."
    shared formal IJob getJob(String jobName);

    shared actual String plural => "Workers";

    "The animal, if any, this worker is personally mounted on."
    shared formal Animal? mount;

    "Any equipment associated with this worker personally. (Generally not much
     more than clothing.)"
    shared formal {Implement*} equipment;
}

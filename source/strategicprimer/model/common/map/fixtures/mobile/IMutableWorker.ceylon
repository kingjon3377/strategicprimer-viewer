import strategicprimer.model.common.map.fixtures.mobile.worker {
    IJob
}

import lovelace.util.common {
    todo
}

import strategicprimer.model.common.map {
    HasMutableImage,
    HasMutablePortrait
}

import strategicprimer.model.common.map.fixtures {
    Implement
}

"Mutator operations for [[IWorker]]."
shared interface IMutableWorker satisfies IWorker&HasMutableImage&HasMutablePortrait {
    "Add a Job. Returns whether the number of Jobs changed as a result of this.

     Note that this does not guarantee that the worker will contain this Job object, nor
     that any changes made to it will be applied to the Job that the worker already had or
     that is actually added. If levels *need* to be added, callers should geth the Job the
     worker contains after this returns using [[getJob]] and apply changes to that."
    todo("Make sure that pre-applied experience is applied if the worker already had a Job
          by this name", "Make void instead of Boolean?")
    shared formal Boolean addJob(IJob job);

    "The animal, if any, this worker is personally mounted on."
    shared actual formal variable Animal? mount;

    "Add an item of equipment that is associated with this worker personally."
    todo("Return whether it was already associated with the worker?")
    shared formal void addEquipment(Implement item);

    "Remove an item of equipment from this worker."
    todo("Make this return the actual item that was removed?")
    shared formal void removeEquipment(Implement item);
}

import strategicprimer.model.common.map.fixtures.mobile {
    IWorker
}

import strategicprimer.model.common.map.fixtures.mobile.worker {
    ISkill
}

"An interface for methods needed for worker advancement. Implementations of
 other driver-model interfaces are expected to also satisfy this interface as
 needed; a full implementation of this and no other interface is not expected
 to be needed."
shared interface IAdvancementModel satisfies IDriverModel {
    "Add a Job to the matching worker in all maps. Returns [[true]] if a
     matching worker was found in at least one map, [[false]] otherwise. If
     an existing Job by that name already existed, it is left alone."
    // TODO: Allow caller to create a non-zero-level Job?
    shared formal Boolean addJobToWorker(IWorker worker, String jobName);

    "Add hours to a Skill to the specified Job in the matching worker in all
     maps.  Returns [[true]] if a matching worker was found in at least one
     map, [[false]] otherwise. If the worker doesn't have that Skill in that
     Job, it is added first; if the worker doesn't have that Job, it is added
     first as in [[addJobToWorker]], then the skill is added to it. The
     [[contextValue]] is passed to
     [[strategicprimer.model.common.map.fixtures.mobile.worker::IMutableSkill.addHours]];
     it should be a random number between 0 and 99."
    // TODO: Take a level-up listener?
    shared formal Boolean addHoursToSkill(IWorker worker, String jobName, String skillName,
        Integer hours, Integer contextValue);

    "Replace [[one skill|delenda]] with [[another|replacement]] in the specified job
     in the specified worker in all maps. Unlike [[addHoursToSkill]], if a map does
     not have an *equal* Job in the matching worker, that map is completely
     skipped.  If the replacement is already present, just remove the first
     skill. Returns [[true]] if the operation was carried out in any of the
     maps, [[false]] otherwise."
    shared formal Boolean replaceSkillInJob(IWorker worker, String jobName, ISkill delenda,
        ISkill replacement);
}

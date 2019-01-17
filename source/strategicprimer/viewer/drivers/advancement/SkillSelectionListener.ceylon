import strategicprimer.model.common.map.fixtures.mobile.worker {
    ISkill,
    IJob
}

"An interface for objects that want to know when the user selects a Skill from a list or
 tree."
interface SkillSelectionListener {
    "Handle a new skill being selected."
    shared formal void selectSkill(ISkill? selectedSkill);
    "Tell the listener that the skill that may be selected soon is in the given Job."
    shared formal void selectJob(IJob? selectedJob);
}

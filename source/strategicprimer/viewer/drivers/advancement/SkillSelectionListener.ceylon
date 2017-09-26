import strategicprimer.model.map.fixtures.mobile.worker {
    ISkill
}
"An interface for objects that want to know when the user selects a Skill from a list or
 tree."
interface SkillSelectionListener {
    "Handle a new skill being selected."
    shared formal void selectSkill(ISkill? selectedSkill);
}

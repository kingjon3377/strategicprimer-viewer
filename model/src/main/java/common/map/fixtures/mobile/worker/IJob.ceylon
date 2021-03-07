import strategicprimer.model.common.map {
    Subsettable,
    HasName
}

"""Skill names that are suspicious when they are the only Skill a Job has. In many
   cases they should be "miscellaneous" instead."""
shared {String*} suspiciousSkills = ["hunter", "hunting", "explorer",
    "exploration", "research", "carpentry", "woodcutting", "farming",
    "food gathering", "scientist", "woodcutter", "farmer", "brickmaker",
    "brickmaking", "administration"];

"An interface for Jobs."
shared interface IJob satisfies HasName&{ISkill*}&Subsettable<IJob> {
    "The worker's Job level in this Job. Cannot be negative."
    shared formal Integer level;

    "Clone the Job."
    shared formal IJob copy();

    """A Job is "empty" if the worker has no levels in it and no experience in any skills
       it contains."""
    shared formal Boolean emptyJob;

    "Get a Skill by name. Constructs a new one if we didn't have one by that name before."
    shared formal ISkill getSkill(String skillName);
}

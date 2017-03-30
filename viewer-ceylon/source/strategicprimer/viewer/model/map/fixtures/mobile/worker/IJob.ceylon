import lovelace.util.common {
    todo
}
import strategicprimer.viewer.model.map {
	Subsettable
}
import model.map {
    HasName
}
"""Skill names that are suspicious when they are the only Skill a Job has. In many
   cases they should be "miscellaneous" instead."""
shared {String*} suspiciousSkills = {"hunter", "hunting", "explorer",
	"exploration", "research", "carpentry", "woodcutting", "farming",
	"food gathering", "scientist", "woodcutter", "farmer", "brickmaker",
	"brickmaking", "administration"};
"An interface for Jobs."
shared interface IJob satisfies HasName&{ISkill*}&Subsettable<IJob> {
	"Add a skill.

	 Note that this does not guarantee that the Job will contain this Skill object, nor
	 that any changes made to it will be applied to the Skill that the Job already had or
	 that is actually added. If levels or hours *need* to be added, callers should get the
	 Skill the Job contains after this returns using [[getSkill]] and apply changes to
	 that.

	 Returns whether this changed the collection of skills."
	todo("Remove that limitation", "Make void instead of Boolean?")
	shared formal Boolean addSkill(ISkill skill);
	"The worker's Job level in this Job. Cannot be negative."
	todo("Move variability, and addSkill(), to a mutator interface?")
	shared formal variable Integer level;
	"Clone the Job."
	shared formal IJob copy();
	"""A Job is "empty" if the worker has no levels in it and no experience in any skills
	   it contains."""
	shared formal Boolean emptyJob;
	"Get a Skill by name. Constructs a new one if we didn't have one by that name before."
	shared formal ISkill getSkill(String skillName);
}
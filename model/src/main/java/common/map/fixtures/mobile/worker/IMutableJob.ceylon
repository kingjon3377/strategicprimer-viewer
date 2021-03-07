import lovelace.util.common {
    todo
}

"Extension of [[IJob]] to add mutator methods."
shared interface IMutableJob satisfies IJob {
    "The worker's Job level in this Job. Cannot be negative."
    shared actual formal variable Integer level;

    "Add a skill.

     Note that this does not guarantee that the Job will contain this Skill object, nor
     that any changes made to it will be applied to the Skill that the Job already had or
     that is actually added. If levels or hours *need* to be added, callers should get the
     Skill the Job contains after this returns using [[getSkill]] and apply changes to
     that.

     Returns whether this changed the collection of skills."
    todo("FIXME: Remove that limitation")
    shared formal void addSkill(ISkill skill);

    """Remove a skill. Note that if the provided skill was not present (by its equals()),
       this is a no-op.

       This is expected to be used only for replacing "miscellaneous" levels, which had
       previously only been done by hand-editing the XML."""
    shared formal void removeSkill(ISkill skill);
}

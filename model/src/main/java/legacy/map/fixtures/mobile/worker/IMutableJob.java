package legacy.map.fixtures.mobile.worker;

/**
 * Extension of {@link IJob} to add mutator methods.
 */
public interface IMutableJob extends IJob {
	/**
	 * Set the worker's Job level in this Job. Cannot be negative.
	 */
	void setLevel(int level);

	/**
	 * Add a skill.
	 *
	 * Note that this does not guarantee that the Job will contain this
	 * Skill object, nor that any changes made to it will be applied to the
	 * Skill that the Job already had or that is actually added. If levels
	 * or hours *need* to be added, callers should get the Skill the Job
	 * contains after this returns using {@link #getSkill} and apply changes to
	 * that.
	 *
	 * FIXME: Remove that limitation
	 *
	 * TODO: Return whether this changed the collection of skills, as this used to be documented to do?
	 */
	void addSkill(ISkill skill);

	/**
	 * Remove a skill. Note that if the provided skill was not present (by
	 * its equals()), this is a no-op.
	 *
	 * This is expected to be used only for replacing "miscellaneous"
	 * levels, which had previously only been done by hand-editing the XML.
	 */
	void removeSkill(ISkill skill);
}

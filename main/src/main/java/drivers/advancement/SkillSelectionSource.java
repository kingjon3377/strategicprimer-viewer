package drivers.advancement;

/**
 * An interface for objects that handle the user's selection of Skills.
 *
 * TODO: is there a more generic interface that could be used instead?
 * (Probably not in Java, given non-reified generics.)
 */
interface SkillSelectionSource {
	/**
	 * Notify the given listener of newly selected skills.
	 */
	void addSkillSelectionListener(SkillSelectionListener listener);

	/**
	 * Stop notifying the given listener.
	 */
	void removeSkillSelectionListener(SkillSelectionListener listener);
}

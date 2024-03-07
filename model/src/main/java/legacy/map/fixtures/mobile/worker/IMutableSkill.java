package legacy.map.fixtures.mobile.worker;

/**
 * Extension of {@link ISkill} adding mutator methods.
 */
public interface IMutableSkill extends ISkill {
	/**
	 * Add hours of training or experience.
	 *
	 * @param hours     The number of hours to add.
	 * @param condition If less than or equal to the total number of hours
	 *                  after the addition, level up and zero the hours instead.
	 *
	 * TODO: Take an IntPredicate instead?
	 *
	 * TODO: Return some indication of whether there's been a level-up, or
	 * the hours total after this operation?
	 */
	void addHours(int hours, int condition);
}

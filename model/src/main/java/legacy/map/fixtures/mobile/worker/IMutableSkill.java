package legacy.map.fixtures.mobile.worker;

import java.util.function.IntPredicate;

/**
 * Extension of {@link ISkill} adding mutator methods.
 */
public interface IMutableSkill extends ISkill {
	/**
	 * Add hours of training or experience.
	 * @param hours the number of hours to add
	 * @param condition If true when applied to the current total number of hours, level
	 *                  up and zero the hours instead.
	 *
	 * TODO: Return some indication of whether there's been a level-up, or
	 * the hours total after this operation?
	 *
	 * TODO: subtract 100 hours (and round up to 0 if then negative) instead of zeroing?
	 */
	void addHours(int hours, IntPredicate condition);
}

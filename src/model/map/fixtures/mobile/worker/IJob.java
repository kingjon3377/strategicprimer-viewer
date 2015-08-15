package model.map.fixtures.mobile.worker;

import model.map.HasName;
import model.map.Subsettable;
/**
 * An interface for Jobs.
 * @author Jonathan Lovelace
 *
 */
public interface IJob extends HasName, Iterable<ISkill>, Subsettable<IJob> {

	/**
	 * Add a skill.
	 *
	 * @param skill the skill to add
	 * @return the result of the operation
	 */
	boolean addSkill(ISkill skill);

	/**
	 * @return the worker's level in the job
	 */
	int getLevel();
	/**
	 * @return a copy of this Job
	 * @param zero whether to "zero out" sensitive details
	 */
	IJob copy(boolean zero);
}
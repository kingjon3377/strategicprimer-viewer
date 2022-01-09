package drivers.common;

import common.map.fixtures.mobile.IUnit;
import common.map.fixtures.mobile.IWorker;

import common.map.fixtures.mobile.worker.ISkill;

/**
 * An interface for methods needed for worker advancement. Implementations of
 * other driver-model interfaces are expected to also satisfy this interface as
 * needed; a full implementation of this and no other interface is not expected
 * to be needed.
 */
public interface IAdvancementModel extends IDriverModel {
	/**
	 * Add a Job to the matching worker in all maps. Returns true if a
	 * matching worker was found in at least one map, false otherwise.
	 * If an existing Job by that name already existed, it is left alone.
	 *
	 * TODO: Allow caller to create a non-zero-level Job?
	 */
	boolean addJobToWorker(IWorker worker, String jobName);

	/**
	 * Add hours to a Skill to the specified Job in the matching worker in
	 * all maps.  Returns true if a matching worker was found in at least
	 * one map, false otherwise. If the worker doesn't have that Skill in
	 * that Job, it is added first; if the worker doesn't have that Job, it
	 * is added first as in {@link addJobToWorker}, then the skill is added
	 * to it. The {@link contextValue} is passed to {@link
	 * IMutableSkill#addHours}; it should be a random number between 0 and 99.
	 *
	 * TODO: Take a level-up listener?
	 */
	boolean addHoursToSkill(IWorker worker, String jobName, String skillName, int hours,
		int contextValue);

	/**
	 * Replace {@link delenda one skill} with {@link replacement another}
	 * in the specified job in the specified worker in all maps. Unlike
	 * {@link addHoursToSkill}, if a map does not have an <em>equal</em>
	 * Job in the matching worker, that map is completely skipped.  If the
	 * replacement is already present, just remove the first skill. Returns
	 * true if the operation was carried out in any of the maps, false otherwise.
	 */
	boolean replaceSkillInJob(IWorker worker, String jobName, ISkill delenda, ISkill replacement);

	/**
	 * Set the given unit's orders for the given turn to the given text.
	 * Returns true if a matching (and mutable) unit was found in at least
	 * one map, false otherwise.
	 */
	boolean setUnitOrders(IUnit unit, int turn, String results);

	/**
	 * Set the given unit's results for the given turn to the given text.
	 * Returns true if a matching (and mutable) unit was found in at least
	 * one map, false otherwise.
	 */
	boolean setUnitResults(IUnit unit, int turn, String results);
}
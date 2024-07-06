package drivers.common;

import legacy.map.fixtures.mobile.IUnit;
import legacy.map.fixtures.mobile.IWorker;

import legacy.map.fixtures.mobile.worker.ISkill;
import legacy.map.fixtures.mobile.worker.IMutableSkill;

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
	 * Add a skill, without any hours in it, to the specified worker in the
	 * specified Job in all maps. Returns true if a matching worker was
	 * found in at least one map, false otherwise. If no existing Job by
	 * that name already exists, a zero-level Job with that name is added
	 * first. If a Skill by that name already exists in the corresponding
	 * Job, it is left alone.
	 */
	boolean addSkillToWorker(IWorker worker, String jobName, String skillName);

	/**
	 * Add a skill, without any hours in it, to all workers in the
	 * specified Job in all maps. Returns true if at least one matching
	 * worker was found in at least one map, false otherwise. If a worker
	 * is in a different unit in some map, the Skill is still added to it.
	 * If no existing Job by that name already exists, a zero-level Job
	 * with that name is added first. If a Skill by that name already
	 * exists in the corresponding Job, it is left alone.
	 */
	boolean addSkillToAllWorkers(IUnit unit, String jobName, String skillName);

	/**
	 * Add hours to a Skill to the specified Job in the matching worker in
	 * all maps.  Returns true if a matching worker was found in at least
	 * one map, false otherwise. If the worker doesn't have that Skill in
	 * that Job, it is added first; if the worker doesn't have that Job, it
	 * is added first as in {@link #addJobToWorker}, then the skill is added
	 * to it. The "contextValue" is passed to {@link
	 * IMutableSkill#addHours}; it should be a
	 * random number between 0 and 99.
	 *
	 * TODO: Take a level-up listener?
	 */
	boolean addHoursToSkill(IWorker worker, String jobName, String skillName, int hours,
	                        int contextValue);

	/**
	 * Add hours to a Skill to the specified Job in all workers in the
	 * given unit in all maps. (If a worker is in a different unit in some
	 * maps, that worker will still receive the hours.) Returns true if at
	 * least one worker received hours, false otherwise. If a worker
	 * doesn't have that skill in that Job, it is added first; if it
	 * doesn't have that Job, it is added first as in {@link
	 * #addJobToWorker}, then the skill is added to it. The contextValue
	 * parameter is used to calculate a new value passed to {@link
	 * IMutableSkill#addHours} for each
	 * worker.
	 *
	 * TODO: Take a level-up listener?
	 */
	boolean addHoursToSkillInAll(IUnit unit, String jobName, String skillName,
	                             int hours, int contextValue);

	/**
	 * Replace "delenda" with "replacement" in the specified job in the
	 * specified worker in all maps. Unlike {@link #addHoursToSkill}, if
	 * a map does not have an <em>equal</em> Job in the matching worker, that
	 * map is completely skipped.  If the replacement is already present, just
	 * remove the first skill. Returns true if the operation was carried out in
	 * any of the maps, false otherwise.
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

package legacy.map.fixtures.mobile.worker;

import legacy.map.Subsettable;
import common.map.HasName;

import java.util.List;

/**
 * An interface for Jobs.
 */
public interface IJob extends HasName, Iterable<ISkill>, Subsettable<IJob> {
	/**
	 * Skill names that are suspicious when they are the only Skill a Job
	 * has. In many cases they should be "miscellaneous" instead.
	 */
	List<String> SUSPICIOUS_SKILLS =
			List.of("hunter", "hunting", "explorer", "exploration", "research", "carpentry",
					"woodcutting", "farming", "food gathering", "scientist", "woodcutter", "farmer", "brickmaker",
					"brickmaking", "administration");

	/**
	 * The worker's Job level in this Job. Cannot be negative.
	 */
	int getLevel();

	/**
	 * Clone the Job.
	 */
	IJob copy();

	/**
	 * A Job is "empty" if the worker has no levels in it and no experience
	 * in any skills it contains.
	 */
	boolean isEmpty();

	/**
	 * Get a Skill by name. Constructs a new one if we didn't have one by
	 * that name before.
	 */
	ISkill getSkill(String skillName);
}

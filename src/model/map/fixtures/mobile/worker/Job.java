package model.map.fixtures.mobile.worker;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.jdt.annotation.Nullable;

import util.ArraySet;
import util.NullCleaner;

/**
 * A Job a worker can work at.
 *
 * @author Jonathan Lovelace
 */
public class Job implements IJob { // NOPMD
	/**
	 * The name of the job.
	 */
	private String name;
	/**
	 * How many levels the worker has in the job.
	 */
	private final int level;
	/**
	 * The worker's level in various skills associated with the job.
	 */
	private final Set<ISkill> skillSet = new ArraySet<>();

	/**
	 * Constructor.
	 *
	 * @param jobName the name of the Job
	 * @param levels how many levels the worker has in the Job.
	 * @param skills the worker's level in the various skills associated with
	 *        the job.
	 */
	public Job(final String jobName, final int levels, final ISkill... skills) {
		super();
		name = jobName;
		level = levels;
		skillSet.addAll(Arrays.asList(skills));
	}

	/**
	 * Add a skill.
	 *
	 * @param skill the skill to add
	 * @return the result of the operation
	 */
	@Override
	public boolean addSkill(final ISkill skill) {
		return skillSet.add(skill);
	}

	/**
	 * @return the name of the job
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * @return the worker's level in the job
	 */
	@Override
	public int getLevel() {
		return level;
	}

	/**
	 * @return an iterator over the worker's level in the various skills
	 *         associated with the job
	 */
	@Override
	public final Iterator<ISkill> iterator() {
		return NullCleaner.assertNotNull(skillSet.iterator());
	}

	/**
	 * @param obj an object
	 * @return whether it's the same as this
	 */
	@Override
	public boolean equals(@Nullable final Object obj) {
		return this == obj || obj instanceof Job
				&& name.equals(((Job) obj).name) && level == ((Job) obj).level
				&& skillSet.equals(((Job) obj).skillSet);
	}

	/**
	 * @return a hash value for the Job.
	 */
	@Override
	public int hashCode() {
		return name.hashCode();
	}

	/**
	 * @return a String representation of the Job
	 */
	@Override
	public String toString() {
		return name + " (" + Integer.toString(level) + ')';
	}

	/**
	 * @param nomen the job's new name
	 */
	@Override
	public final void setName(final String nomen) {
		name = nomen;
	}
}

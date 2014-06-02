package model.map.fixtures.mobile.worker;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.Nullable;

import util.ArraySet;
import util.NullCleaner;
import util.Pair;

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
	 * If this returns false, the caller should append an indication of context,
	 * since Jobs don't have ID #s.
	 *
	 * @param obj
	 *            a Job
	 * @param ostream
	 *            a stream to explain our results on
	 * @param context
	 *            a string to print before every line of output, describing the
	 *            context
	 * @return whether the Job is a "subset" of this---same name, equal or lower
	 *         level, with no extra or higher-level or extra-experienced Skills.
	 * @throws IOException on I/O error writing output to the stream
	 */
	@Override
	public boolean isSubset(final IJob obj, final Appendable ostream,
			final String context) throws IOException {
		if (!name.equals(obj.getName())) {
			ostream.append(context);
			ostream.append("\tPassed Jobs with different names\n");
			return false;
		} else if (level < obj.getLevel()) {
			ostream.append(context);
			ostream.append("\tSubmap has higher level for Job ");
			ostream.append(name);
			ostream.append('\n');
			return false;
		} else {
			boolean retval = true;
			final Map<String, Pair<Integer, Integer>> ours = new HashMap<>();
			for (final ISkill skill : this) {
				if (ours.containsKey(skill.getName())) {
					ostream.append(context);
					ostream.append(" In Job ");
					ostream.append(name);
					ostream.append(":\tMaster map contains duplicate Skill ");
					ostream.append(skill.getName());
					ostream.append('\n');
					retval = false;
				} else {
					ours.put(skill.getName(), Pair.of(NullCleaner
							.assertNotNull(Integer.valueOf(skill.getLevel())),
							NullCleaner.assertNotNull(Integer.valueOf(skill
									.getHours()))));
				}
			}
			for (final ISkill skill : obj) {
				if (!ours.containsKey(skill.getName())) {
					ostream.append(context);
					ostream.append(" In Job ");
					ostream.append(name);
					ostream.append(":\tExtra skill ");
					ostream.append(skill.getName());
					ostream.append('\n');
					retval = false;
				} else {
					// TODO: Move this logic into Skill?
					final Pair<Integer, Integer> pair = ours.get(skill.getName());
					final int lvl = pair.first().intValue();
					final int hours = pair.second().intValue();
					if (skill.getLevel() > lvl) {
						ostream.append(context);
						ostream.append(" In Job");
						ostream.append(name);
						ostream.append(":\tExtra level(s) in ");
						ostream.append(skill.getName());
						ostream.append('\n');
						retval = false;
					} else if (skill.getLevel() == lvl && skill.getHours() > hours) {
						ostream.append(context);
						ostream.append(" In Job ");
						ostream.append(name);
						ostream.append(":\tExtra hours in ");
						ostream.append(skill.getName());
						ostream.append('\n');
						retval = false;
					}
				}
			}
			return retval;
		}
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

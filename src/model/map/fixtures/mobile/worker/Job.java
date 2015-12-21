package model.map.fixtures.mobile.worker;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import util.ArraySet;
import util.NullCleaner;
import util.Pair;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.StreamSupport;

/**
 * A Job a worker can work at.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2012-2015 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program. If not, see
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
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
	private final Collection<ISkill> skillSet = new ArraySet<>();

	/**
	 * Constructor.
	 *
	 * @param jobName the name of the Job
	 * @param levels  how many levels the worker has in the Job.
	 * @param skills  the worker's level in the various skills associated with the job.
	 */
	public Job(final String jobName, final int levels,
	           final @NonNull ISkill @NonNull ... skills) {
		name = jobName;
		level = levels;
		skillSet.addAll(Arrays.asList(skills));
	}

	/**
	 * @param zero whether to "zero out" sensitive information
	 * @return a copy of this
	 */
	@Override
	public IJob copy(final boolean zero) {
		if (zero) {
			return new Job(name, 0);
		} else {
			final IJob retval = new Job(name, level);
			for (final ISkill skill : this) {
				retval.addSkill(skill.copy(false));
			}
			return retval;
		}
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
	 * @return an iterator over the worker's level in the various skills associated with
	 * the job
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
		return (this == obj) || ((obj instanceof Job)
				                         && name.equals(((Job) obj).name) &&
				                         (level == ((Job) obj).level)
				                         && skillSet.equals(((Job) obj).skillSet));
	}

	/**
	 * If this returns false, the caller should append an indication of context, since
	 * Jobs don't have ID #s.
	 *
	 * @param obj     a Job
	 * @param ostream a stream to explain our results on
	 * @param context a string to print before every line of output, describing the
	 *                context
	 * @return whether the Job is a "subset" of this---same name, equal or lower level,
	 * with no extra or higher-level or extra-experienced Skills.
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
							                                  .assertNotNull(
									                                  Integer.valueOf(
											                                  skill
													                                  .getLevel())),
							NullCleaner.assertNotNull(Integer.valueOf(skill
									                                          .getHours
											                                           ()))));
				}
			}
			for (final ISkill skill : obj) {
				if (ours.containsKey(skill.getName())) {
					// TODO: Move this logic into Skill?
					final Pair<Integer, Integer> pair = ours.get(skill.getName());
					final int lvl = pair.first().intValue();
					final int hours = pair.second().intValue();
					if (skill.getLevel() > lvl) {
						ostream.append(context);
						ostream.append(" In Job ");
						ostream.append(name);
						ostream.append(":\tExtra level(s) in ");
						ostream.append(skill.getName());
						ostream.append('\n');
						retval = false;
					} else if ((skill.getLevel() == lvl) && (skill.getHours() > hours)) {
						ostream.append(context);
						ostream.append(" In Job ");
						ostream.append(name);
						ostream.append(":\tExtra hours in ");
						ostream.append(skill.getName());
						ostream.append('\n');
						retval = false;
					}
				} else {
					ostream.append(context);
					ostream.append(" In Job ");
					ostream.append(name);
					ostream.append(":\tExtra skill ");
					ostream.append(skill.getName());
					ostream.append('\n');
					retval = false;
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

	/**
	 * A Job is "empty" if the worker has no levels in it and no experience in any skills
	 * it contains.
	 *
	 * @return whether this Job is "empty"
	 */
	@Override
	public boolean isEmpty() {
		if (level > 0) {
			return false;
		} else {
			return StreamSupport.stream(spliterator(), false).allMatch(ISkill::isEmpty);
		}
	}

	/**
	 * TODO: Should we add and return a new Skill in the not-present case?
	 *
	 * @param skillName the name of a Skill
	 * @return the Skill by that name in the Job, or none if not present
	 */
	@Override
	@Nullable
	public ISkill getSkill(final String skillName) {
		for (final ISkill skill : this) {
			if (skillName.equals(skill.getName())) {
				return skill;
			}
		}
		return null;
	}
}

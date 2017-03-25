package model.map.fixtures.mobile.worker;

import java.util.Formatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import util.LineEnd;

/**
 * A Job a worker can work at.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2012-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public class Job implements IJob {
	/**
	 * The worker's level in various skills associated with the job.
	 */
	private final Map<String, ISkill> skillSet = new HashMap<>();
	/**
	 * The name of the job.
	 */
	private final String name;
	/**
	 * How many levels the worker has in the job.
	 */
	private int level;

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
		Stream.of(skills).forEach(this::addSkill);
	}

	/**
	 * Clone the Job.
	 * @return a copy of this
	 */
	@Override
	public IJob copy() {
		final IJob retval = new Job(name, level);
		for (final ISkill skill : this) {
			retval.addSkill(skill.copy());
		}
		return retval;
	}

	/**
	 * Add a skill.
	 *
	 * @param skill the skill to add
	 * @return the result of the operation
	 */
	@Override
	public boolean addSkill(final ISkill skill) {
		if (skillSet.containsKey(skill.getName())) {
			final ISkill existing = skillSet.get(skill.getName());
			assert (existing != null);
			if (existing.equals(skill)) {
				return false;
			} else {
				skillSet.put(skill.getName(), skill);
				return true;
			}
		} else {
			skillSet.put(skill.getName(), skill);
			return true;
		}
	}

	/**
	 * The name of the Job.
	 * @return the name of the job
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * The worker's level in this Job.
	 * @return the worker's level in the job
	 */
	@Override
	public int getLevel() {
		return level;
	}

	/**
	 * Set the worker's level in this Job. Cannot be negative.
	 * @param newLevel the worker's new level in the job. Must not be negative.
	 */
	@Override
	public void setLevel(final int newLevel) {
		if (newLevel < 0) {
			throw new IllegalArgumentException("Job level cannot be negative");
		}
		level = newLevel;
	}

	/**
	 * An iterator over (the worker's level in) the Skills in this Job.
	 * @return an iterator over the worker's level in the various skills associated with
	 * the job
	 */
	@Override
	public final Iterator<ISkill> iterator() {
		return skillSet.values().iterator();
	}

	/**
	 * A Job equals an object iff it is a Job with the same name and level and identical
	 * skills.
	 * @param obj an object
	 * @return whether it's the same as this
	 */
	@Override
	public boolean equals(@Nullable final Object obj) {
		return (this == obj) || ((obj instanceof Job) && name.equals(((Job) obj).name) &&
										 (level == ((Job) obj).level) &&
										 skillSet.equals(((Job) obj).skillSet));
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
	 */
	@Override
	public boolean isSubset(final IJob obj, final Formatter ostream,
							final String context) {
		if (!areObjectsEqual(ostream, name, obj.getName(),
				"%s\tPassed Jobs with different names%n", context) ||
					!isConditionTrue(ostream, level >= obj.getLevel(),
							"%s\tSubmap has higher level for Job %s%n", context, name,
							LineEnd.LINE_SEP)) {
			return false;
		} else {
			boolean retval = true;
			final Map<String, ISkill> ours = new HashMap<>();
			for (final ISkill skill : this) {
				if (ours.containsKey(skill.getName())) {
					ostream.format(
							"%s In Job %s:\tMaster map contains duplicate Skill %s%n",
							context, name, skill.getName());
					retval = false;
				} else {
					ours.put(skill.getName(), skill);
				}
			}
			for (final ISkill skill : obj) {
				if (ours.containsKey(skill.getName())) {
					@Nullable ISkill ourSkill = ours.get(skill.getName());
					assert (ourSkill != null);
					retval &= ourSkill.isSubset(skill, ostream,
							context + " In Job " + name + ':');
				} else {
					ostream.format("%s In Job %s:\tExtra skill %s%n", context, name,
							skill.getName());
					retval = false;
				}
			}
			return retval;
		}
	}

	/**
	 * For stability, only the name is used to compute the hash value.
	 * @return a hash value for the Job.
	 */
	@Override
	public int hashCode() {
		return name.hashCode();
	}

	/**
	 * JobName (level).
	 * @return a String representation of the Job
	 */
	@Override
	public String toString() {
		return name + " (" + Integer.toString(level) + ')';
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
	 * Get a Skill by name, or null if not present.
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
		final ISkill retval = new Skill(skillName, 0, 0);
		skillSet.put(skillName, retval);
		return retval;
	}
}

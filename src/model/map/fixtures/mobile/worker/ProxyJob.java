package model.map.fixtures.mobile.worker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Formatter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import model.map.fixtures.mobile.IWorker;
import model.map.fixtures.mobile.ProxyFor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import util.NullCleaner;

/**
 * An IJob implementation to let the Job tree operate on a whole unit at once.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2014-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public final class ProxyJob implements IJob, ProxyFor<@NonNull IJob> {
	/**
	 * If false, the worker containing this is representing all the workers in a single
	 * unit; if true, it is representing corresponding workers in corresponding units in
	 * different maps. Thus, if true, we should use the same "random" seed repeatedly in
	 * any given adding-hours operation, and not if false.
	 */
	private final boolean parallel;
	/**
	 * Proxy-skills.
	 */
	private final Collection<ISkill> proxied = new ArrayList<>();
	/**
	 * Jobs we're proxying.
	 */
	private final List<IJob> proxiedJobs = new ArrayList<>();
	/**
	 * The names of skills we're proxying.
	 */
	private final Set<String> skillNames;
	/**
	 * The name of the Job.
	 */
	private String name;

	/**
	 * @param jobName         the name of the Job
	 * @param parallelWorkers whether the workers containing these jobs are corresponding
	 *                        workers in different maps (if true) or workers in the same
	 *                        unit (if false)
	 * @param workers         being proxied
	 */
	public ProxyJob(final String jobName, final boolean parallelWorkers,
					final IWorker... workers) {
		parallel = parallelWorkers;
		name = jobName;
		skillNames = new HashSet<>();
		for (final IWorker worker : workers) {
			boolean unmodified = true;
			for (final IJob job : worker) {
				if (jobName.equals(job.getName())) {
					proxiedJobs.add(job);
					for (final ISkill skill : job) {
						skillNames.add(skill.getName());
					}
					unmodified = false;
				}
			}
			if (unmodified) {
				//noinspection ObjectAllocationInLoop
				final IJob job = new Job(jobName, 0);
				worker.addJob(job);
				boolean absent = true;
				for (final IJob temp : worker) {
					if (temp.getName().equals(jobName)) {
						proxiedJobs.add(temp);
						absent = false;
						break;
					}
				}
				if (absent) {
					proxiedJobs.add(job);
				}
			}
		}
		final IJob @NonNull [] jobsArray = NullCleaner.assertNotNull(
				proxiedJobs.toArray(new IJob[proxiedJobs.size()]));
		proxied.addAll(skillNames.stream()
							   .map(skill -> new ProxySkill(skill, parallel,
																   NullCleaner
																		   .assertNotNull(
																				   jobsArray)))
							   .collect(Collectors.toList()));
	}

	/**
	 * @param zero whether to "zero out" sensitive information
	 * @return a copy of this
	 */
	@Override
	public IJob copy(final boolean zero) {
		final ProxyJob retval = new ProxyJob(name, parallel);
		for (final IJob job : proxiedJobs) {
			retval.addProxied(job.copy(zero));
		}
		return retval;
	}

	/**
	 * @return the name of the Job.
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * @param newName the new name
	 */
	@Override
	public void setName(final String newName) {
		name = newName;
	}

	/**
	 * @return an iterator over the skills that any of the proxied jobs has.
	 */
	@Override
	public Iterator<@NonNull ISkill> iterator() {
		return NullCleaner.assertNotNull(proxied.iterator());
	}

	/**
	 * @param skill a skill to proxy
	 * @return true if we weren't proxying that skill already
	 */
	@Override
	public boolean addSkill(final ISkill skill) {
		for (final ISkill proxy : proxied) {
			if (proxy.getName().equals(skill.getName())) {
				return false;
			}
		}
		proxied.add(new ProxySkill(skill.getName(), parallel, NullCleaner.assertNotNull(
				proxiedJobs.toArray(new IJob[proxiedJobs.size()]))));
		return true;
	}

	/**
	 * @return 0
	 */
	@Override
	public int getLevel() {
		return 0;
	}

	/**
	 * @param newLevel Ignored; always throws
	 */
	@Override
	public void setLevel(final int newLevel) {
		throw new IllegalStateException
					  ("Tried to set the level of all a worker's Jobs at once");
	}

	/**
	 * @return a String representation of the Job.
	 */
	@Override
	public String toString() {
		return name;
	}

	/**
	 * @param obj     ignored
	 * @param ostream a stream to report this call on
	 * @param context a string to print before every line of output, describing the
	 *                context
	 * @return false
	 */
	@Override
	public boolean isSubset(final IJob obj, final Formatter ostream,
							final String context) {
		ostream.format("%s\tisSubset called on ProxyJob%n", context);
		return false;
	}

	/**
	 * Proxy an additional Job.
	 *
	 * @param item the job to proxy for
	 */
	@SuppressWarnings("ObjectEquality")
	@Override
	public void addProxied(final IJob item) {
		if ((item == this) || !name.equals(item.getName())) {
			return;
		}
		proxiedJobs.add(item);
		for (final ISkill skill : proxied) {
			//noinspection unchecked
			((ProxyFor<IJob>) skill).addProxied(item);
		}
		final IJob[] jobsArray = NullCleaner.assertNotNull(
				proxiedJobs.toArray(new IJob[proxiedJobs.size()]));
		for (final ISkill skill : item) {
			if (!skillNames.contains(skill.getName())) {
				//noinspection ObjectAllocationInLoop
				proxied.add(new ProxySkill(skill.getName(), parallel, jobsArray));
			}
		}
	}

	/**
	 * @return the proxied Jobs.
	 */
	@Override
	public Iterable<IJob> getProxied() {
		return new ArrayList<>(proxiedJobs);
	}

	/**
	 * A Job is "empty" if the worker has no levels in it and no experience in any skills
	 * it contains.
	 *
	 * @return whether all of the Jobs this is a proxy for are "empty"
	 */
	@Override
	public boolean isEmpty() {
		return proxiedJobs.stream().allMatch(IJob::isEmpty);
	}

	/**
	 * @param skillName the name of a Skill
	 * @return the Skill by that name the Job has, or null if it has none
	 */
	@Nullable
	@Override
	public ISkill getSkill(final String skillName) {
		for (final ISkill skill : proxied) {
			if (skillName.equals(skill.getName())) {
				return skill;
			}
		}
		final ISkill retval =
				new ProxySkill(skillName, parallel, NullCleaner.assertNotNull(
						proxiedJobs.toArray(new IJob[proxiedJobs.size()])));
		proxied.add(retval);
		return retval;
	}

	/**
	 * @return Whether this should be considered (if true) a proxy for multiple
	 * representations of the same Job, e.g. in different maps, or (if false) a proxy for
	 * different related Jobs.
	 */
	@Override
	public boolean isParallel() {
		return parallel;
	}
}

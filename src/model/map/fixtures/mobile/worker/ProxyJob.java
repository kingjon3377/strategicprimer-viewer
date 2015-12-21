package model.map.fixtures.mobile.worker;

import model.map.fixtures.mobile.IWorker;
import model.map.fixtures.mobile.ProxyFor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import util.NullCleaner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * An IJob implementation to let the Job tree operate on a whole unit at once.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2014-2015 Jonathan Lovelace
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
public final class ProxyJob implements IJob, ProxyFor<@NonNull IJob> {
	/**
	 * If false, the worker containing this is representing all the workers in a single
	 * unit; if true, it is representing corresponding workers in corresponding units in
	 * different maps. Thus, if true, we should use the same "random" seed repeatedly in
	 * any given adding-hours operation, and not if false.
	 */
	private final boolean parallel;
	/**
	 * The name of the Job.
	 */
	private String name;
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
	 * @param nomen   the name of the Job
	 * @param parall  whether the workers containing these jobs are corresponding workers
	 *                in different maps (if true) or workers in the same unit (if false)
	 * @param workers being proxied
	 */
	public ProxyJob(final String nomen, final boolean parall,
	                final IWorker... workers) {
		parallel = parall;
		name = nomen;
		skillNames = new HashSet<>();
		for (final IWorker worker : workers) {
			boolean touched = false;
			for (final IJob job : worker) {
				if (nomen.equals(job.getName())) {
					proxiedJobs.add(job);
					for (final ISkill skill : job) {
						skillNames.add(skill.getName());
					}
					touched = true;
				}
			}
			if (!touched) {
				final IJob job = new Job(nomen, 0);
				worker.addJob(job);
				boolean found = false;
				for (final IJob temp : worker) {
					if (temp.getName().equals(nomen)) {
						proxiedJobs.add(temp);
						found = true;
						break;
					}
				}
				if (!found) {
					proxiedJobs.add(job);
				}
			}
		}
		final IJob @NonNull [] jobsArray =
				NullCleaner.assertNotNull(proxiedJobs
						                          .toArray(new IJob[proxiedJobs.size()
								                                   ]));
		proxied.addAll(skillNames.stream()
				               .map(skill -> new ProxySkill(skill, parallel, jobsArray))
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
	 * @param nomen the new name
	 */
	@Override
	public void setName(final String nomen) {
		name = nomen;
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
		proxied.add(new ProxySkill(skill.getName(), parallel,
				                          NullCleaner.assertNotNull(
						                          proxiedJobs.toArray(new
								                                              IJob[proxiedJobs
								                                                       .size()]))));
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
	 * @return a String representation of the Job.
	 */
	@Override
	public String toString() {
		return getName();
	}

	/**
	 * @param obj     ignored
	 * @param ostream a stream to report this call on
	 * @param context a string to print before every line of output, describing the
	 *                context
	 * @return false
	 * @throws IOException on I/O error writing output to the stream
	 */
	@Override
	public boolean isSubset(final IJob obj, final Appendable ostream,
	                        final String context) throws IOException {
		ostream.append(context);
		ostream.append("\tisSubset called on ProxyJob\n");
		return false;
	}

	/**
	 * Proxy an additional Job.
	 *
	 * @param item the job to proxy for
	 */
	@Override
	public void addProxied(final IJob item) {
		if (item == this || !name.equals(item.getName())) {
			return;
		}
		proxiedJobs.add(item);
		for (final ISkill skill : proxied) {
			((ProxyFor<IJob>) skill).addProxied(item);
		}
		final IJob[] jobsArray =
				NullCleaner.assertNotNull(proxiedJobs
						                          .toArray(new IJob[proxiedJobs.size()
								                                   ]));
		for (final ISkill skill : item) {
			if (!skillNames.contains(skill.getName())) {
				proxied.add(new ProxySkill(skill.getName(), parallel, jobsArray));
			}
		}
	}

	/**
	 * @return the proxied Jobs.
	 */
	@Override
	public Iterable<IJob> getProxied() {
		return proxiedJobs;
	}

	/**
	 * A Job is "empty" if the worker has no levels in it and no experience in any skills
	 * it contains.
	 *
	 * @return whether all of the Jobs this is a proxy for are "empty"
	 */
	@Override
	public boolean isEmpty() {
		return StreamSupport.stream(proxiedJobs.spliterator(), false).allMatch
				                                                              (IJob::isEmpty);
	}

	/**
	 * TODO: Should we return a new Skill (and add it) instead of null if not present?
	 *
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
		return null;
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

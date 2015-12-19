package model.map.fixtures.mobile.worker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import model.map.HasName;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import model.map.IFixture;
import model.map.fixtures.UnitMember;
import model.map.fixtures.mobile.IUnit;
import model.map.fixtures.mobile.IWorker;
import model.map.fixtures.mobile.ProxyFor;
import util.NullCleaner;

/**
 * An IWorker implementation to make the UI able to operate on all of a unit's
 * workers at once.
 *
 * This is part of the Strategic Primer assistive programs suite developed by
 * Jonathan Lovelace.
 *
 * Copyright (C) 2014-2015 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of version 3 of the GNU General Public License as published by the
 * Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 *
 */
public final class ProxyWorker implements IWorker, ProxyFor<@NonNull IWorker> {
	/**
	 * Logger.
	 */
	private static final Logger LOGGER = NullCleaner.assertNotNull(Logger.getLogger(ProxyWorker.class.getName()));
	/**
	 * If false, this is representing all the workers in a single unit; if true,
	 * it is representing corresponding workers in corresponding units in
	 * different maps.
	 */
	private final boolean parallel;
	/**
	 * The proxy Jobs.
	 */
	private final List<IJob> proxyJobs = new ArrayList<>();
	/**
	 * The jobs we're proxying for.
	 */
	private final Collection<String> jobNames = new HashSet<>();
	/**
	 * The workers being proxied.
	 */
	private final List<IWorker> workers = new ArrayList<>();
	/**
	 * No-op constructor for use by copy().
	 * @param paral whether this is a "parallel" or "serial" proxy
	 */
	private ProxyWorker(final boolean paral) {
		parallel = paral;
	}
	/**
	 * @param unit the unit to proxy for
	 */
	public ProxyWorker(final Iterable<UnitMember> unit) {
		parallel = false;
		for (final UnitMember member : unit) {
			if (member instanceof IWorker) {
				workers.add((IWorker) member);
				for (final HasName job : (IWorker) member) {
					jobNames.add(job.getName());
				}
			}
		}
		final IWorker @NonNull [] workerArray =
				NullCleaner.assertNotNull(workers.toArray(new IWorker[workers
						.size()]));
		proxyJobs.addAll(jobNames.stream().map(job -> new ProxyJob(job, parallel, workerArray))
				                 .collect(Collectors.toList()));
	}
	/**
	 * @return a copy of this proxy
	 * @param zero whether to "zero out" sensitive information
	 */
	@Override
	public IWorker copy(final boolean zero) {
		final IWorker retval = new ProxyWorker(parallel);
		for (final IWorker worker : workers) {
			addProxied(worker.copy(zero));
		}
		return retval;
	}
	/**
	 * @param proxied workers to proxy for
	 */
	public ProxyWorker(final @NonNull IWorker @NonNull ... proxied) {
		parallel = true;
		for (final IWorker worker : proxied) {
			if (worker == this) {
				continue;
			}
			workers.add(worker);
			for (final IJob job : worker) {
				jobNames.add(job.getName());
			}
		}
		proxyJobs.addAll(jobNames.stream().map(job -> new ProxyJob(job, parallel, proxied))
				                 .collect(Collectors.toList()));
	}
	/**
	 * @return -1, since this isn't a valid fixture.
	 */
	@Override
	public int getID() {
		return -1;
	}
	/**
	 * @param fix a fixture
	 * @return whether it's equal to this one
	 */
	@Override
	public boolean equalsIgnoringID(final IFixture fix) {
		return fix instanceof ProxyWorker
				&& proxyJobs.equals(((ProxyWorker) fix).proxyJobs);
	}
	/**
	 * @return The iterator over the proxied jobs.
	 */
	@Override
	@NonNull
	public Iterator<@NonNull IJob> iterator() {
		return NullCleaner.assertNotNull(proxyJobs.iterator());
	}
	/**
	 * @param job the job to add
	 * @return true if we weren't already proxying for it
	 */
	@Override
	public boolean addJob(final IJob job) {
		if (jobNames.contains(job.getName())) {
			return false;
		} else {
			final IJob proxy =
					new ProxyJob(job.getName(), parallel,
							NullCleaner.assertNotNull(workers
									.toArray(new IWorker[workers.size()])));
			jobNames.add(proxy.getName());
			proxyJobs.add(proxy);
			return true;
		}
	}
	/**
	 * @param obj ignored
	 * @param ostream a stream to report the call on
	 * @param context
	 *            a string to print before every line of output, describing the
	 *            context
	 * @return false
	 * @throws IOException on I/O error writing output to the stream
	 */
	@Override
	public boolean isSubset(final IFixture obj, final Appendable ostream,
			final String context) throws IOException {
		ostream.append(context);
		ostream.append("\tisSubset called on ProxyWorker\n");
		return false;
	}
	/**
	 * Proxy an additional worker.
	 * @param item the worker to proxy
	 */
	@Override
	public void addProxied(final IWorker item) {
		if (item == this) {
			return;
		}
		workers.add(item);
		final IWorker[] workerArray =
				NullCleaner.assertNotNull(workers.toArray(new IWorker[workers
						.size()]));
		final Collection<IJob> proxyJobsTemp = new ArrayList<>(proxyJobs);
		for (final IJob job : item) {
			final String name = job.getName();
			if (jobNames.contains(name)) {
				proxyJobs.stream().filter(proxyJob -> proxyJob.getName().equals(name)).forEach(proxyJob -> {
					((ProxyFor<IJob>) proxyJob).addProxied(job);
					proxyJobsTemp.remove(proxyJob);
				});
			} else {
				jobNames.add(name);
				proxyJobs.add(new ProxyJob(name, parallel, workerArray));
			}
			jobNames.add(job.getName());
		}
		for (final IJob proxyJob : proxyJobs) {
			final String name = proxyJob.getName();
			final IJob job = new Job(name, 0);
			((ProxyFor<IJob>) proxyJob).addProxied(job);
		}
	}
	/**
	 * @return the proxied Workers.
	 */
	@Override
	public Iterable<IWorker> getProxied() {
		return workers;
	}

	/**
	 * TODO: pass through to proxied workers.
	 * @return the name of an image to represent the worker
	 */
	@Override
	public String getDefaultImage() {
		for (final IWorker worker : workers) {
			return worker.getDefaultImage();
		}
		return "worker.png";
	}
	/**
	 * @param img the name of an image to use for this particular fixture
	 */
	@Override
	public void setImage(final String img) {
		LOGGER.log(Level.WARNING, "setImage() called on a ProxyWorker");
		for (final IWorker worker : workers) {
			worker.setImage(img);
		}
	}

	/**
	 * @return the name of an image to use for this particular fixture.
	 */
	@Override
	public String getImage() {
		@Nullable String image = null;
		for (final IWorker worker : workers) {
			if (image == null) {
				image = worker.getImage();
			} else if (!image.equals(worker.getImage())) {
				return "";
			}
		}
		if (image == null) {
			return "";
		} else {
			return image;
		}
	}
	/**
	 * @return the race of the workers
	 */
	@Override
	public String getKind() {
		@Nullable String kind = null;
		for (final IWorker worker : workers) {
			if (kind == null) {
				kind = worker.getKind();
			} else if (!kind.equals(worker.getKind())) {
				return "proxied";
			}
		}
		if (kind == null) {
			return "proxied";
		} else {
			return kind;
		}
	}
	/**
	 * @param nKind the new race of the proxied workers
	 */
	@Override
	public void setKind(final String nKind) {
		for (final IWorker worker : workers) {
			worker.setKind(nKind);
		}
	}
	/**
	 * @return the name of the workers (or "proxied" if they don't agree)
	 */
	@Override
	public String getName() {
		@Nullable String name = null;
		for (final IWorker worker : workers) {
			if (name == null) {
				name = worker.getName();
			} else if (!name.equals(worker.getName())) {
				return "proxied";
			}
		}
		if (name == null) {
			return "proxied";
		} else {
			return name;
		}
	}
	/**
	 * @param nomen the new name for the workers
	 */
	@Override
	public void setName(final String nomen) {
		for (final IWorker worker : workers) {
			worker.setName(nomen);
		}
	}
	/**
	 * @return the race of the proxied workers
	 */
	@Override
	public String getRace() {
		return getKind();
	}
	/**
	 * TODO: Should we add and return a new Job instead of null if not present?
	 *
	 * @param name the name of a Job
	 * @return the Job by that name the worker has, or null if it has none
	 */
	@Nullable
	@Override
	public IJob getJob(final String name) {
		for (final IJob job : proxyJobs) {
			if (name.equals(job.getName())) {
				return job;
			}
		}
		return null;
	}
	/**
	 * @return a string representation of this class
	 */
	@Override
	public String toString() {
		return "ProxyWorker";
	}

	/**
	 * @return Whether this should be considered (if true) a proxy for multiple representations of the same Worker,
	 * e.g. in different maps, or (if false) a proxy for different related Workers.
	 */
	@Override
	public boolean isParallel() {
		return parallel;
	}
}

package model.map.fixtures.mobile.worker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Formatter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.map.HasName;
import model.map.IFixture;
import model.map.fixtures.UnitMember;
import model.map.fixtures.mobile.IWorker;
import model.map.fixtures.mobile.ProxyFor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import util.TypesafeLogger;

/**
 * An IWorker implementation to make the UI able to operate on all of a unit's workers at
 * once.
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
public final class ProxyWorker implements IWorker, ProxyFor<@NonNull IWorker> {
	/**
	 * Logger.
	 */
	private static final Logger LOGGER = TypesafeLogger.getLogger(ProxyWorker.class);
	/**
	 * If false, this is representing all the workers in a single unit; if true, it is
	 * representing corresponding workers in corresponding units in different maps.
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
	 * Cached stats for the workers. Null if there are no workers being proxied, or if
	 * they do not share identical stats.
	 */
	@Nullable
	private WorkerStats stats;

	/**
	 * No-op constructor for use by copy().
	 *
	 * @param parallelProxy whether this is a "parallel" or "serial" proxy
	 */
	private ProxyWorker(final boolean parallelProxy) {
		parallel = parallelProxy;
		stats = null;
	}

	/**
	 * Constructor, for proxying all workers in a single unit.
	 * @param unit the unit to proxy for
	 */
	public ProxyWorker(final Iterable<UnitMember> unit) {
		parallel = false;
		stats = null;
		for (final UnitMember member : unit) {
			if (member instanceof IWorker) {
				final WorkerStats tempStats = ((IWorker) member).getStats();
				final WorkerStats priorStats = stats;
				if (workers.isEmpty()) {
					stats = tempStats;
				} else if (!Objects.equals(tempStats, priorStats)) {
					stats = null;
				}
				workers.add((IWorker) member);
				for (final HasName job : (IWorker) member) {
					jobNames.add(job.getName());
				}
			}
		}
		final IWorker @NonNull [] workerArray =
				workers.toArray(new IWorker[workers.size()]);
		jobNames.stream().map(job -> new ProxyJob(job, false, workerArray))
				.forEach(proxyJobs::add);
	}

	/**
	 * Constructor for proxying a series of (parallel) workers.
	 * @param proxied workers to proxy for
	 */
	@SuppressWarnings({"ObjectEquality", "OverloadedVarargsMethod"})
	public ProxyWorker(final @NonNull IWorker @NonNull ... proxied) {
		parallel = true;
		stats = null;
		for (final IWorker worker : proxied) {
			if (worker == this) {
				continue;
			}
			final WorkerStats tempStats = worker.getStats();
			final WorkerStats priorStats = stats;
			if (workers.isEmpty()) {
				stats = tempStats;
			} else if (!Objects.equals(tempStats, priorStats)) {
				stats = null;
			}
			workers.add(worker);
			for (final IJob job : worker) {
				jobNames.add(job.getName());
			}
		}
		jobNames.stream().map(job -> new ProxyJob(job, true, proxied))
				.forEach(proxyJobs::add);
	}

	/**
	 * Clone the object.
	 * @param zero whether to "zero out" sensitive information
	 * @return a copy of this proxy
	 */
	@Override
	public IWorker copy(final boolean zero) {
		final ProxyWorker retval = new ProxyWorker(parallel);
		for (final IWorker worker : workers) {
			retval.addProxied(worker.copy(zero));
		}
		return retval;
	}

	/**
	 * If parallel, returns the ID of the worker; otherwise, returns -1.
	 *
	 * @return the ID of parallel workers, or -1.
	 */
	@Override
	public int getID() {
		if (parallel) {
			return workers.stream().mapToInt(IWorker::getID).reduce(-1, (left, right) -> {
				if (left == right) {
					return left;
				} else {
					return -1;
				}
			});
		} else {
			return -1;
		}
	}

	/**
	 * An object is equal iff it is a ProxyWorker proxying the same Jobs.
	 *
	 * TODO: Should check parallel as well.
	 * @param fix a fixture
	 * @return whether it's equal to this one
	 */
	@SuppressWarnings({"CastToConcreteClass", "InstanceofInterfaces"})
	@Override
	public boolean equalsIgnoringID(final IFixture fix) {
		return (fix instanceof ProxyWorker) &&
					   proxyJobs.equals(((ProxyWorker) fix).proxyJobs);
	}

	/**
	 * An iterator over the workers' Jobs.
	 * @return The iterator over the proxied jobs.
	 */
	@Override
	@NonNull
	public Iterator<@NonNull IJob> iterator() {
		return proxyJobs.iterator();
	}

	/**
	 * Add a Job to the workers.
	 * @param job the job to add
	 * @return true if we weren't already proxying for it
	 */
	@Override
	public boolean addJob(final IJob job) {
		if (jobNames.contains(job.getName())) {
			return false;
		} else {
			final IJob proxy = new ProxyJob(job.getName(), parallel, workers.toArray(
					new IWorker[workers.size()]));
			jobNames.add(proxy.getName());
			proxyJobs.add(proxy);
			return true;
		}
	}

	/**
	 * Always returns false; proxies should not be involved in subset checking.
	 * @param obj     ignored
	 * @param ostream a stream to report the call on
	 * @param context a string to print before every line of output, describing the
	 *                context
	 * @return false
	 */
	@Override
	public boolean isSubset(final IFixture obj, final Formatter ostream,
							final String context)  {
		ostream.format("%s\tisSubset called on ProxyWorker%n", context);
		return false;
	}

	/**
	 * Proxy an additional worker.
	 *
	 * @param item the worker to proxy
	 */
	@SuppressWarnings("ObjectEquality")
	@Override
	public void addProxied(final IWorker item) {
		if (item == this) {
			return;
		}
		final WorkerStats tempStats = item.getStats();
		final WorkerStats priorStats = stats;
		if (workers.isEmpty()) {
			stats = tempStats;
		} else if (!Objects.equals(tempStats, priorStats)) {
			stats = null;
		}
		workers.add(item);
		final IWorker[] workerArray = workers.toArray(new IWorker[workers.size()]);
		final Collection<IJob> proxyJobsTemp = new ArrayList<>(proxyJobs);
		for (final IJob job : item) {
			final String name = job.getName();
			if (jobNames.contains(name)) {
				proxyJobs.stream().filter(proxyJob -> proxyJob.getName().equals(name))
						.forEach(proxyJob -> {
							//noinspection unchecked
							((ProxyFor<IJob>) proxyJob).addProxied(job);
							proxyJobsTemp.remove(proxyJob);
						});
			} else {
				jobNames.add(name);
				//noinspection ObjectAllocationInLoop
				proxyJobs.add(new ProxyJob(name, parallel, workerArray));
			}
			jobNames.add(job.getName());
		}
		for (final IJob proxyJob : proxyJobs) {
			// FIXME: This can't be right!
			final String name = proxyJob.getName();
			//noinspection ObjectAllocationInLoop
			final IJob job = new Job(name, 0);
			//noinspection unchecked
			((ProxyFor<IJob>) proxyJob).addProxied(job);
		}
	}

	/**
	 * An iterable view of the proxied workers.
	 * @return the proxied Workers.
	 */
	@Override
	public Iterable<IWorker> getProxied() {
		return new ArrayList<>(workers);
	}

	/**
	 * The default icon filename for the proxied workers.
	 * @return the name of an image to represent the worker
	 */
	@Override
	public String getDefaultImage() {
		@Nullable
		String retval = null;
		for (final IWorker worker : workers) {
			if (retval == null) {
				retval = worker.getDefaultImage();
			} else if (!retval.equals(worker.getDefaultImage())) {
				return "worker.png";
			}
		}
		if (retval == null) {
			return "worker.png";
		} else {
			return retval;
		}
	}

	/**
	 * The per-instance icon filename shared by the proxied workers.
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
	 * Set the per-instance icon filename.
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
	 * Get the race (aka kind) of the workers. If not all are the same, "proxied".
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
	 * Returns the name of the workers, or "proxied" if they don't all share a name.
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
	 * Delegates to getKind().
	 * @return the race of the proxied workers
	 */
	@Override
	public String getRace() {
		return getKind();
	}

	/**
	 * Get a Job by name.
	 * @param jobName the name of a Job
	 * @return the Job by that name the worker has, or null if it has none
	 */
	@Nullable
	@Override
	public IJob getJob(final String jobName) {
		for (final IJob job : proxyJobs) {
			if (jobName.equals(job.getName())) {
				return job;
			}
		}
		final IJob retval = new ProxyJob(jobName, parallel, workers.toArray(
				new IWorker[workers.size()]));
		jobNames.add(jobName);
		proxyJobs.add(retval);
		return retval;
	}

	/**
	 * A trivial toString().
	 * @return a string representation of this class
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "ProxyWorker";
	}

	/**
	 * Whether this should be considered (if true) a proxy for multiple representations
	 * of the same Worker, e.g. in different maps, or (if false) a proxy for different
	 * related Workers.
	 * @return whether this is a "parallel" proxy.
	 */
	@Override
	public boolean isParallel() {
		return parallel;
	}

	/**
	 * The stats of the proxied workers.
	 * @return the stats of the proxied workers, or null if either no workers are being
	 * proxied or their stats are not all identical.
	 */
	@Nullable
	@Override
	public WorkerStats getStats() {
		return stats;
	}
}

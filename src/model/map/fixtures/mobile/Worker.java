package model.map.fixtures.mobile;

import java.util.Arrays;
import java.util.Collection;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import model.map.HasPortrait;
import model.map.IFixture;
import model.map.fixtures.mobile.worker.IJob;
import model.map.fixtures.mobile.worker.Job;
import model.map.fixtures.mobile.worker.WorkerStats;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import util.ArraySet;
import util.ListMaker;

/**
 * A worker (or soldier) in a unit. This is deliberately not a TileFixture: these should
 * only be part of a unit, not as a top-level tag.
 *
 * And TODO: some of the other MobileFixtures should be similarly converted.
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
public class Worker implements IWorker, HasPortrait {
	/**
	 * The set of jobs the worker is trained or experienced in.
	 */
	private final Collection<IJob> jobSet = new ArraySet<>();
	/**
	 * The ID number of the worker.
	 */
	private final int id;
	/**
	 * The worker's name.
	 */
	private final String name;
	/**
	 * The worker's race (elf, dwarf, human, etc.).
	 */
	private final String race;
	/**
	 * The worker's stats.
	 */
	@Nullable
	private WorkerStats stats = null;
	/**
	 * The name of an image to use for this particular fixture.
	 */
	private String image = "";
	/**
	 * The filename of an image to use as a portrait for the unit.
	 */
	private String portraitName = "";

	/**
	 * Constructor.
	 *
	 * @param wName      the worker's name
	 * @param workerRace the worker's race
	 * @param idNum      the ID number of the worker
	 * @param jobs       the Jobs the worker is trained in
	 */
	public Worker(final String wName, final String workerRace, final int idNum,
				  final @NonNull IJob @NonNull ... jobs) {
		name = wName;
		id = idNum;
		race = workerRace;
		jobSet.addAll(Arrays.asList(jobs));
	}

	/**
	 * Whether two Sets of Jobs are equal, neither containing a nonempty Job that the
	 * other does not.
	 * @param firstSet  a set of Jobs
	 * @param secondSet a set of Jobs
	 * @return whether they are equal, ignoring any "empty" Jobs.
	 */
	private static boolean areJobSetsEqual(final Collection<IJob> firstSet,
										   final Collection<IJob> secondSet) {
		final Predicate<IJob> nonempty = job -> !job.isEmpty();
		return firstSet.stream().filter(nonempty).collect(Collectors.toSet())
					   .equals(secondSet.stream().filter(nonempty)
									   .collect(Collectors.toSet()));
	}

	/**
	 * Add a job.
	 *
	 * @param job the job to add.
	 * @return the result of the operation
	 */
	@Override
	public boolean addJob(final IJob job) {
		return jobSet.add(job);
	}

	/**
	 * An iterator over the worker's Jobs.
	 * @return An iterator over the worker's jobs.
	 */
	@Override
	public final Iterator<IJob> iterator() {
		return jobSet.iterator();
	}

	/**
	 * The worker's ID number.
	 * @return the ID number of the worker.
	 */
	@Override
	public int getID() {
		return id;
	}

	/**
	 * The worker's race.
	 * @return the worker's race (elf, human, or whatever)
	 */
	@Override
	public String getRace() {
		return race;
	}

	/**
	 * The worker's name.
	 * @return the worker's name
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * An object is equal iff it is a worker that is equal if we ignore ID and also has
	 * the same ID as this one.
	 * @param obj an object
	 * @return whether it's the same as this
	 */
	@Override
	public boolean equals(@Nullable final Object obj) {
		return (this == obj) ||
					   ((obj instanceof IWorker) && (((IWorker) obj).getID() == id)
								&& equalsIgIDImpl((IWorker) obj));
	}

	/**
	 * Use the ID for hashing.
	 * @return a hash code for the object
	 */
	@Override
	public int hashCode() {
		return id;
	}

	/**
	 * A simple toString(). TODO: omit race if human?
	 * @return a String representation of the Worker.
	 */
	@Override
	public String toString() {
		return name + ", a " + race;
	}

	/**
	 * This delegates to a Worker-specific helper method.
	 *
	 * TODO: Remove obsolete warning-suppression: we don't cast to a concrete class.
	 * @param fix a fixture
	 * @return whether it equals this one except its ID.
	 */
	@SuppressWarnings("ObjectEquality")
	@Override
	public boolean equalsIgnoringID(final IFixture fix) {
		return (this == fix) || ((fix instanceof IWorker) && equalsIgIDImpl((IWorker)
																					fix));
	}

	/**
	 * A worker is equal iff it has the same name, race, and stats (if any), and neither
	 * has a non-empty Job the other does not.
	 * @param fix a worker
	 * @return whether it equals this one except for ID.
	 */
	private boolean equalsIgIDImpl(final IWorker fix) {
		return fix.getName().equals(name) &&
					   areJobSetsEqual(jobSet, ListMaker.toList(fix)) &&
					   fix.getRace().equals(race) &&
					   Objects.equals(stats, fix.getStats());
	}

	/**
	 * A fixture is a subset if it is a worker with the same ID, name, race, and stats,
	 * and no Jobs we don't have, and its Jobs are subsets of our corresponding Jobs.
	 * @param obj     another UnitMember
	 * @param ostream a stream to report an explanation on
	 * @param context a string to print before every line of output, describing the
	 *                context
	 * @return whether that member equals this one
	 */
	@SuppressWarnings("CastToConcreteClass")
	@Override
	public boolean isSubset(final IFixture obj, final Formatter ostream,
							final String context) {
		if (obj.getID() == id) {
			if (obj instanceof Worker) {
				if (areObjectsEqual(ostream, name, ((Worker) obj).name,
						"%s In worker %s (ID #%d):\tNames differ%n", context, name,
						Integer.valueOf(id)) &&
							areObjectsEqual(ostream, race, ((Worker) obj).race,
									"%s In worker %s (ID #%d):\tRaces differ%n",
									context, name, Integer.valueOf(id)) &&
							areObjectsEqual(ostream, stats, ((Worker) obj).stats,
									"%s In worker %s (ID #%d):\tStats differ%n",
									context, name, Integer.valueOf(id))) {
					final Map<String, IJob> ours = new HashMap<>();
					for (final IJob job : jobSet) {
						ours.put(job.getName(), job);
					}
					boolean retval = true;
					for (final IJob job : ((Worker) obj).jobSet) {
						if (!ours.containsKey(job.getName())) {
							ostream.format("%s In worker %s (ID #%d):\tExtra Job: %s%n",
									context, name, Integer.valueOf(id), job.getName());
							retval = false;
						} else if (!ours.get(job.getName()).isSubset(job, ostream,
								String.format("%s In worker %s (ID #%d):", context, name,
										Integer.valueOf(id)))) {
							retval = false;
						}
					}
					return retval;
				} else {
					return false;
				}
			} else {
				ostream.format("%sFor ID #%d, different kinds of members%n", context,
						Integer.valueOf(id));
				return false;
			}
		} else {
			ostream.format("%sCalled with different IDs, #%d and #%d%n", context,
					Integer.valueOf(id), Integer.valueOf(obj.getID()));
			return false;
		}
	}

	/**
	 * The worker's race.
	 * @return the worker's "kind" (i.e. race, i.e elf, dwarf, human, etc.)
	 */
	@Override
	public String getKind() {
		return race;
	}

	/**
	 * The worker's stats.
	 * @return the worker's stats
	 */
	@Override
	@Nullable
	public WorkerStats getStats() {
		return stats;
	}

	/**
	 * Set the worker's stats.
	 * @param newStats the worker's new stats
	 */
	public void setStats(final WorkerStats newStats) {
		stats = newStats;
	}

	/**
	 * This is just for icons in lists and such, not for the map, since this isn't a
	 * TileFixture.
	 *
	 * @return the filename of the image representing a worker.
	 */
	@Override
	public String getDefaultImage() {
		return "worker.png";
	}

	/**
	 * The per-instance icon filename.
	 * @return the name of an image to use for this particular fixture.
	 */
	@Override
	public String getImage() {
		return image;
	}

	/**
	 * Set the per-instance icon filename.
	 * @param img the name of an image to use for this particular fixture
	 */
	@Override
	public void setImage(final String img) {
		image = img;
	}

	/**
	 * Clone the object.
	 * @param zero whether to "zero out" the worker
	 * @return a copy of this worker
	 */
	@SuppressWarnings("MethodReturnOfConcreteClass")
	@Override
	public Worker copy(final boolean zero) {
		if (zero) {
			final Worker retval = new Worker(name, race, id);
			retval.image = image;
			return retval;
		} else {
			final Worker retval = new Worker(name, race, id);
			final WorkerStats localStats = stats;
			if (localStats != null) {
				retval.stats = localStats.copy();
			}
			retval.image = image;
			for (final IJob job : this) {
				if (!job.isEmpty()) {
					retval.addJob(job.copy());
				}
			}
			return retval;
		}
	}

	/**
	 * Get a Job by name.
	 * @param jobName the name of a Job
	 * @return the Job by that name the worker has, or null if it has none
	 */
	@Nullable
	@Override
	public IJob getJob(final String jobName) {
		for (final IJob job : this) {
			if (jobName.equals(job.getName())) {
				return job;
			}
		}
		final IJob retval = new Job(jobName, 0);
		jobSet.add(retval);
		return retval;
	}

	/**
	 * The filename of the worker's portrait, if any.
	 * @return The filename of an image to use as a portrait for the unit.
	 */
	@Override
	public String getPortrait() {
		return portraitName;
	}

	/**
	 * Set a portrait filename.
	 * @param portrait The filename of an image to use as a portrait for the unit.
	 */
	@Override
	public void setPortrait(final String portrait) {
		portraitName = portrait;
	}
}

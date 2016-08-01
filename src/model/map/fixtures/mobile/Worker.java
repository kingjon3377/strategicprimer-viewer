package model.map.fixtures.mobile;

import controller.map.misc.CLIHelper;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import model.map.HasPortrait;
import model.map.IFixture;
import model.map.fixtures.mobile.worker.IJob;
import model.map.fixtures.mobile.worker.WorkerStats;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import util.ArraySet;
import util.LineEnd;
import util.NullCleaner;

/**
 * A worker (or soldier) in a unit. This is deliberately not a TileFixture: these should
 * only be part of a unit, not as a top-level tag.
 *
 * And TODO: some of the other MobileFixtures should be similarly converted.
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
public class Worker implements IWorker, HasPortrait {
	/**
	 * The worker's name.
	 */
	private String name;

	/**
	 * The worker's race (elf, dwarf, human, etc.).
	 */
	private String race;
	/**
	 * The set of jobs the worker is trained or experienced in.
	 */
	private final Collection<IJob> jobSet = new ArraySet<>();

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
	 * @return An iterator over the worker's jobs.
	 */
	@Override
	public final Iterator<IJob> iterator() {
		return NullCleaner.assertNotNull(jobSet.iterator());
	}

	/**
	 * The ID number of the worker.
	 */
	private final int id;

	/**
	 * @return the ID number of the worker.
	 */
	@Override
	public int getID() {
		return id;
	}

	/**
	 * @return the worker's race (elf, human, or whatever)
	 */
	@Override
	public String getRace() {
		return race;
	}

	/**
	 * @return the worker's name
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * @param obj an object
	 * @return whether it's the same as this
	 */
	@Override
	public boolean equals(@Nullable final Object obj) {
		return (this == obj) || ((obj instanceof IWorker) && (((IWorker) obj).getID() == id)
										&& equalsIgIDImpl((IWorker) obj));
	}

	/**
	 * @return a hash code for the object
	 */
	@Override
	public int hashCode() {
		return id;
	}

	/**
	 * @return a String representation of the Worker.
	 */
	@Override
	public String toString() {
		return name + ", a " + race;
	}

	/**
	 * @param fix a fixture
	 * @return whether it equals this one except its ID.
	 */
	@SuppressWarnings({"ObjectEquality", "CastToConcreteClass"})
	@Override
	public boolean equalsIgnoringID(final IFixture fix) {
		return (this == fix) || ((fix instanceof IWorker) && equalsIgIDImpl((IWorker)
																				fix));
	}

	/**
	 * @param fix a worker
	 * @return whether it equals this one except for ID.
	 */
	private boolean equalsIgIDImpl(final IWorker fix) {
		return fix.getName().equals(name) &&
					   areJobSetsEqual(jobSet, CLIHelper.toList(fix)) &&
					   fix.getRace().equals(race) &&
					   Objects.equals(stats, fix.getStats());
	}

	/**
	 * TODO: Improve performance.
	 *
	 * @param firstSet  a set of Jobs
	 * @param secondSet a set of Jobs
	 * @return whether they are equal, ignoring any "empty" Jobs.
	 */
	private static boolean areJobSetsEqual(final Collection<IJob> firstSet, final
	Collection<IJob> secondSet) {
		for (final IJob job : firstSet) {
			if (!job.isEmpty() && !secondSet.contains(job)) {
				return false;
			}
		}
		for (final IJob job : secondSet) {
			if (!job.isEmpty() && !firstSet.contains(job)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * @param obj     another UnitMember
	 * @param ostream a stream to report an explanation on
	 * @param context a string to print before every line of output, describing the
	 *                context
	 * @return whether that member equals this one
	 * @throws IOException on I/O error writing output to the stream
	 */
	@SuppressWarnings("CastToConcreteClass")
	@Override
	public boolean isSubset(final IFixture obj, final Appendable ostream,
							final String context) throws IOException {
		if (obj.getID() == id) {
			if (obj instanceof Worker) {
				final String localContext =
						context + " In worker " + ((Worker) obj).name
								+ " (ID #" + Integer.toString(id) + "):";
				if (areObjectsEqual(ostream, name, ((Worker) obj).name, localContext,
						"\tNames differ", LineEnd.LINE_SEP) &&
						areObjectsEqual(ostream, race, ((Worker) obj).race, localContext,
								":\tRaces differ", LineEnd.LINE_SEP) &&
						areObjectsEqual(ostream, stats, ((Worker) obj).stats, localContext,
								":\tStats differ", LineEnd.LINE_SEP)) {
					final Map<String, IJob> ours = new HashMap<>();
					for (final IJob job : jobSet) {
						ours.put(job.getName(), job);
					}
					boolean retval = true;
					for (final IJob job : ((Worker) obj).jobSet) {
						if (!ours.containsKey(job.getName())) {
							ostream.append(localContext);
							ostream.append("\tExtra Job: ");
							ostream.append(job.getName());
							ostream.append(LineEnd.LINE_SEP);
							retval = false;
						} else if (!NullCleaner.assertNotNull(ours.get(job.getName())).isSubset(job,
								ostream, localContext)) {
							retval = false;
						}
					}
					return retval;
				} else {
					return false;
				}
			} else {
				ostream.append("For ID #");
				ostream.append(Integer.toString(id));
				ostream.append(", different kinds of members");
				return false;
			}
		} else {
			ostream.append("Called with different IDs, #");
			ostream.append(Integer.toString(id));
			ostream.append(" and #");
			ostream.append(Integer.toString(obj.getID()));
			ostream.append(LineEnd.LINE_SEP);
			return false;
		}
	}

	/**
	 * @return the worker's "kind" (i.e. race, i.e elf, dwarf, human, etc.)
	 */
	@Override
	public String getKind() {
		return race;
	}

	/**
	 * @param newName the worker's new name
	 */
	@Override
	public final void setName(final String newName) {
		name = newName;
	}

	/**
	 * @param nKind the worker's new race
	 */
	@Override
	public void setKind(final String nKind) {
		race = nKind;
	}

	/**
	 * @return the worker's stats
	 */
	@Override
	@Nullable
	public WorkerStats getStats() {
		return stats;
	}

	/**
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
	 * @param img the name of an image to use for this particular fixture
	 */
	@Override
	public void setImage(final String img) {
		image = img;
	}

	/**
	 * @return the name of an image to use for this particular fixture.
	 */
	@Override
	public String getImage() {
		return image;
	}

	/**
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
					retval.addJob(job.copy(false));
				}
			}
			return retval;
		}
	}

	/**
	 * TODO: Should we add and return a new Job instead of null if not present?
	 *
	 * @param jobName the name of a Job
	 * @return the Job by that name the worker has, or null if it has none
	 */
	@SuppressWarnings("ReturnOfNull")
	@Nullable
	@Override
	public IJob getJob(final String jobName) {
		for (final IJob job : this) {
			if (jobName.equals(job.getName())) {
				return job;
			}
		}
		return null;
	}
	/**
	 * The filename of an image to use as a portrait for the unit.
	 */
	private String portraitName = "";
	/**
	 * @return The filename of an image to use as a portrait for the unit.
	 */
	@Override
	public String getPortrait() {
		return portraitName;
	}

	/**
	 * @param portrait The filename of an image to use as a portrait for the unit.
	 */
	@Override
	public void setPortrait(final String portrait) {
		portraitName = portrait;
	}
}

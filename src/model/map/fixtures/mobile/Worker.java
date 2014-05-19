package model.map.fixtures.mobile;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import model.map.HasImage;
import model.map.HasKind;
import model.map.HasName;
import model.map.IFixture;
import model.map.fixtures.UnitMember;
import model.map.fixtures.mobile.worker.IJob;
import model.map.fixtures.mobile.worker.Job;
import model.map.fixtures.mobile.worker.WorkerStats;

import org.eclipse.jdt.annotation.Nullable;

import util.ArraySet;
import util.NullCleaner;

/**
 * A worker (or soldier) in a unit. This is deliberately not a TileFixture:
 * these should only be part of a unit, not as a top-level tag.
 *
 * And TODO: some of the other MobileFixtures should be similarly converted.
 *
 * @author Jonathan Lovelace
 *
 */
public class Worker implements HasName, HasKind, HasImage, IWorker {
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
	private final Set<IJob> jobSet = new ArraySet<>();

	/**
	 * The worker's stats.
	 */
	@Nullable
	private WorkerStats stats;

	/**
	 * The name of an image to use for this particular fixture.
	 */
	private String image = "";

	/**
	 * Constructor.
	 *
	 * @param wName the worker's name
	 * @param workerRace the worker's race
	 * @param idNum the ID number of the worker
	 * @param jobs the Jobs the worker is trained in
	 */
	public Worker(final String wName, final String workerRace, final int idNum,
			final Job... jobs) {
		super();
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
	private final int id; // NOPMD

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
		return this == obj || obj instanceof Worker && ((Worker) obj).id == id
				&& equalsIgIDImpl((Worker) obj);
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
	@Override
	public boolean equalsIgnoringID(final IFixture fix) {
		return this == fix || fix instanceof Worker
				&& equalsIgIDImpl((Worker) fix);
	}
	/**
	 * @param fix a worker
	 * @return whether it equals this one except for ID.
	 */
	private boolean equalsIgIDImpl(final Worker fix) {
		final WorkerStats locStats = stats;
		if (locStats == null) {
			return fix.name.equals(name) && fix.jobSet.equals(jobSet) // NOPMD
					&& fix.race.equals(race) && fix.stats == null;
		} else {
			return fix.name.equals(name) && fix.jobSet.equals(jobSet)
					&& fix.race.equals(race) && locStats.equals(fix.stats);
		}
	}

	/**
	 * @param obj another UnitMember
	 * @param ostream a stream to report an explanation on
	 * @return whether that member equals this one
	 */
	@Override
	public boolean isSubset(final UnitMember obj, final PrintWriter ostream) {
		if (obj.getID() == id) {
			if (obj instanceof Worker) {
				if (!name.equals(((Worker) obj).name)) {
					ostream.print("For worker with ID #");
					ostream.print(id);
					ostream.println(", names differ");
					return false;
				} else if (!race.equals(((Worker) obj).race)) {
					ostream.print("For worker ");
					ostream.print(name);
					ostream.print(", ID #");
					ostream.print(id);
					ostream.println(", races differ");
					return false;
				} else if (!Objects.equals(stats, ((Worker) obj).stats)) {
					ostream.print("For worker ");
					ostream.print(name);
					ostream.print(", ID #");
					ostream.print(id);
					ostream.println(", stats differ");
					return false;
				} else {
					boolean retval = true;
					final Map<String, IJob> ours = new HashMap<>();
					for (final IJob job : jobSet) {
						ours.put(job.getName(), job);
					}
					for (final IJob job : ((Worker) obj).jobSet) {
						if (job == null) {
							continue;
						} else if (!ours.containsKey(job.getName())) {
							ostream.print("In worker ");
							ostream.print(name);
							ostream.print(" (#");
							ostream.print(id);
							ostream.print("): Extra Job: ");
							ostream.println(job.getName());
							retval = false;
						} else if (!ours.get(job.getName()).isSubset(job, ostream)) {
							ostream.print(" (in worker ");
							ostream.print(name);
							ostream.print(", ID #");
							ostream.print(id);
							ostream.println(')');
							retval = false;
						}
					}
					return retval;
				}
			} else {
				ostream.print("For ID #");
				ostream.print(id);
				ostream.print(", different kinds of members");
				return false;
			}
		} else {
			ostream.print("Called with different IDs, #");
			ostream.print(id);
			ostream.print(" and #");
			ostream.println(obj.getID());
			return false;
		}
	}
	/**
	 * @return the worker's "kind" (i.e. race, i.e elf, dwarf, human, etc.)
	 */
	@Override
	public String getKind() {
		return getRace();
	}

	/**
	 * @param nomen the worker's new name
	 */
	@Override
	public final void setName(final String nomen) {
		name = nomen;
	}

	/**
	 * @param kind the worker's new race
	 */
	@Override
	public void setKind(final String kind) {
		race = kind;
	}

	/**
	 * @return the worker's stats
	 */
	@Nullable
	public WorkerStats getStats() {
		return stats;
	}

	/**
	 * @param wstats the worker's new stats
	 */
	public void setStats(final WorkerStats wstats) {
		stats = wstats;
	}

	/**
	 * This is just for icons in lists and such, not for the map, since this
	 * isn't a TileFixture.
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
}

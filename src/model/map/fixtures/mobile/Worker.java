package model.map.fixtures.mobile;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;

import model.map.HasImage;
import model.map.HasKind;
import model.map.HasName;
import model.map.IFixture;
import model.map.fixtures.UnitMember;
import model.map.fixtures.mobile.worker.Job;
import model.map.fixtures.mobile.worker.WorkerStats;

import org.eclipse.jdt.annotation.Nullable;

import util.ArraySet;

/**
 * A worker (or soldier) in a unit. This is deliberately not a TileFixture:
 * these should only be part of a unit, not as a top-level tag.
 *
 * And TODO: some of the other MobileFixtures should be similarly converted.
 *
 * @author Jonathan Lovelace
 *
 */
public class Worker implements UnitMember, Iterable<Job>, HasName, HasKind,
		HasImage {
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
	 * The worker's race (elf, dwarf, human, etc.).
	 */
	private String race;
	/**
	 * The set of jobs the worker is trained or experienced in.
	 */
	private final Set<Job> jobSet = new ArraySet<>();

	/**
	 * Add a job.
	 *
	 * @param job the job to add.
	 * @return the result of the operation
	 */
	public boolean addJob(final Job job) {
		return jobSet.add(job);
	}

	/**
	 * @return An iterator over the worker's jobs.
	 */
	@Override
	public Iterator<Job> iterator() {
		final Iterator<Job> iter = jobSet.iterator();
		assert iter != null;
		return iter;
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
	 * The worker's name.
	 */
	private String name;

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
		return this == obj
				|| (obj instanceof Worker && ((Worker) obj).name.equals(name)
						&& ((Worker) obj).id == id && ((Worker) obj).jobSet
							.equals(jobSet))
				&& ((Worker) obj).race.equals(race)
				&& (stats != null ? stats.equals(((Worker) obj).stats)
						: ((Worker) obj).stats == null);
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
		return this == fix
				|| (fix instanceof Worker && equalsIgIDImpl((Worker) fix));
	}
	/**
	 * @param fix a worker
	 * @return whether it equals this one except for ID.
	 */
	private boolean equalsIgIDImpl(final Worker fix) {
		return (fix.name.equals(name) && fix.jobSet.equals(jobSet))
				&& fix.race.equals(race)
				&& (stats != null ? stats.equals(fix.stats) : fix.stats == null);
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
	 * The worker's stats.
	 */
	@Nullable
	private WorkerStats stats;

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
	 * The name of an image to use for this particular fixture.
	 */
	private String image = "";

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

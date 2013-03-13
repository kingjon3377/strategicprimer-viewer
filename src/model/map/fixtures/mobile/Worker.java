package model.map.fixtures.mobile;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;

import model.map.HasKind;
import model.map.HasName;
import model.map.IFixture;
import model.map.fixtures.UnitMember;
import model.map.fixtures.mobile.worker.Job;
import util.ArraySet;

/**
 * A worker (or soldier) in a unit. This is deliberately not a TileFixture:
 * these should only be part of a unit, not as a top-level tag. (And TODO: some
 * of the other MobileFixtures should be similarly converted.)
 *
 * TODO: Add Jobs, skills, etc.
 * @author Jonathan Lovelace
 *
 */
public class Worker implements UnitMember, Iterable<Job>, HasName, HasKind {
	/**
	 * Version UID for serialization.
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * Constructor.
	 * @param wName the worker's name
	 * @param workerRace the worker's race
	 * @param idNum the ID number of the worker
	 * @param jobs the Jobs the worker is trained in
	 */
	public Worker(final String wName, final String workerRace, final int idNum, final Job... jobs) {
		super();
		name = wName;
		id = idNum;
		race = workerRace;
		jobSet.addAll(Arrays.asList(jobs));
	}
	/**
	 * The worker's race (elf, dwarf, human, etc.).
	 */
	private final String race;
	/**
	 * The set of jobs the worker is trained or experienced in.
	 */
	private final Set<Job> jobSet = new ArraySet<Job>();
	/**
	 * Add a job.
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
		return jobSet.iterator();
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
	private final String name;
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
	public boolean equals(final Object obj) {
		return this == obj
				|| (obj instanceof Worker && ((Worker) obj).name.equals(name)
						&& ((Worker) obj).id == id && ((Worker) obj).jobSet
							.equals(jobSet))
				&& ((Worker) obj).race.equals(race);
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
				|| (fix instanceof Worker && ((Worker) fix).name.equals(name) && ((Worker) fix).jobSet
						.equals(jobSet)) && ((Worker) fix).race.equals(race);
	}
	/**
	 * @return the worker's "kind" (i.e. race, i.e elf, dwarf, human, etc.)
	 */
	@Override
	public String getKind() {
		return getRace();
	}
}

package model.character;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import view.util.IsAdmin;

/**
 * A worker or warrior.
 * 
 * @author Jonathan Lovelace
 */
public class SPCharacter implements Serializable {
	/**
	 * Version UID for serialization.
	 */
	private static final long serialVersionUID = -1906146834037495060L;
	/**
	 * The character's name.
	 */
	private String name;

	/**
	 * @return the character's name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param _name
	 *            the character's name
	 */
	public void setName(final String _name) {
		name = _name;
	}

	/**
	 * Constructor.
	 * 
	 * @param _name
	 *            the character's name
	 */
	public SPCharacter(final String _name) {
		name = _name;
		if (IsAdmin.IS_ADMIN) {
			stats = new AdminStatsImpl();
		} else {
			stats = new PlayerStatsImpl();
		}
	}

	/**
	 * Constructor taking a "stats" value. For use in the admin side when all we
	 * have is the summary.
	 * 
	 * @param _name
	 *            the character's name
	 * @param _stats
	 *            the character's stats
	 */
	public SPCharacter(final String _name, final CharStats _stats) {
		name = _name;
		stats = _stats;
	}

	/**
	 * The character's statistics.
	 */
	private CharStats stats;

	/**
	 * @return the character's statistics
	 */
	public CharStats getStats() {
		return stats;
	}

	/**
	 * The character's levels in Jobs, if any
	 */
	private final List<JobLevels> jobs = new ArrayList<JobLevels>();

	/**
	 * @return the character's levels in Jobs, if any
	 */
	public List<JobLevels> getJobs() {
		return new ArrayList<JobLevels>(jobs);
	}

	/**
	 * Add a new job.
	 * 
	 * @param job
	 *            the job to add
	 */
	public void addJob(final JobLevels job) {
		jobs.add(job);
	}

	/**
	 * Remove a job.
	 * 
	 * @param job
	 *            the job to remove
	 */
	public void removeJob(final JobLevels job) {
		jobs.remove(job);
	}
}

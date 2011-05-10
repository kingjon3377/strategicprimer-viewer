package model.character;

import java.io.Serializable;

/**
 * A class to represent having (or trying to acquire) levels in a Job.
 * 
 * @author Jonathan Lovelace
 * 
 */
public class JobLevels implements Serializable {
	/**
	 * Version UID for serialization
	 */
	private static final long serialVersionUID = -7717128400521917978L;

	/**
	 * No-arg constructor. Has zero levels in Laborer.
	 */
	public JobLevels() {
		this(JobType.Laborer, 0);
	}

	/**
	 * Constructor. Has zero levels in the specified Job.
	 * 
	 * @param job
	 *            the job type
	 */
	public JobLevels(final JobType job) {
		this(job, 0);
	}

	/**
	 * Constructor.
	 * 
	 * @param job
	 *            the job type
	 * @param lvls
	 *            how many levels in that job
	 */
	public JobLevels(final JobType job, final int lvls) {
		if (lvls < 0) {
			throw new IllegalArgumentException(
					"Can't have a negative number of levels");
		}
		jobType = job;
		levels = lvls;
	}

	/**
	 * The job type.
	 */
	private JobType jobType;
	/**
	 * How many levels in it.
	 */
	private int levels;

	/**
	 * @return the job type
	 */
	public JobType getJob() {
		return jobType;
	}

	/**
	 * @param job
	 *            the job type
	 */
	public void setJob(final JobType job) {
		jobType = job;
	}

	/**
	 * @return how many levels in the job
	 */
	public int getLevels() {
		return levels;
	}

	/**
	 * @param lvls
	 *            how many levels in the job
	 */
	public void setLevels(final int lvls) {
		if (lvls < 0) {
			throw new IllegalArgumentException(
					"Can't have a negative number of levels");
		}
		levels = lvls;
	}

	/**
	 * @param obj
	 *            another object
	 * @return whether it's the same as this one.
	 */
	@Override
	public boolean equals(final Object obj) {
		return obj instanceof JobLevels
				&& ((JobLevels) obj).getJob().equals(jobType)
				&& ((JobLevels) obj).getLevels() == levels;
	}

	/**
	 * @return a hash-code for the object
	 */
	@Override
	public int hashCode() {
		return jobType.hashCode() + levels * JobType.values().length;
	}
}

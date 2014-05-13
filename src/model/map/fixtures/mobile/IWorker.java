package model.map.fixtures.mobile;

import model.map.fixtures.UnitMember;
import model.map.fixtures.mobile.worker.IJob;
/**
 * An interface for Workers.
 * @author Jonathan Lovelace
 *
 */
public interface IWorker extends UnitMember, Iterable<IJob> {
	/**
	 * Add a job.
	 *
	 * @param job the job to add.
	 * @return the result of the operation
	 */
	boolean addJob(IJob job);
}
package model.map.fixtures.mobile;

import model.map.HasImage;
import model.map.HasKind;
import model.map.HasName;
import model.map.fixtures.UnitMember;
import model.map.fixtures.mobile.worker.IJob;
/**
 * An interface for Workers.
 * @author Jonathan Lovelace
 *
 */
public interface IWorker extends UnitMember, Iterable<IJob>, HasName, HasKind,
		HasImage {
	/**
	 * Add a job.
	 *
	 * @param job the job to add.
	 * @return the result of the operation
	 */
	boolean addJob(IJob job);
	/**
	 * @return the worker's race
	 */
	String getRace();
	/**
	 * Specialization of method from IFixture.
	 * @return a copy of this worker
	 * @param zero whether to "zero out" sensitive information
	 */
	@Override
	IWorker copy(boolean zero);
}
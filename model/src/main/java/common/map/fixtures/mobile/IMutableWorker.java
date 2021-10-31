package common.map.fixtures.mobile;

import common.map.fixtures.mobile.worker.IJob;

import common.map.HasMutableImage;
import common.map.HasMutablePortrait;

/**
 * Mutator operations for {@link IWorker}.
 */
public interface IMutableWorker extends IWorker, HasMutableImage, HasMutablePortrait {
	/**
	 * Add a Job. Returns whether the number of Jobs changed as a result of
	 * this.
	 *
	 * Note that this does not guarantee that the worker will contain this
	 * Job object, nor that any changes made to it will be applied to the
	 * Job that the worker already had or that is actually added. If levels
	 * *need* to be added, callers should geth the Job the worker contains
	 * after this returns using {@link getJob} and apply changes to that.
	 *
	 * TODO: Make sure that pre-applied experience is applied if the worker
	 * already had a Job by this name
	 *
	 * TODO: Make void instead of boolean?
	 */
	boolean addJob(IJob job);
}

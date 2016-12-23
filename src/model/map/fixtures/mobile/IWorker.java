package model.map.fixtures.mobile;

import model.map.HasKind;
import model.map.HasMutableImage;
import model.map.HasMutableName;
import model.map.fixtures.UnitMember;
import model.map.fixtures.mobile.worker.IJob;
import model.map.fixtures.mobile.worker.WorkerStats;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * An interface for Workers.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2014-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public interface IWorker
		extends UnitMember, Iterable<@NonNull IJob>, HasMutableName, HasKind,
						HasMutableImage {
	/**
	 * Add a job.
	 *
	 * Note that this does not guarantee that the worker will contain this Job object,
	 * nor that any changes made to it will be applied to the Job that the worker
	 * already had or that is actually added. (TODO: implementations *should* do that.)
	 * If levels *need* to be added, callers should get the Job the worker contains
	 * after this returns using {@link #getJob(String)} and apply changes to that.
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
	 * @return the worker's stats
	 */
	@Nullable WorkerStats getStats();

	/**
	 * Specialization of method from IFixture.
	 *
	 * @param zero whether to "zero out" sensitive information
	 * @return a copy of this worker
	 */
	@Override
	IWorker copy(boolean zero);

	/**
	 * @param jobName the name of a Job
	 * @return the Job by that name the worker has, or null if it has none
	 */
	@Nullable IJob getJob(String jobName);
}

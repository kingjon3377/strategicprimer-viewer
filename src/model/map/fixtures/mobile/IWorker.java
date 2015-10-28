package model.map.fixtures.mobile;

import org.eclipse.jdt.annotation.Nullable;

import model.map.HasImage;
import model.map.HasKind;
import model.map.HasName;
import model.map.fixtures.UnitMember;
import model.map.fixtures.mobile.worker.IJob;
/**
 * An interface for Workers.
 *
 * This is part of the Strategic Primer assistive programs suite developed by
 * Jonathan Lovelace.
 *
 * Copyright (C) 2014-2015 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of version 3 of the GNU General Public License as published by the
 * Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * @author Jonathan Lovelace
 *
 */
public interface IWorker extends UnitMember, Iterable<IJob>, HasName, HasKind,
		HasImage {
	/**
	 * Add a job.
	 *
	 * Note that this does not guarantee that the worker will contain this Job
	 * object, nor that any changes made to it will be applied to the Job that
	 * the worker already had or that is actually added. (TODO: implementations
	 * *should* do that.) If levels *need* to be added, callers should get the
	 * Job the worker contains after this returns (TODO: make that easier) and
	 * apply changes to that.
	 *
	 * @param job
	 *            the job to add.
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
	/**
	 * TODO: Should we create and return a new Job instead of null if not present?
	 *
	 * @param name the name of a Job
	 * @return the Job by that name the worker has, or null if it has none
	 */
	@Nullable
	IJob getJob(String name);
}
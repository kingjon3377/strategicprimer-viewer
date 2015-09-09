package model.map.fixtures.mobile.worker;

import model.map.HasName;
import model.map.Subsettable;
/**
 * An interface for Jobs.
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
public interface IJob extends HasName, Iterable<ISkill>, Subsettable<IJob> {

	/**
	 * Add a skill.
	 *
	 * @param skill the skill to add
	 * @return the result of the operation
	 */
	boolean addSkill(ISkill skill);

	/**
	 * @return the worker's level in the job
	 */
	int getLevel();
	/**
	 * @return a copy of this Job
	 * @param zero whether to "zero out" sensitive details
	 */
	IJob copy(boolean zero);
	/**
	 * A Job is "empty" if the worker has no levels in it and no experience in any skills it contains.
	 * @return whether this Job is "empty"
	 */
	boolean isEmpty();
}
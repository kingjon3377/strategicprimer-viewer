package model.map.fixtures.mobile.worker;

import model.map.HasName;

/**
 * An interface for Skills.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2014-2015 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program. If not, see
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public interface ISkill extends HasName {
	/**
	 * @return how many levels the worker has in the skill
	 */
	int getLevel();

	/**
	 * @return how many hours the worker has accumulated since leveling up last
	 */
	int getHours();

	/**
	 * Add hours of training or experience.
	 *
	 * @param hrs       the number of hours to add
	 * @param condition If less than or equal to the number of hours after the addition,
	 *                  level up and zero the hours instead.
	 */
	void addHours(int hrs, int condition);

	/**
	 * @return a copy of this skill
	 */
	ISkill copy();

	/**
	 * A Skill is "empty" if the worker has no levels in it and no hours of experience in
	 * it.
	 *
	 * @return whether this skill is "empty"
	 */
	boolean isEmpty();
}

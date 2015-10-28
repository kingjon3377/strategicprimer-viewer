package model.map.fixtures.mobile.worker;

import org.eclipse.jdt.annotation.Nullable;

/**
 * A skill a worker has experience or training in.
 *
 * This is part of the Strategic Primer assistive programs suite developed by
 * Jonathan Lovelace.
 *
 * Copyright (C) 2012-2015 Jonathan Lovelace
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
public class Skill implements ISkill {
	/**
	 * The name of the skill.
	 */
	private String name;
	/**
	 * How many levels the worker has in the skill.
	 */
	private int level;
	/**
	 * How many hours the worker has gained since leveling up last.
	 */
	private int hours;

	/**
	 * Constructor.
	 *
	 * @param skillName the name of the skill
	 * @param skillLevel how many levels the worker has in the skill
	 * @param time how many hours of training or experience the worker has
	 *        gained since last gaining a level.
	 */
	public Skill(final String skillName, final int skillLevel, final int time) {
		name = skillName;
		level = skillLevel;
		hours = time;
	}
	/**
	 * @return a copy of this skill
	 * @param zero whether to "zero out" sensitive information
	 */
	@Override
	public Skill copy(final boolean zero) {
		if (zero) {
			return new Skill(name, 0, 0);
		} else {
			return new Skill(name, level, hours);
		}
	}
	/**
	 * @return the name of the skill
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * @return how many levels the worker has in the skill
	 */
	@Override
	public int getLevel() {
		return level;
	}

	/**
	 * @return how many hours the worker has accumulated since leveling up last
	 */
	@Override
	public int getHours() {
		return hours;
	}

	/**
	 * @param obj an object
	 * @return whether it's the same as this one
	 */
	@Override
	public boolean equals(@Nullable final Object obj) {
		return this == obj || obj instanceof Skill
				&& name.equals(((Skill) obj).name)
				&& level == ((Skill) obj).level && hours == ((Skill) obj).hours;
	}

	/**
	 * @return a hash value for the object
	 */
	@Override
	public int hashCode() {
		return name.hashCode();
	}

	/**
	 * Add hours of training or experience.
	 *
	 * @param hrs the number of hours to add
	 * @param condition If less than or equal to the number of hours after the
	 *        addition, level up and zero the hours instead.
	 */
	@Override
	public void addHours(final int hrs, final int condition) {
		hours += hrs;
		if (condition <= hours) {
			level++;
			hours = 0;
		}
	}

	/**
	 * @return a string representation of the skill
	 */
	@Override
	public String toString() {
		return name + " (" + Integer.toString(level) + ')';
	}

	/**
	 * @param nomen the skill's new name
	 */
	@Override
	public final void setName(final String nomen) {
		name = nomen;
	}

	/**
	 * A Skill is "empty" if the worker has no levels in it and no hours of
	 * experience in it.
	 *
	 * @return whether this skill is "empty"
	 */
	@Override
	public boolean isEmpty() {
		return level == 0 && hours == 0;
	}

}

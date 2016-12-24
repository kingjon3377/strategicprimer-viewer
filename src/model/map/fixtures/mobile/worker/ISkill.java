package model.map.fixtures.mobile.worker;

import java.util.Formatter;
import java.util.Objects;
import model.map.HasName;
import model.map.Subsettable;
import org.eclipse.jdt.annotation.NonNull;

/**
 * An interface for Skills.
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
public interface ISkill extends HasName, Subsettable<@NonNull ISkill> {
	/**
	 * How many levels the worker has in the skill.
	 * @return how many levels the worker has in the skill
	 */
	int getLevel();

	/**
	 * How many hours the worker has accumulated since the skill level last increased.
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
	@SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
	void addHours(int hrs, int condition);

	/**
	 * Clone the skill.
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

	/**
	 * A skill is a subset if it has the same name, equal or lower level, and if equal
	 * level equal or lower hours.
	 * @param obj     another Skill
	 * @param ostream a stream to explain our results on
	 * @param context a string to print before every line of output, describing the
	 *                context
	 * @return whether the Skill is a "subset" of this---same name, equal or lower level,
	 * equal or lower number of hours if equal level.
	 */
	@Override
	default boolean isSubset(final ISkill obj, final Formatter ostream,
							 final String context) {
		final int lvl = getLevel();
		final int hours = getHours();
		if (Objects.equals(obj.getName(), getName())) {
			if (obj.getLevel() > lvl) {
				ostream.format("%s\tExtra level(s) in %s%n", context, obj.getName());
				return false;
			} else if ((obj.getLevel() == lvl) && (obj.getHours() > hours)) {
				ostream.format("%s\tExtra hours in %s%n", context, obj.getName());
				return false;
			}
			return true;
		} else {
			ostream.format("%s\tCalled with non-corresponding skill, %s (this is %s)%n",
					context, obj.getName(), getName());
			return false;
		}
	}
}

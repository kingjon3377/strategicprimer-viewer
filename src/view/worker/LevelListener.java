package view.worker;

import model.listeners.LevelGainListener;
import model.listeners.SkillSelectionListener;
import model.listeners.UnitMemberListener;
import model.map.HasName;
import model.map.fixtures.UnitMember;
import model.map.fixtures.mobile.worker.ISkill;
import org.eclipse.jdt.annotation.Nullable;

import static view.util.SystemOut.SYS_OUT;

/**
 * A listener to print a line when a worker gains a level.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2013-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public final class LevelListener
		implements LevelGainListener, UnitMemberListener, SkillSelectionListener {
	/**
	 * The current worker.
	 */
	@Nullable
	private UnitMember worker = null;
	/**
	 * The current skill.
	 */
	@Nullable
	private ISkill skill = null;

	/**
	 * Wrapper around {@link HasName#getName()} that also handles non-HasName objects,
	 * using their toString().
	 * @param named something that may have a name
	 * @return its name if it has one, "null" if null, or its toString otherwise.
	 */
	@SuppressWarnings("TypeMayBeWeakened")
	private static String getName(final Object named) {
		if (named instanceof HasName) {
			return ((HasName) named).getName();
		} else {
			return named.toString();
		}
	}

	/**
	 * Set the currently-selected skill.
	 * @param selectedSkill the newly selected skill
	 */
	@Override
	public void selectSkill(@Nullable final ISkill selectedSkill) {
		skill = selectedSkill;
	}

	/**
	 * Handle level gain notification.
	 */
	@SuppressWarnings("resource")
	@Override
	public void level() {
		final UnitMember wkr = worker;
		final ISkill skl = skill;
		if ((wkr != null) && (skl != null)) {
			SYS_OUT.append(getName(wkr));
			SYS_OUT.append(" gained a level in ");
			SYS_OUT.append(getName(skl));
		}
	}

	/**
	 * Select a new unit member.
	 * @param old      the previously selected member
	 * @param selected the newly selected unit member
	 */
	@Override
	public void memberSelected(@Nullable final UnitMember old,
							   @Nullable final UnitMember selected) {
		worker = selected;
	}

	/**
	 * A trivial toString().
	 * @return a String representation of the object
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "LevelListener";
	}
}

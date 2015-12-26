package view.worker;

import model.listeners.LevelGainListener;
import model.listeners.SkillSelectionListener;
import model.listeners.UnitMemberListener;
import model.map.HasName;
import model.map.fixtures.UnitMember;
import model.map.fixtures.mobile.worker.ISkill;
import org.eclipse.jdt.annotation.Nullable;
import util.NullCleaner;

import static view.util.SystemOut.SYS_OUT;

/**
 * A listener to print a line when a worker gains a level.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2013-2015 Jonathan Lovelace
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
public final class LevelListener implements LevelGainListener,
		                                            UnitMemberListener,
		                                            SkillSelectionListener {
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
	 * @param nSkill the newly selected skill
	 */
	@Override
	public void selectSkill(@Nullable final ISkill nSkill) {
		skill = nSkill;
	}

	/**
	 * Handle level gain notification.
	 */
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
	 * @param old      the previously selected member
	 * @param selected the newly selected unit member
	 */
	@Override
	public void memberSelected(@Nullable final UnitMember old,
	                           @Nullable final UnitMember selected) {
		worker = selected;
	}

	/**
	 * @param named something that may have a name
	 * @return its name if it has one, "null" if null, or its toString otherwise.
	 */
	@SuppressWarnings("TypeMayBeWeakened")
	private static String getName(final Object named) {
		if (named instanceof HasName) {
			return ((HasName) named).getName(); // NOPMD
		} else {
			return NullCleaner.valueOrDefault(named.toString(), "");
		}
	}

	/**
	 * @return a String representation of the object
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "LevelListener";
	}
}

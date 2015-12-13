package view.worker;

import static view.util.SystemOut.SYS_OUT;

import java.io.IOException;

import org.eclipse.jdt.annotation.Nullable;

import model.listeners.LevelGainListener;
import model.listeners.SkillSelectionListener;
import model.listeners.UnitMemberListener;
import model.map.HasName;
import model.map.IFixture;
import model.map.fixtures.UnitMember;
import model.map.fixtures.mobile.worker.ISkill;
import util.NullCleaner;

/**
 * A listener to print a line when a worker gains a level.
 *
 * This is part of the Strategic Primer assistive programs suite developed by
 * Jonathan Lovelace.
 *
 * Copyright (C) 2013-2015 Jonathan Lovelace
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
 * this program. If not, see <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public final class LevelListener implements LevelGainListener,
		UnitMemberListener, SkillSelectionListener {
	/**
	 * A type-safe null Skill.
	 */
	private static final ISkill NULL_SKILL = new ISkill() {
		@Override
		public boolean equals(@Nullable final Object obj) {
			return this == obj;
		}
		@Override
		public int hashCode() {
			return -1;
		}
		@Override
		public void addHours(final int hrs, final int condition) {
			// Do nothing
		}
		@Override
		public String getName() {
			return "null";
		}
		@Override
		public void setName(final String nomen) {
			throw new IllegalStateException("setName called on null Skill");
		}
		@Override
		public int getLevel() {
			return -1;
		}
		@Override
		public int getHours() {
			return -1;
		}
		@Override
		public ISkill copy(final boolean zero) {
			throw new IllegalStateException("copy called on null Skill");
		}
		@Override
		public boolean isEmpty() {
			return true;
		}
	};
	/**
	 * A type-safe null UnitMember.
	 */
	private static final UnitMember NULL_UM = new UnitMember() {
		@Override
		public int getID() {
			return -1;
		}
		@Override
		public boolean equalsIgnoringID(final IFixture fix) {
			return this == fix;
		}

		/**
		 * @param obj
		 *            another UnitMember
		 * @param ostream
		 *            a stream to report an explanation on
		 * @param context
		 *            a string to print before every line of output, describing
		 *            the context
		 * @return false
		 */
		@Override
		public boolean isSubset(final IFixture obj, final Appendable ostream,
				final String context) throws IOException {
			return false;
		}
		/**
		 * @return nothing; throws
		 * @param zero ignored
		 */
		@Override
		public UnitMember copy(final boolean zero) {
			throw new IllegalStateException("Tried to copy a null object");
		}
	};
	/**
	 * The current worker.
	 */
	private UnitMember worker = NULL_UM;
	/**
	 * The current skill.
	 */
	private ISkill skill = NULL_SKILL;
	/**
	 *
	 * @param nSkill the newly selected skill
	 */
	@Override
	public void selectSkill(@Nullable final ISkill nSkill) {
		skill = NullCleaner.valueOrDefault(nSkill, NULL_SKILL);
	}
	/**
	 * Handle level gain notification.
	 */
	@Override
	public void level() {
		final UnitMember wkr = worker;
		final ISkill skl = skill;
		if (!NULL_UM.equals(wkr) && !NULL_SKILL.equals(skl)) {
			final StringBuilder builder = new StringBuilder();
			builder.append(getName(wkr));
			builder.append(" gained a level in ");
			builder.append(getName(skl));
			SYS_OUT.println(builder.toString());
		}
	}

	/**
	 * @param old the previously selected member
	 * @param selected the newly selected unit member
	 */
	@Override
	public void memberSelected(@Nullable final UnitMember old,
			@Nullable final UnitMember selected) {
		worker = NullCleaner.valueOrDefault(selected, NULL_UM);
	}

	/**
	 * @param named something that may have a name
	 * @return its name if it has one, "null" if null, or its toString
	 *         otherwise.
	 */
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
	@Override
	public String toString() {
		return "LevelListener";
	}
}

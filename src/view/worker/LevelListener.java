package view.worker;

import model.listeners.CompletionListener;
import model.listeners.LevelGainListener;
import model.listeners.UnitMemberListener;
import model.map.HasName;
import model.map.IFixture;
import model.map.fixtures.UnitMember;
import model.map.fixtures.mobile.worker.Skill;

import org.eclipse.jdt.annotation.Nullable;

import view.util.SystemOut;

/**
 * A listener to print a line when a worker gains a level.
 *
 * @author Jonathan Lovelace
 */
public final class LevelListener implements LevelGainListener,
		UnitMemberListener, CompletionListener {
	/**
	 * A type-safe null Skill.
	 */
	private static final Skill NULL_SKILL = new Skill("null", -1, -1) {
		@Override
		public boolean equals(@Nullable final Object obj) {
			return this == obj;
		}
		@Override
		public int hashCode() {
			return -1;
		}
		// ESCA-JAVA0025:
		@Override
		public void addHours(final int hrs, final int condition) {
			// Do nothing
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
	};
	/**
	 * The current worker.
	 */
	private UnitMember worker = NULL_UM;
	/**
	 * The current skill.
	 */
	private Skill skill = NULL_SKILL;

	/**
	 * @param result maybe the newly selected skill
	 */
	@Override
	public void stopWaitingOn(final Object result) {
		if ("null_skill".equals(result)) {
			skill = NULL_SKILL;
		} else if (result instanceof Skill) {
			skill = (Skill) result;
		}
	}

	/**
	 * Handle level gain notification.
	 */
	@Override
	public void level() {
		if (!NULL_UM.equals(worker) && !NULL_SKILL.equals(skill)) {
			final UnitMember wkr = worker;
			final Skill skl = skill;
			assert skl != null;
			final StringBuilder builder = new StringBuilder();
			builder.append(getName(wkr));
			builder.append(" gained a level in ");
			builder.append(getName(skl));
			SystemOut.SYS_OUT.println(builder.toString());
		}
	}

	/**
	 * @param old the previously selected member
	 * @param selected the newly selected unit member
	 */
	@Override
	public void memberSelected(@Nullable final UnitMember old,
			@Nullable final UnitMember selected) {
		worker = selected == null ? NULL_UM : selected;
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
			return named.toString();
		}
	}
}

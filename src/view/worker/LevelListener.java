package view.worker;

import model.listeners.CompletionListener;
import model.listeners.LevelGainListener;
import model.listeners.UnitMemberListener;
import model.map.HasName;
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
	 * The current worker.
	 */
	@Nullable
	private UnitMember worker = null;
	/**
	 * The current skill.
	 */
	@Nullable
	private Skill skill = null;

	/**
	 * @param result maybe the newly selected skill
	 */
	@Override
	public void stopWaitingOn(final Object result) {
		if ("null_skill".equals(result)) {
			skill = null;
		} else if (result instanceof Skill) {
			skill = (Skill) result;
		}
	}

	/**
	 * Handle level gain notification.
	 */
	@Override
	public void level() {
		if (worker != null && skill != null) {
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
		worker = selected;
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

package model.listeners;

import model.map.fixtures.UnitMember;

import org.eclipse.jdt.annotation.Nullable;

/**
 * An interface for objects that want to know when a new unit member (usually a
 * worker) is selected.
 *
 * @author Jonathan Lovelace
 *
 */
public interface UnitMemberListener {
	/**
	 * @param old the previous selection
	 * @param selected the new selection. Because sometimes there's no selection, may be null.
	 */
	void memberSelected(@Nullable final UnitMember old, @Nullable final UnitMember selected);
}

package model.listeners;
/**
 * An interface for objects that notify others of the user's selection of a unit member.
 * @author Jonathan Lovelace
 *
 */
public interface UnitMemberSelectionSource {
	/**
	 * @param list a listener to add
	 */
	void addUnitMemberListener(final UnitMemberListener list);
	/**
	 * @param list a listener to remove
	 */
	void removeUnitMemberListener(final UnitMemberListener list);
}

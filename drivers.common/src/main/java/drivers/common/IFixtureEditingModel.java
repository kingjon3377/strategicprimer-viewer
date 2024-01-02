package drivers.common;

import legacy.map.HasKind;
import common.map.HasName;
import legacy.map.HasOwner;
import legacy.map.Player;

import legacy.map.fixtures.UnitMember;

import legacy.map.fixtures.mobile.IUnit;

public interface IFixtureEditingModel extends IDriverModel {
	/**
	 * Move a unit-member from one unit to another.
	 */
	void moveMember(UnitMember member, IUnit old, IUnit newOwner);

	/**
	 * Remove the given unit from the map. It must be empty, and may be
	 * required to be owned by the current player. The operation will also
	 * fail if "matching" units differ in name or kind from the provided
	 * unit.  Returns true if the preconditions were met and the unit was
	 * removed, and false otherwise.
	 */
	boolean removeUnit(IUnit unit);

	/**
	 * Add a new member to a unit.
	 * @param unit The unit that should own the member
	 * @param member The member to add to the unit
	 */
	void addUnitMember(IUnit unit, UnitMember member);

	/**
	 * Change something's name. Returns true if we were able to find it and
	 * changed its name, false on failure.
	 */
	boolean renameItem(HasName item, String newName);

	/**
	 * Change something's kind. Returns true if we were able to find it and
	 * changed its kind, false on failure.
	 */
	boolean changeKind(HasKind item, String newKind);

	/**
	 * Dismiss a unit member from a unit and from the player's service.
	 */
	void dismissUnitMember(UnitMember member);

	/**
	 * Add a unit member to the unit that contains the given member in each
	 * map.  Returns true if any of the maps had a unit containing the
	 * existing sibling, to which the new member was added, false
	 * otherwise.
	 *
	 * @param base The member that is already in the tree
	 * @param sibling The member to add as its sibling
	 */
	boolean addSibling(UnitMember base, UnitMember sibling);

	/**
	 * Change the owner of the given item in all maps. Returns true if this
	 * succeeded in any map, false otherwise.
	 */
	boolean changeOwner(HasOwner item, Player newOwner);

	/**
	 * Add a unit in its owner's HQ.
	 * @param unit The unit to add
	 */
	void addUnit(IUnit unit);

	/**
	 * Sort the members of the given unit in all maps. Returns true if any
	 * map contained a matching unit, false otherwise.
	 *
	 * TODO: Also support fortresses, and any other
	 * fixtures-containing-fixtures as they come to exist
	 */
	boolean sortFixtureContents(IUnit fixture);

	/**
	 * The unit members that have been dismissed during this session.
	 */
	Iterable<UnitMember> getDismissed();
}

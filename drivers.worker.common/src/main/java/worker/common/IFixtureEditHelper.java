package worker.common;

import common.map.HasName;
import common.map.HasOwner;
import common.map.fixtures.UnitMember;

import drivers.common.PlayerChangeListener;
import drivers.common.MapChangeListener;

import common.map.fixtures.mobile.IUnit;

import common.map.HasKind;
import common.map.Player;

/**
 * An interface for helpers to allow the worker tree component, the fixture
 * list in the map viewer, etc., to allow the user to edit fixtures.
 */
public interface IFixtureEditHelper extends NewUnitListener, PlayerChangeListener, MapChangeListener {
	/**
	 * Move a member between units.
	 *
	 * @param member The member to move.
	 * @param old Its prior owner
	 * @param newOwner Its new owner
	 */
	void moveMember(UnitMember member, IUnit old, IUnit newOwner);

	/**
	 * Add a new unit, and also handle adding it to the map (via the driver model).
	 *
	 * @param unit The unit to add
	 */
	void addUnit(IUnit unit);

	/**
	 * (Try to) remove a unit (from the map, via the driver model).
	 *
	 * @param unit The unit to remove
	 */
	void removeUnit(IUnit unit);

	/**
	 * Add a new member to a unit.
	 *
	 * @param unit The unit that should contain the member
	 * @param member The member to add to the unit
	 */
	void addUnitMember(IUnit unit, UnitMember member);

	/**
	 * Change something's name.
	 */
	void renameItem(HasName item, String newName);

	/**
	 * Change something's kind; in the worker mgmt GUI, if it's a unit,
	 * this means moving it in the tree, since units' kinds are currently
	 * their parent nodes.
	 */
	void changeKind(HasKind item, String newKind);

	/**
	 * Dismiss a unit member from a unit and from the player's service.
	 */
	void dismissUnitMember(UnitMember member);

	/**
	 * Add a unit member to the unit that contains the given member. If the
	 * base is not in the tree, the model is likely to simply ignore the
	 * call, but the behavior is undefined.
	 *
	 * @param base The member that is already in the tree.
	 * @param sibling The member to add as its sibling.
	 */
	void addSibling(UnitMember base, UnitMember sibling);

	/**
	 * Change the owner of the given item.
	 */
	void changeOwner(HasOwner item, Player newOwner);

	/**
	 * Sort the contents of the given unit.
	 */
	void sortMembers(IUnit fixture);
}

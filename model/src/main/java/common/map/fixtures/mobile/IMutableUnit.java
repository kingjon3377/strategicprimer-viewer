package common.map.fixtures.mobile;

import common.map.fixtures.UnitMember;

import common.map.HasMutableImage;
import common.map.HasMutableKind;
import common.map.HasMutableName;
import common.map.HasMutableOwner;
import common.map.HasMutablePortrait;

/**
 * An interface for mutator methods on units.
 */
public interface IMutableUnit extends IUnit, HasMutableKind, HasMutableName,
		HasMutableImage, HasMutableOwner, HasMutablePortrait {
	/**
	 * Set the unit's orders for a turn.
	 */
	void setOrders(int turn, String newOrders);

	/**
	 * Set the unit's results for a turn.
	 */
	void setResults(int turn, String newResults);

	/**
	 * Add a member.
	 */
	void addMember(UnitMember member);

	/**
	 * Remove a member
	 */
	void removeMember(UnitMember member);

	/**
	 * Change the internal order of members to be sorted. Sort order is
	 * implementation-defined.
	 */
	void sortMembers();

	/**
	 * Clone the unit.
	 */
	@Override
	IMutableUnit copy(boolean zero);
}

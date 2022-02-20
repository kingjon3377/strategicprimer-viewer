package common.map.fixtures.towns;

import common.map.HasMutableName;
import common.map.HasMutableImage;

import common.map.fixtures.FortressMember;

/**
 * A fortress on the map. A player can only have one fortress per tile, but
 * multiple players may have fortresses on the same tile.
 *
 * FIXME: We need something about buildings yet
 */
public interface IMutableFortress extends IFortress, HasMutableImage,
		IMutableTownFixture, HasMutableName {
	/**
	 * Add a member to the fortress.
	 */
	void addMember(FortressMember member);

	/**
	 * Remove a member from the fortress.
	 */
	void removeMember(FortressMember member);

	/**
	 * Clone the fortress.
	 */
	@Override
	IMutableFortress copy(CopyBehavior zero);
}

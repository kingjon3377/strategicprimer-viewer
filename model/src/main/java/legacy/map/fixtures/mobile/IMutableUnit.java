package legacy.map.fixtures.mobile;

import legacy.map.fixtures.UnitMember;

import legacy.map.HasMutableImage;
import legacy.map.HasMutableKind;
import legacy.map.HasMutableName;
import legacy.map.HasMutableOwner;
import legacy.map.HasMutablePortrait;

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
    IMutableUnit copy(CopyBehavior zero);
}

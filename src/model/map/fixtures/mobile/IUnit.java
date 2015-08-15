package model.map.fixtures.mobile;

import model.map.FixtureIterable;
import model.map.HasImage;
import model.map.HasKind;
import model.map.HasName;
import model.map.HasOwner;
import model.map.Subsettable;
import model.map.fixtures.UnitMember;

/**
 * An interface for units.
 * @author Jonathan Lovelace
 */
public interface IUnit extends MobileFixture, HasImage, HasKind,
		FixtureIterable<UnitMember>, HasName, HasOwner, Subsettable<IUnit> {
	/**
	 * @return the unit's orders
	 */
	String getOrders();
	/**
	 * @param newOrders the unit's new orders
	 */
	void setOrders(String newOrders);
	/**
	 * @return a verbose description of the Unit.
	 */
	String verbose();
	/**
	 * Add a member.
	 * @param member the member to add
	 */
	void addMember(UnitMember member);
	/**
	 * Remove a member.
	 * @param member the member to remove
	 */
	void removeMember(UnitMember member);
	/**
	 * A specialization of the method from IFixture.
	 * @return a copy of the member
	 * @param zero whether to "zero out" or omit sensitive information
	 */
	@Override
	IUnit copy(boolean zero);
}

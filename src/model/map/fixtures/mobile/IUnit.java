package model.map.fixtures.mobile;

import org.eclipse.jdt.annotation.NonNull;

import model.map.FixtureIterable;
import model.map.HasImage;
import model.map.HasKind;
import model.map.HasName;
import model.map.HasOwner;
import model.map.fixtures.FortressMember;
import model.map.fixtures.UnitMember;

/**
 * An interface for units.
 *
 * This is part of the Strategic Primer assistive programs suite developed by
 * Jonathan Lovelace.
 *
 * Copyright (C) 2014-2015 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of version 3 of the GNU General Public License as published by the
 * Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public interface IUnit extends MobileFixture, HasImage, HasKind,
		FixtureIterable<@NonNull UnitMember>, HasName, HasOwner, FortressMember {
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

package model.map;


/**
 * A supertype for both TileFixture and any UnitMembers (etc.) that shouldn't be
 * TileFixtures, so we don't have to special-case them for things like
 * searching.
 *
 * This is part of the Strategic Primer assistive programs suite developed by
 * Jonathan Lovelace.
 *
 * Copyright (C) 2013-2015 Jonathan Lovelace
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
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * @author Jonathan Lovelace
 *
 */
public interface IFixture {
	/**
	 * @return an ID (UID for most fixtures, though perhaps not for things like
	 *         mountains and hills) for the fixture.
	 */
	int getID();

	/**
	 * @param fix a fixture
	 * @return whether it's equal, ignoring ID (and DC for events), to this one
	 */
	boolean equalsIgnoringID(IFixture fix);

	/**
	 * @return A copy of this fixture, possibly "sanitized" in a way that won't
	 *         break subsets.
	 * @param zero
	 *            whether to "zero out" (omit) sensitive information in the copy
	 */
	IFixture copy(boolean zero);
}

package model.map;


/**
 * A supertype for both TileFixture and any UnitMembers (etc.) that shouldn't be
 * TileFixtures, so we don't have to special-case them for things like searching.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2013-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public interface IFixture {
	/**
	 * The fixture's ID number.
	 * @return an ID (UID for most fixtures, though perhaps not for things like
	 * mountains) for the fixture.
	 */
	int getID();

	/**
	 * Whether a fixture is equal if we ignore its ID.
	 * @param fix a fixture
	 * @return whether it's equal, ignoring ID (and DC for events), to this one
	 */
	boolean equalsIgnoringID(IFixture fix);

	/**
	 * Clone the fixture.
	 * @param zero whether to "zero out" (omit) sensitive information in the copy
	 * @return A copy of this fixture, possibly "sanitized" in a way that won't break
	 * subsets.
	 */
	IFixture copy(boolean zero);
}

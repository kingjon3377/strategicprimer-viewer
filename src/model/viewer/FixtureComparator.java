package model.viewer;

import java.util.Comparator;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import model.map.TileFixture;

/**
 * A Comparator for TileFixtures. In the new map version, only the upper-most of
 * a tile's fixtures is visible.
 *
 * This is part of the Strategic Primer assistive programs suite developed by
 * Jonathan Lovelace.
 *
 * Copyright (C) 2011-2014 Jonathan Lovelace
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
 * TODO: tests
 *
 * @author Jonathan Lovelace
 *
 */
public class FixtureComparator implements Comparator<@NonNull TileFixture> {
	/**
	 * Compare two fixtures.
	 *
	 * @param one The first fixture
	 * @param two The second fixture
	 * @return the result of the comparison.
	 */
	@Override
	public int compare(@Nullable final TileFixture one,
			@Nullable final TileFixture two) {
		if (one == null || two == null) {
			throw new IllegalArgumentException("Asked to compare null fixture");
		}
		return two.getZValue() - one.getZValue();
	}

	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "FixtureComparator";
	}
}

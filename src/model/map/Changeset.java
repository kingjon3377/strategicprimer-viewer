package model.map;

/**
 * An interface to represent a set of changes that can be made to a map (TODO: or to
 * what?) It'll be used to represent the differences between an earlier and a later map.
 *
 * TODO: Tests.
 *
 * TODO: Think of how to implement this.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2012-2015 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program. If not, see
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public interface Changeset {
	/**
	 * @return The number of the "before" turn.
	 */
	int from();

	/**
	 * @return The number of the "after" turn.
	 */
	int to(); // NOPMD

	/**
	 * @return the inverse of this set of operations
	 */
	Changeset invert();

	/**
	 * Apply the changeset to a map.
	 *
	 * TODO: Should this possibly take different arguments?
	 *
	 * @param map the map to apply it to.
	 */
	void apply(IMapNG map);
}

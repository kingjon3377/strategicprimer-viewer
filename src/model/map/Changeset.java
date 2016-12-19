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
 * Copyright (C) 2012-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public interface Changeset {
	/**
	 * The number of the turn before the changeset is applied.
	 * @return The number of the "before" turn.
	 */
	int from();

	/**
	 * The number of the turn after the changeset is applied.
	 * @return The number of the "after" turn.
	 */
	int to();

	/**
	 * Returns the inverse of the changeset.
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

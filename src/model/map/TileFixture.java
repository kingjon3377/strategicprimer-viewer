package model.map;

import org.eclipse.jdt.annotation.NonNull;

/**
 * Something that can go on a tile.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2011-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
@SuppressWarnings("ComparableImplementedButEqualsNotOverridden")
public interface TileFixture extends IFixture, Comparable<@NonNull TileFixture> {
	// Marker interface; also, TODO: what members should this have?

	/**
	 * @return a String describing all members of a kind of fixture.
	 */
	String plural();

	/**
	 * @return a *short*, no more than one line and preferably no more than two dozen
	 * characters, description of the fixture, suitable for saying what it is when an
	 * explorer happens on it.
	 */
	String shortDesc();

	/**
	 * Specialization of method from IFixture.
	 *
	 * @param zero whether to "zero out" any sensitive information
	 * @return a copy of this fixture
	 */
	@Override
	TileFixture copy(boolean zero);

	/**
	 * @param fix A TileFixture to compare to
	 * @return the result of the comparison
	 */
	@SuppressWarnings("ParameterNameDiffersFromOverriddenParameter")
	@Override
	default int compareTo(final TileFixture fix) {
		final int ours = hashCode();
		final int theirs = fix.hashCode();
		if (ours > theirs) {
			return 1;
		} else if (ours == theirs) {
			return 0;
		} else {
			return -1;
		}
	}

	/**
	 * The required Perception check for an explorer to find the fixture.
	 *
	 * Some rough guidelines for the scale:
	 *
	 * <ul>
	 * <li>0 is "impossible to miss": the type of terrain you pass through</li>
	 * <li>10 and under is "hard to miss": forests, mountains, rivers, perhaps hills</li>
	 * <li>10-20 is "not hard to spot": shrubs, active populations</li>
	 * <li>20-30 is "you have to be observant": ruins, etc.</li>
	 * <li>30+ is generally "<i>really</i> observant or specialized equipment":
	 * unexposed mineral deposits, portals to other worlds, etc.</li>
	 * </ul>
	 *
	 * TODO: In many or most cases, DCs should take surrounding-terrain context into
	 * account, and so this shouldn't be an instance function here, but either on the
	 * map or in a separate class
	 *
	 * @return the DC to discover the fixture.
	 */
	int getDC();
}

package model.map;

/**
 * An interface for the representation of a player in the game.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2017 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public interface Player extends Comparable<Player>, HasName {
	/**
	 * The player's ID number.
	 * @return the player's number
	 */
	int getPlayerId();

	/**
	 * Whether this is the current player.
	 * @return true iff this is the current player
	 */
	boolean isCurrent();

	/**
	 * Whether this is the (or an) "independent" player.
	 * @return whether this is the (or an) "independent" player---the "owner" of unowned
	 * fixtures.
	 */
	default boolean isIndependent() {
		return "independent".equalsIgnoreCase(getName());
	}
}

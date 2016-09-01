package model.map;

import org.eclipse.jdt.annotation.NonNull;

/**
 * An interface for collections of players.
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
public interface IPlayerCollection
		extends Iterable<@NonNull Player>, Subsettable<@NonNull IPlayerCollection> {

	/**
	 * @param player a player-id
	 * @return the player with that ID, or a new Player with that number if we don't have
	 * it.
	 */
	Player getPlayer(int player);

	/**
	 * Note that this method currently iterates through all the players to find the one
	 * marked current.
	 *
	 * @return the current player, or a new player with a negative number and the empty
	 * string for a name.
	 */
	Player getCurrentPlayer();

	/**
	 * @param obj an object
	 * @return whether we contain it
	 */
	boolean contains(Player obj);

	/**
	 * @return a player for "independent" fixtures.
	 */
	Player getIndependent();

	/**
	 * @return a copy of this collection
	 */
	IPlayerCollection copy();
}

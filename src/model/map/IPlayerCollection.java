package model.map;
/**
 * An interface for collections of players.
 *
 * This is part of the Strategic Primer assistive programs suite developed by
 * Jonathan Lovelace.
 *
 * Copyright (C) 2013-2014 Jonathan Lovelace
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
public interface IPlayerCollection extends Iterable<Player>,
		Subsettable<IPlayerCollection> {

	/**
	 * @param player a player-id
	 *
	 * @return the player with that ID, or a new Player with that number if we
	 *         don't have it.
	 */
	Player getPlayer(int player);

	/**
	 * Note that this method currently iterates through all the players to find
	 * the one marked current.
	 *
	 *
	 * @return the current player, or a new player with a negative number and
	 *         the empty string for a name.
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
	 * @param zero whether to "zero" sensitive details: should probably be ignored
	 * @return a copy of this collection
	 */
	IPlayerCollection copy(boolean zero);
}

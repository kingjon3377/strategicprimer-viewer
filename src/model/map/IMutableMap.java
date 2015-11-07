package model.map;


/**
 * An interface to specify all the mutator methods that were in IMap, and to
 * explicitly return mutable collections.
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
 * this program. If not, see <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 * @deprecated the old API is deprecated in this branch
 */
@Deprecated
public interface IMutableMap extends IMap {
	/**
	 * Add a player to the game.
	 *
	 * @param player the player to add
	 */
	void addPlayer(Player player);
	/**
	 * @return a mutable view of the players in the map
	 */
	@Override
	IMutablePlayerCollection getPlayers();
	/**
	 * @return a mutable view of the tiles in the map
	 */
	@Override
	IMutableTileCollection getTiles();
	/**
	 * @param point a point
	 * @return the tile at those coordinates
	 */
	@Override
	IMutableTile getTile(Point point);
}

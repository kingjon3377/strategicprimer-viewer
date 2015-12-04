package model.map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import model.map.fixtures.Ground;
import model.map.fixtures.terrain.Forest;

/**
 * A mutable map.
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
 *
 */
public interface IMutableMapNG extends IMapNG {
	/**
	 * @param player a player to add
	 */
	void addPlayer(Player player);
	/**
	 * @param location a location
	 * @param terrain the new "base terrain" at that location
	 */
	void setBaseTerrain(Point location, TileType terrain);
	/**
	 * @param location a location
	 * @param mtn whether it is mountainous
	 */
	void setMountainous(Point location, boolean mtn);
	/**
	 * @param location a location
	 * @param rivers rivers to add there
	 */
	void addRivers(Point location, River @NonNull ... rivers);
	/**
	 * @param location a location
	 * @param rivers rivers to remove there
	 */
	void removeRivers(Point location, River... rivers);

	/**
	 * @param location
	 *            a location
	 * @param forest
	 *            the forest (if any) at that location; null to remove any that
	 *            is there
	 */
	void setForest(Point location, @Nullable Forest forest);
	/**
	 * @param location a location
	 * @param ground the Ground at that location; null to remove any that is there
	 */
	void setGround(Point location, @Nullable Ground ground);
	/**
	 * Note that this is not necessarily related to the other querying methods;
	 * it's possible for the "Ground" to be null but for there to be a Ground
	 * here.
	 *
	 * @param location a location
	 * @param fix a fixture to add there
	 */
	void addFixture(Point location, TileFixture fix);
	/**
	 * Note that this is not necessarily related to the specific properties; if
	 * getForest doesn't return null, passing that here will probably have no
	 * effect.
	 *
	 * @param location a location
	 * @param fix a fixture to remove there.
	 */
	void removeFixture(Point location, TileFixture fix);
	/**
	 * @param player the new current player
	 */
	void setCurrentPlayer(Player player);
	/**
	 * @param turn the new current turn
	 */
	void setTurn(int turn);
}

package model.map;

import model.map.fixtures.Ground;
import model.map.fixtures.terrain.Forest;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A mutable map.
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
public interface IMutableMapNG extends IMapNG {
	/**
	 * Add a player to the map.
	 * @param player a player to add
	 */
	@SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
	void addPlayer(Player player);

	/**
	 * Set base terrain at a location.
	 * @param location    a location
	 * @param terrainType the new "base terrain" at that location
	 */
	void setBaseTerrain(Point location, TileType terrainType);

	/**
	 * Set whether a location is mountainous.
	 * @param location a location
	 * @param mtn      whether it is mountainous
	 */
	void setMountainous(Point location, boolean mtn);

	/**
	 * Add rivers at a location.
	 * @param location    a location
	 * @param addedRivers rivers to add there
	 */
	@SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
	void addRivers(Point location, @NonNull River @NonNull ... addedRivers);

	/**
	 * Remove rivers at a location.
	 * @param location      a location
	 * @param removedRivers rivers to remove there
	 */
	@SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
	void removeRivers(Point location, @NonNull River @NonNull ... removedRivers);

	/**
	 * Set the primary forest at a location.
	 * @param location a location
	 * @param forest   the forest (if any) at that location; null to remove any that is
	 *                 there
	 */
	void setForest(Point location, @Nullable Forest forest);

	/**
	 * Set the primary ground at a location.
	 * @param location  a location
	 * @param newGround the Ground at that location; null to remove any that is there
	 */
	void setGround(Point location, @Nullable Ground newGround);

	/**
	 * Note that this is not necessarily related to the other querying methods; it's
	 * possible for the "Ground" to be null but for there to be a Ground here.
	 *
	 * @param location a location
	 * @param fix      a fixture to add there
	 */
	@SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
	void addFixture(Point location, TileFixture fix);

	/**
	 * Note that this is not necessarily related to the specific properties; if getForest
	 * doesn't return null, passing that here will probably have no effect.
	 *
	 * @param location a location
	 * @param fix      a fixture to remove there.
	 */
	@SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
	void removeFixture(Point location, TileFixture fix);

	/**
	 * Set the current player.
	 * @param player the new current player
	 */
	void setCurrentPlayer(Player player);

	/**
	 * Set the current turn.
	 * @param currentTurn the new current turn
	 */
	void setCurrentTurn(int currentTurn);
}

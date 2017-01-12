package model.workermgmt;

import java.util.List;
import model.map.Player;
import model.map.fixtures.mobile.IUnit;
import model.misc.IMultiMapModel;
import org.eclipse.jdt.annotation.Nullable;

/**
 * An interface for a model to underlie the advancement GUI, etc.
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
public interface IWorkerModel extends IMultiMapModel {
	/**
	 * All the players in all the maps.
	 * @return a list of all the players in all the maps
	 */
	List<Player> getPlayers();

	/**
	 * The units in the map belonging to the given player.
	 * @param player a player in the map
	 * @return a list of the units in the map belonging to the player
	 */
	List<IUnit> getUnits(Player player);

	/**
	 * The "kinds" of units that the given player has.
	 * @param player a player in the map
	 * @return the "kinds" of unit that player has.
	 */
	List<String> getUnitKinds(Player player);

	/**
	 * The units with the given "kind" that the given player has.
	 * @param player a player in the map
	 * @param kind   a "kind" of unit.
	 * @return a list of the units of that kind in the map belonging to that player
	 */
	List<IUnit> getUnits(Player player, String kind);

	/**
	 * Add a unit in its owner's HQ.
	 *
	 * @param unit the unit to add.
	 */
	@SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
	void addUnit(IUnit unit);

	/**
	 * Get a unit by ID #.
	 *
	 * @param owner the unit's owner
	 * @param id    the ID # to search for
	 * @return the unit with that ID, or null if no unit has that ID.
	 */
	@Nullable IUnit getUnitByID(Player owner, int id);
}

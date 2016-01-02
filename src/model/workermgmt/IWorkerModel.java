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
 * Copyright (C) 2013-2015 Jonathan Lovelace
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
public interface IWorkerModel extends IMultiMapModel {
	/**
	 * @return a list of all the players in all the maps
	 */
	List<Player> getPlayers();
	/**
	 * @param player a player in the map
	 * @return a list of the units in the map belonging to the player
	 */
	List<IUnit> getUnits(Player player);

	/**
	 * @param player a player in the map
	 * @return the "kinds" of unit that player has.
	 */
	List<String> getUnitKinds(Player player);

	/**
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

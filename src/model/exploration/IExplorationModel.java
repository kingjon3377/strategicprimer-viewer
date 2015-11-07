package model.exploration;

import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

import model.listeners.MovementCostSource;
import model.listeners.SelectionChangeSource;
import model.map.Player;
import model.map.Point;
import model.map.TileFixture;
import model.map.fixtures.mobile.IUnit;
import model.map.fixtures.mobile.SimpleMovement.TraversalImpossibleException;
import model.misc.IMultiMapModel;

/**
 * A model for exploration drivers.
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
public interface IExplorationModel extends IMultiMapModel,
		SelectionChangeSource, MovementCostSource {
	/**
	 * An enumeration of directions.
	 */
	public enum Direction {
		/**
		 * North.
		 */
		North,
		/**
		 * Northeast.
		 */
		Northeast,
		/**
		 * East.
		 */
		East,
		/**
		 * Southeast.
		 */
		Southeast,
		/**
		 * South.
		 */
		South,
		/**
		 * Southwest.
		 */
		Southwest,
		/**
		 * West.
		 */
		West,
		/**
		 * Northwest.
		 */
		Northwest,
		/**
		 * Stay still.
		 */
		Nowhere;
	}

	/**
	 * @return all the players that are shared by all the maps
	 */
	List<Player> getPlayerChoices();

	/**
	 * @param player a player
	 * @return all that player's units in the master map
	 */
	List<IUnit> getUnits(Player player);

	/**
	 * Move the currently selected unit from its current tile one tile in the
	 * specified direction. Moves the unit in all maps where the unit *was* in
	 * the specified tile, copying terrain information if the tile didn't exist
	 * in a subordinate map. If movement in the specified direction is
	 * impossible, we update all subordinate maps with the terrain information
	 * showing that, then re-throw the exception; callers should deduct a
	 * minimal MP cost.
	 *
	 * @param direction the direction to move
	 * @return the movement cost
	 * @throws TraversalImpossibleException if movement in that direction is
	 *         impossible
	 */
	int move(Direction direction) throws TraversalImpossibleException;

	/**
	 * @param point a point
	 * @param direction a direction
	 * @return the point bordering the specified one in the specified direction
	 */
	Point getDestination(Point point, Direction direction);

	/**
	 * @param fix a fixture
	 * @return the first location found (search order is not defined) containing
	 *         a fixture "equal to" the specified one. (Using it on mountains,
	 *         e.g., will *not* do what you want ...)
	 */
	Point find(TileFixture fix);

	/**
	 * @return the currently selected unit---may be null!
	 */
	@Nullable
	IUnit getSelectedUnit();

	/**
	 * @return its location. This will *not* be null.
	 */
	Point getSelectedUnitLocation();
}

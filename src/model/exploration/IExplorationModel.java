package model.exploration;

import java.util.List;

import model.map.Player;
import model.map.Point;
import model.map.TileFixture;
import model.map.fixtures.mobile.SimpleMovement.TraversalImpossibleException;
import model.map.fixtures.mobile.Unit;
import model.misc.IMultiMapModel;
/**
 * A model for exploration drivers.
 * @author Jonathan Lovelace
 *
 */
public interface IExplorationModel extends IMultiMapModel {
	/**
	 * An enumeration of directions.
	 */
	enum Direction {
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
		Northwest;
	}
	/**
	 * @return all the players that are shared by all the maps
	 */
	List<Player> getPlayerChoices();
	/**
	 * @param player a player
	 * @return all that player's units in the master map
	 */
	List<Unit> getUnits(final Player player);

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
	int move(final Direction direction)
			throws TraversalImpossibleException;
	/**
	 * @param point a point
	 * @param direction a direction
	 * @return the point bordering the specified one in the specified direction
	 */
	Point getDestination(final Point point, final Direction direction);
	/**
	 * @param fix a fixture
	 * @return the first location found (search order is not defined) containing a
	 *         fixture "equal to" the specified one. (Using it on mountains,
	 *         e.g., will *not* do what you want ...)
	 */
	Point find(final TileFixture fix);
	/**
	 * @return the currently selected unit---may be null!
	 */
	Unit getSelectedUnit();
	/**
	 * @return its location. This will *not* be null.
	 */
	Point getSelectedUnitLocation();
}

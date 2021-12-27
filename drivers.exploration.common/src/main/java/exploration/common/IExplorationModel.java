package exploration.common;

import org.jetbrains.annotations.Nullable;
import drivers.common.IFixtureEditingModel;
import drivers.common.SelectionChangeSource;
import drivers.common.IMultiMapModel;
import common.map.Point;
import common.map.Player;
import common.map.Direction;
import common.map.River;
import common.map.TileFixture;
import common.map.TileType;
import common.map.fixtures.mobile.IUnit;

/**
 * A model for exploration apps.
 */
public interface IExplorationModel extends IMultiMapModel, SelectionChangeSource,
		MovementCostSource, IFixtureEditingModel {
	/**
	 * Players that are shared by all the maps. TODO: Collection rather than just Iterable?
	 */
	Iterable<Player> getPlayerChoices();

	/**
	 * The given player's units in the main (master) map. TODO: Collection rather than just Iterable?
	 */
	Iterable<IUnit> getUnits(Player player);

	/**
	 * Move the currently selected unit from its current location one tile
	 * in the specified direction. Moves the unit in all maps where the
	 * unit <em>was</em> in that tile, copying terrain information if the
	 * tile didn't exist in a subordinate map. If movement in the specified
	 * direction is impossible, we update all subordinate maps with the
	 * terrain information showing that, then re-throw the exception;
	 * callers should deduct a minimal MP cost (though we notify listeners
	 * of that cost). We return the cost of the move in MP, which we also tell listeners about.
	 * 
	 * @throws TraversalImpossibleException if movement in the specified direction is impossible
	 */
	int move(Direction direction, Speed speed) throws TraversalImpossibleException;

	/**
	 * Given a starting point and a direction, get the next point in that direction.
	 */
	Point getDestination(Point point, Direction direction);

	/**
	 * Get the location of the first fixture that can be found that is
	 * "equal to" the given fixture, or "the invalid point" if not
	 * found.
	 */
	Point find(TileFixture fixture);

	/**
	 * The currently selected unit, if any.
	 */
	@Nullable
	IUnit getSelectedUnit();

	/**
	 * Set the currently selected unit. TODO: Do we really need to allow setting it to null?
	 */
	void setSelectedUnit(@Nullable IUnit selectedUnit);

	/**
	 * The location of the currently selected unit, or "the invalid point" if none.
	 */
	Point getSelectedUnitLocation();

	/**
	 * If there is a currently selected unit, make any independent villages
	 * at its location change to be owned by the owner of the currently
	 * selected unit. This costs MP.
	 */
	void swearVillages();

	/**
	 * If there is a currently selected unit, change one
	 * {@link common.map.fixtures.Ground},
	 * {@link common.map.fixtures.resources.StoneDeposit}, or
	 * {common.map.fixtures.resources.MineralVein} at the location of that
	 * unit from unexposed to exposed (and discover it). This costs MP.
	 */
	void dig();

	/**
	 * Add the given {@link unit} at the given {@link location}.
	 */
	void addUnitAtLocation(IUnit unit, Point location);

	/**
	 * Copy the given fixture from the main map to subordinate maps. (It is
	 * found in the main map by ID, rather than trusting the input, unless
	 * it is animal tracks.) If it is a cache, remove it from the main map.
	 * Default to removing sensitive information from the copies.  Returns
	 * true if we think this changed anything in any of the sub-maps.
	 */
	default boolean copyToSubMaps(Point location, TileFixture fixture) {
		return copyToSubMaps(location, fixture, true);
	}

	/**
	 * Copy the given fixture from the main map to subordinate maps. (It is
	 * found in the main map by ID, rather than trusting the input, unless
	 * it is animal tracks.) If it is a cache, remove it from the main map.
	 * If {@link zero}, remove sensitive information from the copies.
	 * Returns true if we think this changed anything in any of the sub-maps.
	 */
	boolean copyToSubMaps(Point location, TileFixture fixture, boolean zero);

	/**
	 * Copy terrain, including any mountain, rivers, and roads, from the
	 * main map to subordinate maps.
	 *
	 * // FIXME: Is this really necessary?
	 */
	void copyTerrainToSubMaps(Point location);

	/**
	 * Set sub-map terrain at the given location to the given type.
	 *
	 * @deprecated Can we redesign the fixture list to not need this for the exploration GUI?
	 */
	@Deprecated
	void setSubMapTerrain(Point location, @Nullable TileType terrain);

	/**
	 * Copy the given rivers to sub-maps, if they are present in the main map.
	 */
	void copyRiversToSubMaps(Point location, River... rivers);

	/**
	 * Remove the given rivers from sub-maps.
	 *
	 * @deprecated Can we redesign the fixture list to not need this for the exploration GUI?
	 */
	@Deprecated
	void removeRiversFromSubMaps(Point location, River... rivers);

	/**
	 * Remove the given fixture from sub-maps.
	 *
	 * @deprecated Can we redesign the fixture list to not need this for the exploration GUI?
	 */
	@Deprecated
	void removeFixtureFromSubMaps(Point location, TileFixture fixture);

	/**
	 * Set whether sub-maps have a mountain at the given location.
	 *
	 * @deprecated Can we redesign the fixture list to not need this for the exploration GUI?
	 */
	@Deprecated
	void setMountainousInSubMap(Point location, boolean mountainous);
}

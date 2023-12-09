package exploration.common;

import legacy.map.IFixture;

import java.util.Collection;
import java.util.List;

import legacy.map.fixtures.Ground;
import legacy.map.fixtures.resources.StoneDeposit;
import org.jetbrains.annotations.Nullable;
import drivers.common.IFixtureEditingModel;
import drivers.common.SelectionChangeSource;
import drivers.common.IMultiMapModel;
import legacy.map.Point;
import common.map.Player;
import legacy.map.Direction;
import legacy.map.River;
import legacy.map.TileFixture;
import legacy.map.TileType;
import legacy.map.fixtures.mobile.IUnit;

/**
 * A model for exploration apps.
 */
public interface IExplorationModel extends IMultiMapModel, SelectionChangeSource,
	MovementCostSource, IFixtureEditingModel {
	/**
	 * Players that are shared by all the maps.
	 */
	Collection<Player> getPlayerChoices();

	/**
	 * The given player's units in the main (master) map.
	 */
	List<IUnit> getUnits(Player player);

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
	Number move(Direction direction, Speed speed) throws TraversalImpossibleException;

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
	 * {@link Ground},
	 * {@link StoneDeposit}, or
	 * {legacy.map.fixtures.resources.MineralVein} at the location of that
	 * unit from unexposed to exposed (and discover it). This costs MP.
	 */
	void dig();

	/**
	 * Add the given unit at the given location.
	 */
	void addUnitAtLocation(IUnit unit, Point location);

	/**
	 * Copy the given fixture from the main map to subordinate maps. (It is
	 * found in the main map by ID, rather than trusting the input, unless
	 * it is animal tracks.) If it is a cache, remove it from the main map.
	 * Default to removing sensitive information from the copies.  Returns
	 * true if we think this changed anything in any of the sub-maps.
	 */
	default boolean copyToSubMaps(final Point location, final TileFixture fixture) {
		return copyToSubMaps(location, fixture, IFixture.CopyBehavior.ZERO);
	}

	/**
	 * Copy the given fixture from the main map to subordinate maps. (It is
	 * found in the main map by ID, rather than trusting the input, unless
	 * it is animal tracks.) If it is a cache, remove it from the main map.
	 * If "zero" is {@link IFixture.CopyBehavior#ZERO}, remove sensitive information from the copies.
	 * Returns true if we think this changed anything in any of the sub-maps.
	 */
	boolean copyToSubMaps(Point location, TileFixture fixture, IFixture.CopyBehavior zero);

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

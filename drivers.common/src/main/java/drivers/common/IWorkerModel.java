package drivers.common;

import java.util.Collection;

import org.jetbrains.annotations.Nullable;

import common.map.Player;

import common.map.fixtures.mobile.IUnit;
import common.map.fixtures.towns.IFortress;

/**
 * A driver model for the worker management app and the advancement app, aiming
 * to provide a hierarchical view of the units in the map and their members,
 * regardless of their location.
 */
public interface IWorkerModel extends IMultiMapModel, IAdvancementModel, IFixtureEditingModel {
	/**
	 * All the players in all the maps.
	 */
	Iterable<Player> getPlayers();

	/**
	 * The units in the map belonging to the given player.
	 * @param player The player whose units we want
	 */
	Collection<IUnit> getUnits(Player player);

	/**
	 * The units in the map of the given "kind" belonging to the given player.
	 * @param player The player whose units we want
	 * @param kind Which "kind" to restrict to
	 */
	Collection<IUnit> getUnits(Player player, String kind);

	/**
	 * The "kinds" of units that the given player has.
	 *
	 * @param player The player whose unit kinds we want
	 */
	Iterable<String> getUnitKinds(Player player);

	/**
	 * Get a unit by ID number.
	 */
	@Nullable IUnit getUnitByID(Player owner, int id);

	/**
	 * The player that the UI seems to be concerned with.
	 */
	Player getCurrentPlayer();

	/**
	 * The player that the UI seems to be concerned with.
	 */
	void setCurrentPlayer(Player currentPlayer);

	/**
	 * The fortresses belonging to the specified player.
	 *
	 * @param player The player whose fortresses we want
	 *
	 * TODO: Return their positions with them?
	 */
	Iterable<IFortress> getFortresses(Player player);
}

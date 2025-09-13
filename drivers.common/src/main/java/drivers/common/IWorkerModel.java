package drivers.common;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;


import legacy.map.Player;

import legacy.map.fixtures.mobile.IUnit;
import legacy.map.fixtures.towns.IFortress;
import org.jspecify.annotations.Nullable;

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
	 *
	 * @param player The player whose units we want
	 */
	Collection<IUnit> getUnits(Player player);

	/**
	 * A stream of the units in the map belonging to the given player.
	 *
	 * @param player The player whose units we want
	 */
	Stream<IUnit> streamUnits(Player player);

	/**
	 * The units in the map of the given "kind" belonging to the given player.
	 *
	 * @param player The player whose units we want
	 * @param kind   Which "kind" to restrict to
	 */
	Collection<IUnit> getUnits(Player player, String kind);

	/**
	 * The "kinds" of units that the given player has.
	 *
	 * @param player The player whose unit kinds we want
	 */
	List<String> getUnitKinds(Player player);

	/**
	 * Get a unit by ID number.
	 */
	@Nullable
	IUnit getUnitByID(Player owner, int id);

	/**
	 * The player that the UI seems to be concerned with.
	 */
	Player getCurrentPlayer();

	/**
	 * The player that the UI seems to be concerned with.
	 */
	void setCurrentPlayer(Player currentPlayer);

	/**
	 * The fortresses belonging to the specified player. In other model types we'd consider returning their locations
	 * with them, but the apps that use this model don't care.
	 *
	 * @param player The player whose fortresses we want
	 */
	Iterable<IFortress> getFortresses(Player player);
}

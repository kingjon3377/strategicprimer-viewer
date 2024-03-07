package drivers.turnrunning;

import legacy.map.IFixture;
import legacy.map.fixtures.LegacyQuantity;
import org.jetbrains.annotations.Nullable;

import legacy.map.ILegacyMap;

import java.util.Objects;
import java.util.function.IntSupplier;

import legacy.map.HasExtent;
import legacy.map.HasPopulation;
import legacy.map.Player;
import legacy.map.Point;
import legacy.map.TileFixture;

import exploration.common.IExplorationModel;

import java.math.BigDecimal;

import drivers.common.IAdvancementModel;

import legacy.map.fixtures.FortressMember;
import legacy.map.fixtures.IResourcePile;

import legacy.map.fixtures.mobile.IUnit;

import legacy.map.fixtures.towns.IFortress;

/**
 * A model for turn-running apps.
 */
public interface ITurnRunningModel extends IExplorationModel, IAdvancementModel {
	/**
	 * Add a copy of the given fixture to all submaps at the given location
	 * iff no fixture with the same ID is already there.
	 */
	void addToSubMaps(Point location, TileFixture fixture, IFixture.CopyBehavior zero);

	/**
	 * Reduce the population of a group of plants, animals, etc., and copy
	 * the reduced form into all subordinate maps.
	 */
	<T extends HasPopulation<? extends TileFixture> & TileFixture> void reducePopulation(
			Point location, T fixture, IFixture.CopyBehavior zero, int reduction);

	/**
	 * Reduce the acreage of a fixture, and copy the reduced form into all subordinate maps.
	 */
	<T extends HasExtent<? extends TileFixture> & TileFixture> void reduceExtent(
			Point location, T fixture, IFixture.CopyBehavior zero, BigDecimal reduction);

	/**
	 * Reduce the matching {@link IResourcePile resource}, in a {@link
	 * IUnit unit} or {@link IFortress fortress}
	 * owned by the specified player, by the
	 * specified amount. Returns true if any (mutable) resource piles
	 * matched in any of the maps, false otherwise.
	 */
	boolean reduceResourceBy(IResourcePile resource, BigDecimal amount, Player owner);

	/**
	 * Remove the given {@link IResourcePile resource} from a {@link IUnit
	 * unit} or {@link IFortress fortress} owned
	 * by the specified player in all maps. Returns true if
	 * any matched in any of the maps, false otherwise.
	 *
	 * @deprecated Use {@link #reduceResourceBy} when possible instead.
	 */
	@Deprecated
	boolean removeResource(IResourcePile resource, Player owner);

	/**
	 * Add a resource with the given ID, kind, contents, quantity, and
	 * created date in the given unit in all maps.  Returns true if a
	 * matching (and mutable) unit was found in at least one map, false
	 * otherwise.
	 */
	boolean addResource(IUnit container, int id, String kind, String contents, LegacyQuantity quantity,
						int createdDate);

	/**
	 * Add a resource with the given ID, kind, contents, quantity, and
	 * created date in the given fortress in all maps.  Returns true if a
	 * matching (and mutable) fortress was found in at least one map, false
	 * otherwise.
	 */
	boolean addResource(IFortress container, int id, String kind, String contents, LegacyQuantity quantity,
						int createdDate);

	/**
	 * Add a resource with the given ID, kind, contents, and quantity in
	 * the given unit in all maps.  Returns true if a matching (and
	 * mutable) unit was found in at least one map, false otherwise.
	 */
	boolean addResource(IUnit container, int id, String kind, String contents, LegacyQuantity quantity);

	/**
	 * Add a resource with the given ID, kind, contents, and quantity in
	 * the given fortress in all maps.  Returns true if a matching (and
	 * mutable) fortress was found in at least one map, false otherwise.
	 */
	boolean addResource(IFortress container, int id, String kind, String contents, LegacyQuantity quantity);

	/**
	 * Add (a copy of) an existing resource to the given player's HQ, or
	 * failing that to any fortress belonging to the given player, in all
	 * maps. Returns true if a matching (and mutable) fortress was found in
	 * at least one map, false otherwise.
	 *
	 * TODO: Make a way to add to units
	 */
	default boolean addExistingResource(final FortressMember resource, final Player owner) {
		return addExistingResource(resource, owner, "HQ");
	}

	/**
	 * Add (a copy of) an existing resource to the fortress belonging to
	 * the given player with the given name, or failing that to any
	 * fortress belonging to the given player, in all maps. Returns
	 * true if a matching (and mutable) fortress was found in at least one
	 * map, false otherwise.
	 *
	 * TODO: Make a way to add to units
	 */
	boolean addExistingResource(FortressMember resource, Player owner, String fortName);

	/**
	 * Add a non-talking animal population to the given unit in all maps.
	 * Returns true if the input makes sense and a matching (and mutable)
	 * unit was found in at least one map, false otherwise.
	 *
	 * Note the last two parameters are <em>reversed</em> from the {@link
	 * common.map.fixtures.mobile::AnimalImpl} constructor, to better fit
	 * the needs of <em>our</em> callers.
	 */
	boolean addAnimal(IUnit container, String kind, String status, int id, int population, int born);

	/**
	 * Add a non-talking animal population to the given unit in all maps.
	 * Returns true if the input makes sense and a matching (and mutable)
	 * unit was found in at least one map, false otherwise.
	 */
	default boolean addAnimal(final IUnit container, final String kind, final String status, final int id,
							  final int population) {
		return addAnimal(container, kind, status, id, population, -1);
	}

	/**
	 * Add a non-talking animal population to the given unit in all maps.
	 * Returns true if the input makes sense and a matching (and mutable)
	 * unit was found in at least one map, false otherwise.
	 */
	default boolean addAnimal(final IUnit container, final String kind, final String status, final int id) {
		return addAnimal(container, kind, status, id, 1);
	}

	/**
	 * Find the given player's HQ in the main map. If we can't find a
	 * {@link IFortress fortress} named "HQ", return <em>a</em> fortress of
	 * that player. If it can't find even one, return null.
	 */
	default @Nullable IFortress findHQ(final Player player) {
		return findHQ(player, "HQ");
	}

	/**
	 * Find the given player's HQ (or, rather, fortress with the given
	 * name) in the main map. If we can't find a {@link IFortress fortress}
	 * with the given name, return <em>a</em> fortress of that player. If
	 * it can't find even one, return null.
	 */
	default @Nullable IFortress findHQ(final Player player, final String fortressName) {
		IFortress retval = null;
		final ILegacyMap map = getMap();
		for (final IFortress fortress : map.streamAllFixtures()
				.filter(IFortress.class::isInstance).map(IFortress.class::cast)
				.filter(f -> player.equals(f.owner())).toList()) {
			if (fortressName.equals(fortress.getName())) {
				return fortress;
			} else if (Objects.isNull(retval)) {
				retval = fortress;
			}
		}
		return retval;
	}

	/**
	 * Transfer "quantity" units from "from" to (if
	 * not all of it) another resource in "to" in all maps.
	 * If this leaves any behind in any map, "id" will be called
	 * exactly once to generate the ID number for the resource in the
	 * destination in maps where that is the case. Returns true if a
	 * matching resource and destination are found (and the transfer
	 * occurs) in any map, false otherwise.
	 */
	boolean transferResource(IResourcePile from, IUnit to, BigDecimal quantity, IntSupplier id);

	/**
	 * Transfer "quantity" units from "from" to (if
	 * not all of it) another resource in "to" in all
	 * maps. If this leaves any behind in any map, "id" will be
	 * called exactly once to generate the ID number for the resource in
	 * the destination in maps where that is the case. Returns true if a
	 * matching resource and destination are found (and the transfer
	 * occurs) in any map, false otherwise.
	 */
	boolean transferResource(IResourcePile from, IFortress to, BigDecimal quantity, IntSupplier id);
}

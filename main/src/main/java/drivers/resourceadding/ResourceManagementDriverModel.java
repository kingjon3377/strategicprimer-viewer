package drivers.resourceadding;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.StreamSupport;
import java.util.stream.Collectors;

import legacy.map.fixtures.LegacyQuantity;
import lovelace.util.LovelaceLogger;
import org.jetbrains.annotations.Nullable;

import legacy.map.fixtures.towns.IMutableFortress;

import drivers.common.SimpleMultiMapModel;
import drivers.common.IDriverModel;

import legacy.map.fixtures.FortressMember;
import legacy.map.fixtures.IMutableResourcePile;
import legacy.map.fixtures.IResourcePile;
import legacy.map.fixtures.ResourcePileImpl;

import legacy.map.Player;
import legacy.map.IMutableLegacyMap;

import java.math.BigDecimal;

/**
 * A driver model for resource-entering drivers.
 */
/* package */ final class ResourceManagementDriverModel extends SimpleMultiMapModel {
	// TODO: Make these private and expose them as fromMap() and
	// fromDriverModel() static factory methods, as they were (in effect)
	// in Ceylon?
	public ResourceManagementDriverModel(final IMutableLegacyMap map) {
		super(map);
	}

	public ResourceManagementDriverModel(final IDriverModel driverModel) {
		super(driverModel);
	}

	/**
	 * All the players in all the maps.
	 */
	public Iterable<Player> getPlayers() {
		return streamAllMaps().flatMap(m -> StreamSupport.stream(m.getPlayers().spliterator(), false))
				.collect(Collectors.toSet());
	}

	/**
	 * Add a resource to a player's HQ.
	 *
	 * TODO: Use the "HQ or, failing that, any other fortress" algorithm used by other driver models.
	 */
	public void addResource(final FortressMember resource, final Player player) {
		final Predicate<Object> isFortress = IMutableFortress.class::isInstance;
		final Function<Object, IMutableFortress> fortressCast = IMutableFortress.class::cast;
		for (final IMutableLegacyMap map : getRestrictedAllMaps()) {
			final Player mapPlayer = map.getCurrentPlayer();
			// Operate on maps where the current player is independent OR matches
			// ("independent" also including negative ID)
			if (mapPlayer.isIndependent() || mapPlayer.getPlayerId() < 0 ||
					mapPlayer.getPlayerId() == player.getPlayerId()) {
				final IMutableFortress fortress = map.streamAllFixtures()
						.filter(isFortress)
						.map(fortressCast)
						.filter(f -> "HQ".equals(f.getName()))
						.filter(f -> mapPlayer.getPlayerId() ==
								f.owner().getPlayerId())
						.findAny().orElse(null);
				if (Objects.isNull(fortress)) {
					LovelaceLogger.warning("Didn't find HQ for %s", mapPlayer);
				} else {
					fortress.addMember(resource);
					map.setModified(true);
				}
			} else {
				LovelaceLogger.debug("Skipping map because current player isn't independent and doesn't match");
			}
		}
	}

	public IResourcePile addResourcePile(final Player player, final int id, final String kind, final String resource,
										 final BigDecimal quantity, final String units,
										 final @Nullable Integer created) {
		final IMutableResourcePile pile = new ResourcePileImpl(id, kind, resource,
				new LegacyQuantity(quantity, units));
		if (Objects.nonNull(created)) {
			pile.setCreated(created);
		}
		addResource(pile, player);
		return pile;
	}

	/**
	 * Get the current player. If none is current, returns null.
	 */
	public @Nullable Player getCurrentPlayer() {
		return StreamSupport.stream(getPlayers().spliterator(), false)
				.filter(Player::isCurrent).findAny().orElse(null);
	}
}

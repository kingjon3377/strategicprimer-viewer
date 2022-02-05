package drivers.resourceadding;

import java.util.stream.StreamSupport;
import java.util.stream.Collectors;
import org.jetbrains.annotations.Nullable;

import common.map.fixtures.towns.IMutableFortress;

import drivers.common.SimpleMultiMapModel;
import drivers.common.IDriverModel;

import common.map.fixtures.FortressMember;
import common.map.fixtures.IMutableResourcePile;
import common.map.fixtures.IResourcePile;
import common.map.fixtures.Quantity;
import common.map.fixtures.ResourcePileImpl;

import common.map.Player;
import common.map.IMutableMapNG;

import java.math.BigDecimal;

/**
 * A driver model for resource-entering drivers.
 */
/* package */ class ResourceManagementDriverModel extends SimpleMultiMapModel {
	// TODO: Make these private and expose them as fromMap() and
	// fromDriverModel() static factory methods, as they were (in effect)
	// in Ceylon?
	public ResourceManagementDriverModel(final IMutableMapNG map) {
		super(map);
	}

	public ResourceManagementDriverModel(final IDriverModel driverModel) {
		super(driverModel);
	}

	/**
	 * All the players in all the maps.
	 */
	public Iterable<Player> getPlayers() {
		return StreamSupport.stream(getAllMaps().spliterator(), false)
			.flatMap(m -> StreamSupport.stream(m.getPlayers().spliterator(), false))
			.collect(Collectors.toSet());
	}

	/**
	 * Add a resource to a player's HQ.
	 *
	 * TODO: Use the "HQ or, failing that, any other fortress" algorithm used by other driver models.
	 */
	public void addResource(final FortressMember resource, final Player player) {
		for (IMutableMapNG map : getRestrictedAllMaps()) {
			Player mapPlayer = map.getCurrentPlayer();
			// FIXME: It looks like this always skips the main map because the map player is independent there ...
			if (mapPlayer.isIndependent() || mapPlayer.getPlayerId() < 0 ||
					mapPlayer.getPlayerId() == player.getPlayerId()) {
				IMutableFortress fortress = map.streamAllFixtures()
						.filter(IMutableFortress.class::isInstance)
						.map(IMutableFortress.class::cast)
						.filter(f -> "HQ".equals(f.getName()))
						.filter(f -> mapPlayer.getPlayerId() ==
							f.getOwner().getPlayerId())
						.findAny().orElse(null);
				if (fortress != null) {
					fortress.addMember(resource);
					map.setModified(true);
				} // TODO: Else log why we're skipping the map
			} // TODO: Else log why we're skipping the map
		}
	}

	public IResourcePile addResourcePile(final Player player, final int id, final String kind, final String resource,
	                                     final BigDecimal quantity, final String units, @Nullable final Integer created) {
		IMutableResourcePile pile = new ResourcePileImpl(id, kind, resource,
			new Quantity(quantity, units));
		if (created != null) {
			pile.setCreated(created);
		}
		addResource(pile, player);
		return pile;
	}

	/**
	 * Get the current player. If none is current, returns null.
	 */
	@Nullable
	public Player getCurrentPlayer() {
		return StreamSupport.stream(getPlayers().spliterator(), false)
			.filter(Player::isCurrent).findAny().orElse(null);
	}
}

package model.resources;

import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import model.map.IMapNG;
import model.map.IMutableMapNG;
import model.map.Player;
import model.map.fixtures.FortressMember;
import model.map.fixtures.towns.Fortress;
import model.misc.IDriverModel;
import model.misc.SimpleMultiMapModel;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import util.Pair;

/**
 * A driver model for resource-entering drivers.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2015-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public class ResourceManagementDriver extends SimpleMultiMapModel {
	/**
	 * @param map  the first map
	 * @param file the file it was loaded from
	 */
	public ResourceManagementDriver(final IMutableMapNG map, final Optional<Path> file) {
		super(map, file);
	}

	/**
	 * @param driverModel a driver model to take our state from
	 */
	public ResourceManagementDriver(final IDriverModel driverModel) {
		super(driverModel);
	}

	/**
	 * @return the players to choose from
	 */
	public Iterable<Player> getPlayers() {
		return StreamSupport.stream(getAllMaps().spliterator(), false).flatMap(
				pair -> StreamSupport.stream(pair.first().players().spliterator(),
						false))
					   .collect(Collectors.toSet());
	}

	/**
	 * Add a resource to a player's HQ.
	 *
	 * @param resource the resource to add
	 * @param player   the player to add it for
	 */
	@SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
	public void addResource(@SuppressWarnings("TypeMayBeWeakened")
							final FortressMember resource, final Player player) {
		for (final Pair<@NonNull IMutableMapNG, Optional<Path>> pair : getAllMaps()) {
			final IMutableMapNG map = pair.first();
			final Player currP = map.getCurrentPlayer();
			if (currP.isIndependent() || (currP.getPlayerId() < 0) ||
						(currP.getPlayerId() == player.getPlayerId())) {
				addResourceToMap(resource.copy(false), map, player);
			}
		}
	}

	/**
	 * Add a resource to a player's HQ.
	 *
	 * @param resource the resource to add
	 * @param map      the map to add it in
	 * @param player   the player to add it for
	 */
	@SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
	public void addResourceToMap(final FortressMember resource, final IMapNG map,
								 final Player player) {
		map.locationStream().flatMap(map::streamOtherFixtures)
				.filter(Fortress.class::isInstance).map(Fortress.class::cast)
				.filter(fort -> "HQ".equals(fort.getName()) && (player.getPlayerId() ==
																		fort.getOwner()
																				.getPlayerId()))
				.forEach(fort -> fort.addMember(resource));
	}
	/**
	 * @return the current player, or null if there is none
	 */
	@Nullable
	public Player getCurrentPlayer() {
		return StreamSupport.stream(getPlayers().spliterator(), false)
				.filter(Player::isCurrent)
				.findAny().orElse(null);
	}
}

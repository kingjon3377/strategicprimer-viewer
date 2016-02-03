package model.resources;

import java.io.File;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import model.map.IMapNG;
import model.map.IMutableMapNG;
import model.map.Player;
import model.map.fixtures.FortressMember;
import model.map.fixtures.towns.Fortress;
import model.misc.IDriverModel;
import model.misc.SimpleMultiMapModel;
import util.Pair;

/**
 * A driver model for resource-entering drivers.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2015 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program. If not, see
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public class ResourceManagementDriver extends SimpleMultiMapModel {
	/**
	 * @param map  the first map
	 * @param file the file it was loaded from
	 */
	public ResourceManagementDriver(final IMutableMapNG map, final File file) {
		super(map, file);
	}

	/**
	 * @param dmodel a driver model to take our state from
	 */
	public ResourceManagementDriver(final IDriverModel dmodel) {
		super(dmodel);
	}

	/**
	 * @return the players to choose from
	 */
	public Iterable<Player> getPlayers() {
		return StreamSupport.stream(getAllMaps().spliterator(), false)
					   .flatMap(pair -> StreamSupport.stream(pair.first().players()
																	 .spliterator(),
							   false)).collect(
						Collectors.toSet());
	}

	/**
	 * Add a resource to a player's HQ.
	 *
	 * @param resource the resource to add
	 * @param player   the player to add it for
	 */
	public void addResource(@SuppressWarnings("TypeMayBeWeakened")
							final FortressMember resource, final Player player) {
		for (final Pair<IMutableMapNG, File> pair : getAllMaps()) {
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
	public void addResourceToMap(final FortressMember resource, final IMapNG map,
								 final Player player) {
		map.locationStream().flatMap(map::streamOtherFixtures)
				.filter(Fortress.class::isInstance).map(Fortress.class::cast)
				.filter(fort -> "HQ".equals(fort.getName()) && (player.getPlayerId() ==
						                                                fort.getOwner()
								                                                .getPlayerId()))
				.forEach(fort -> fort.addMember(resource));
	}
}

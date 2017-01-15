package model.workermgmt;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import model.map.IMutableMapNG;
import model.map.Player;
import model.map.Point;
import model.map.fixtures.mobile.IUnit;
import model.map.fixtures.mobile.ProxyFor;
import model.map.fixtures.mobile.ProxyUnit;
import model.map.fixtures.mobile.Unit;
import model.map.fixtures.towns.Fortress;
import model.misc.IDriverModel;
import model.misc.SimpleMultiMapModel;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import util.MultiMapHelper;
import util.Pair;
import view.util.SystemOut;

/**
 * A model to underlie the advancement GUI, etc.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2013-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public final class WorkerModel extends SimpleMultiMapModel implements IWorkerModel {
	/**
	 * Constructor.
	 *
	 * @param map  the map we're wrapping.
	 * @param file the file the map was loaded from or should be saved to
	 */
	public WorkerModel(final IMutableMapNG map, final Optional<Path> file) {
		super(map, file);
	}

	/**
	 * Copy constructor.
	 *
	 * @param model a driver model
	 */
	public WorkerModel(final IDriverModel model) {
		super(model);
	}

	/**
	 * Flatten and filter the stream to include only units, and only those owned by the
	 * given player.
	 * @param iter   a sequence of members of that type
	 * @param player a player
	 * @return a list of the members of the sequence that are units owned by the player
	 */
	private static Stream<IUnit> getUnits(final Stream<@NonNull ? super Unit> iter,
										  final Player player) {
		return iter.flatMap(item -> {
			if (item instanceof Fortress) {
				return ((Fortress) item).stream();
			} else {
				return Stream.of(item);
			}
		}).filter(IUnit.class::isInstance).map(IUnit.class::cast)
												 .filter(unit -> Objects.equals(
														 unit.getOwner(), player));
	}

	/**
	 * All the players in all the maps.
	 * TODO: Here and in IMapNG, should we have a stream-of-Players method?
	 * @return a list of all the players in all the maps
	 */
	@Override
	public List<Player> getPlayers() {
		return streamAllMaps().map(Pair::first).flatMap(
				map -> StreamSupport.stream(map.players().spliterator(), false))
					   .distinct().collect(Collectors.toList());
	}

	/**
	 * Get all the given player's units. TODO: Maybe return Stream instead in interface?
	 * @param player a player in the map
	 * @return a list of that player's units
	 */
	@Override
	public List<IUnit> getUnits(final Player player) {
		if (getSubordinateMaps().iterator().hasNext()) {
			return new ArrayList<>(streamAllMaps().map(Pair::first).flatMap(
					map -> map.locationStream().flatMap(
							point -> getUnits(map.streamOtherFixtures(point),
									player))).collect(
					(Supplier<TreeMap<Integer, IUnit>>) TreeMap::new,
					(retval, unit) -> ((ProxyFor<IUnit>) MultiMapHelper
																 .getMapValue(retval,
							Integer.valueOf(unit.getID()), ProxyUnit::new))
							.addProxied(unit), Map::putAll).values());
		} else {
			// Just in case I missed something in the proxy implementation, make
			// sure things work correctly when there's only one map.
			return getUnits(getMap().locationStream().flatMap(
					point -> getMap().streamOtherFixtures(point)), player)
						   .collect(Collectors.toList());
		}
	}

	/**
	 * Get all the "kinds" of units the player has. TODO: should this be a Stream?
	 * @param player a player in the map
	 * @return the "kinds" of unit that player has.
	 */
	@Override
	public List<String> getUnitKinds(final Player player) {
		return getUnits(player).stream().map(IUnit::getKind).distinct()
					   .collect(Collectors.toList());
	}

	/**
	 * Get all the player's units of the given kind. TODO: should this be a Stream?
	 * @param player a player in the map
	 * @param kind   a "kind" of unit.
	 * @return a list of the units of that kind in the map belonging to that player
	 */
	@Override
	public List<IUnit> getUnits(final Player player, final String kind) {
		return getUnits(player).stream().filter(unit -> kind.equals(unit.getKind()))
					   .collect(Collectors.toList());
	}

	/**
	 * Add a unit to all the maps, at the location of the player's HQ in the main map.
	 * @param unit the unit to add
	 */
	@Override
	public void addUnit(final IUnit unit) {
		for (final Point point : getMap().locations()) {
			if (getMap().streamOtherFixtures(point).filter(Fortress.class::isInstance)
						.map(Fortress.class::cast).anyMatch(
							fort -> "HQ".equals(fort.getName()) &&
											unit.getOwner().equals(fort.getOwner()))) {
				addUnitAtLocation(unit, point);
				return;
			}
		}
		SystemOut.SYS_OUT.println("No suitable location found");
	}

	/**
	 * Get a unit by its owner and ID.
	 * @param owner the unit's owner
	 * @param id    the ID # to search for
	 * @return the unit with that ID, or null if none.
	 */
	@Override
	public @Nullable IUnit getUnitByID(final Player owner, final int id) {
		final Optional<IUnit> retval =
				getUnits(owner).stream().filter(unit -> id == unit.getID()).findAny();
		return retval.orElse(null);
	}
	/**
	 * Add a unit at the given location in the given map.
	 * @param map the unit to work in
	 * @param unit the unit to add
	 * @param location where to add it
	 */
	private static void addUnitAtLocation(final IMutableMapNG map, final IUnit unit,
										  final Point location) {
		final Optional<Fortress> fort =
				map.streamOtherFixtures(location).filter(Fortress.class::isInstance)
						.map(Fortress.class::cast)
						.filter(fix -> unit.getOwner().equals(fix.getOwner())).findAny();
		if (fort.isPresent()) {
			fort.get().addMember(unit.copy(false));
		} else {
			map.addFixture(location, unit.copy(false));
		}
	}
	/**
	 * Add a unit at the given location in all maps.
	 *
	 * @param unit     the unit to add
	 * @param location where to add it
	 */
	@SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
	private void addUnitAtLocation(final IUnit unit, final Point location) {
		if (getSubordinateMaps().iterator().hasNext()) {
			for (final Pair<IMutableMapNG, Optional<Path>> pair : getAllMaps()) {
				addUnitAtLocation(pair.first(), unit, location);
			}
		} else {
			addUnitAtLocation(getMap(), unit, location);
		}
	}

	/**
	 * A trivial toString().
	 * @return a String representation of the object
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "WorkerModel";
	}
}

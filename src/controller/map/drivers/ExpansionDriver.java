package controller.map.drivers;

import controller.map.misc.ICLIHelper;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import model.exploration.SurroundingPointIterable;
import model.map.HasOwner;
import model.map.IFixture;
import model.map.IMapNG;
import model.map.IMutableMapNG;
import model.map.Player;
import model.map.Point;
import model.map.TileFixture;
import model.map.TileType;
import model.map.fixtures.Ground;
import model.map.fixtures.UnitMember;
import model.map.fixtures.mobile.IUnit;
import model.map.fixtures.mobile.SimpleMovement;
import model.map.fixtures.resources.CacheFixture;
import model.map.fixtures.terrain.Forest;
import model.map.fixtures.towns.ITownFixture;
import model.misc.IDriverModel;
import model.misc.IMultiMapModel;
import model.misc.SimpleMultiMapModel;
import org.eclipse.jdt.annotation.Nullable;
import util.ArraySet;
import util.NullCleaner;
import util.Pair;

/**
 * A driver to update a player's map to include a certain minimum distance around allied
 * villages.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2014-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public final class ExpansionDriver implements SimpleCLIDriver {
	/**
	 * An object indicating how to use and invoke this driver.
	 */
	private static final DriverUsage USAGE =
			new DriverUsage(false, "-n", "--expand", ParamCount.AtLeastTwo,
								   "Expand a player's map.",
								   "Ensure a player's map covers all terrain allied " +
										   "villages can see."

			);
	/**
	 * Logger.
	 */
	private static final Logger LOGGER =
			NullCleaner.assertNotNull(Logger.getLogger(ExpansionDriver.class.getName()));
	/**
	 * The exception to throw if our mock-object's expectations are violoated
	 */
	private static final IllegalStateException ISE =
			new IllegalStateException("Unsupported method called on mock object");

	static {
		USAGE.addSupportedOption("--current-turn=NN");
	}

	/**
	 * @param player a player
	 * @return a mock Unit that responds to getOwner() with that player and throws on
	 * any other method call
	 */
	private static IUnit mock(final Player player) {
		return new IUnit() {
			@Override
			public NavigableMap<Integer, String> getAllOrders() {
				throw ISE;
			}

			@Override
			public NavigableMap<Integer, String> getAllResults() {
				throw ISE;
			}

			@Override
			public String plural() {
				throw ISE;
			}

			@Override
			public String shortDesc() {
				throw ISE;
			}

			@Override
			public int getID() {
				throw ISE;
			}

			@Override
			public boolean equalsIgnoringID(final IFixture fix) {
				throw ISE;
			}

			@Override
			public int compareTo(final TileFixture fix) {
				throw ISE;
			}

			@Override
			public String getDefaultImage() {
				throw ISE;
			}

			@Override
			public String getImage() {
				throw ISE;
			}

			@Override
			public String getKind() {
				throw ISE;
			}

			@Override
			public Iterator<UnitMember> iterator() {
				throw ISE;
			}

			@Override
			public String getName() {
				throw ISE;
			}

			@Override
			public Player getOwner() {
				return player;
			}

			@Override
			public boolean isSubset(final IFixture obj, final Formatter ostream,
									final String context) {
				throw ISE;
			}

			@Override
			public String getOrders(final int turn) {
				throw ISE;
			}

			@Override
			public void setOrders(final int turn, final String newOrders) {
				throw ISE;
			}

			@Override
			public String getResults(final int turn) {
				throw ISE;
			}

			@Override
			public void setResults(final int turn, final String newOrders) {
				throw ISE;
			}

			@Override
			public String verbose() {
				throw ISE;
			}

			@Override
			public void addMember(final UnitMember member) {
				throw ISE;
			}

			@Override
			public void removeMember(final UnitMember member) {
				throw ISE;
			}

			@Override
			public IUnit copy(final boolean zero) {
				throw ISE;
			}

			@Override
			public boolean equals(@Nullable final Object obj) {
				throw ISE;
			}

			@Override
			public int hashCode() {
				throw ISE;
			}
		};
	}

	/**
	 * @param master the master map
	 * @param map    a player's map, to be expanded
	 */
	private static void expand(final IMapNG master, final IMutableMapNG map) {
		final Player currentPlayer = map.getCurrentPlayer();
		final Collection<Point> villagePoints = map.locationStream()
														.filter(point ->
																		containsSwornVillage(
																				master,
																				point,
																				currentPlayer))
														.collect(Collectors.toSet());
		final Map<Point, Set<TileFixture>> fixAdditions = new HashMap<>();
		final Map<Point, TileType> terrainAdditions = new HashMap<>();
		final IUnit mock = mock(currentPlayer);
		for (final Point point : villagePoints) {
			addSurroundingTerrain(point, master, map, terrainAdditions);
			addSurroundingFixtures(point, master, fixAdditions, mock);
		}
		for (final Map.Entry<Point, TileType> entry : terrainAdditions.entrySet()) {
			if (entry == null) {
				continue;
			}
			map.setBaseTerrain(NullCleaner.assertNotNull(entry.getKey()),
					NullCleaner.assertNotNull(entry.getValue()));
		}
		for (final Map.Entry<Point, Set<TileFixture>> entry : fixAdditions.entrySet()) {
			if (entry == null) {
				continue;
			}
			final Point point = NullCleaner.assertNotNull(entry.getKey());
			for (final TileFixture fix : entry.getValue()) {
				if (fix instanceof HasOwner) {
					map.addFixture(point,
							fix.copy(!((HasOwner) fix).getOwner().equals
																		  (currentPlayer)));
				} else {
					map.addFixture(point, fix.copy(true));
				}
			}
		}
	}

	/**
	 * @param point     a location
	 * @param master    the master map
	 * @param additions a collection of additions to make (by which they are returned)
	 * @param owned     a "unit" (probably a mock-object) indicating the player we're
	 *                  concerned with.
	 */
	@SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
	private static void addSurroundingFixtures(final Point point,
											   final IMapNG master,
											   final Map<Point, Set<TileFixture>>
													   additions,
											   final IUnit owned) {
		final List<TileFixture> possibilities = new ArrayList<>();
		for (final Point neighbor : new SurroundingPointIterable(point,
																		master
																				.dimensions())) {
			final Set<TileFixture> neighborFixtures =
					getSetFromMap(additions, neighbor);
			possibilities.clear();
			final Ground ground = master.getGround(neighbor);
			final Forest forest = master.getForest(neighbor);
			if (ground != null) {
				possibilities.add(ground);
			}
			if (forest != null) {
				possibilities.add(forest);
			}
			for (final TileFixture fix : master.getOtherFixtures(neighbor)) {
				if (neighborFixtures.contains(fix)) {
					continue;
				} else if (SimpleMovement.shouldAlwaysNotice(owned, fix)) {
					neighborFixtures.add(fix);
				} else if (SimpleMovement.shouldSometimesNotice(owned, fix)
								   && !(fix instanceof CacheFixture)) {
					possibilities.add(fix);
				}
			}
			if (!possibilities.isEmpty()) {
				Collections.shuffle(possibilities);
				neighborFixtures.add(possibilities.get(0));
			}
		}
	}

	/**
	 * @param map a mapping from a key to a set of values
	 * @param key the key to query the map for
	 * @param <K> the type of the key
	 * @param <V> the type of members of the set
	 * @return the value at the key, or a new ArraySet (which is added) if there is no
	 * value there yet.
	 */
	private static <K, V> Set<V> getSetFromMap(final Map<K, Set<V>> map, final K key) {
		if (map.containsKey(key)) {
			return NullCleaner.assertNotNull(map.get(key));
		} else {
			final Set<V> retval = new ArraySet<>();
			map.put(key, retval);
			return retval;
		}
	}

	/**
	 * @param point     a location
	 * @param master    the master map
	 * @param map       a player's map
	 * @param additions a collection of additions to make (by which they are returned)
	 */
	@SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
	private static void addSurroundingTerrain(final Point point, final IMapNG master,
											  final IMutableMapNG map,
											  final Map<Point, TileType> additions) {
		for (final Point neighbor : new SurroundingPointIterable(point,
																		map.dimensions()
		)) {
			if (!additions.containsKey(neighbor)
						&& (TileType.NotVisible == map.getBaseTerrain(neighbor))) {
				additions.put(neighbor, master.getBaseTerrain(neighbor));
				if (master.isMountainous(neighbor)) {
					map.setMountainous(neighbor, true);
				}
			}
		}
	}

	/**
	 * @param map    a map
	 * @param point  a point in the map
	 * @param player a player
	 * @return whether there is a village or town at that location that belongs to that
	 * player
	 */
	private static boolean containsSwornVillage(final IMapNG map, final Point point,
												final Player player) {
		return map.streamOtherFixtures(point).anyMatch(
				fix -> (fix instanceof ITownFixture) &&
							   ((HasOwner) fix).getOwner().equals(player));
	}

	/**
	 * @return an object indicating how to use and invoke this driver.
	 */
	@Override
	public DriverUsage usage() {
		return USAGE;
	}

	/**
	 * @return a String representation of the object
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "ExpansionDriver";
	}

	/**
	 * Run the driver.
	 *
	 * @param cli the interface for user I/O
	 * @param options command-line options passed in
	 * @param model   the driver model
	 */
	@Override
	public void startDriver(final ICLIHelper cli, final SPOptions options,
							final IDriverModel model) {
		final IMultiMapModel mapModel;
		if (model instanceof IMultiMapModel) {
			mapModel = (IMultiMapModel) model;
		} else {
			LOGGER.warning(
					"Expansion on a master map with no subordinate maps does nothing");
			mapModel = new SimpleMultiMapModel(model);
		}
		for (final Pair<IMutableMapNG, Optional<Path>> pair : mapModel
																	  .getSubordinateMaps()) {
			expand(mapModel.getMap(), pair.first());
		}
	}
}

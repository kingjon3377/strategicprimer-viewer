package controller.map.drivers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.stream.XMLStreamException;

import org.eclipse.jdt.annotation.Nullable;

import controller.map.drivers.ISPDriver.DriverUsage.ParamCount;
import controller.map.formatexceptions.MapVersionException;
import controller.map.formatexceptions.SPFormatException;
import controller.map.misc.MapReaderAdapter;
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
import util.ArraySet;
import util.NullCleaner;
import util.Warning;

/**
 * A driver to update a player's map to include a certain minimum distance
 * around allied villages.
 *
 * This is part of the Strategic Primer assistive programs suite developed by
 * Jonathan Lovelace.
 *
 * Copyright (C) 2014-2015 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of version 3 of the GNU General Public License as published by the
 * Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * @author Jonathan Lovelace
 *
 */
public class ExpansionDriver implements ISPDriver {
	/**
	 * An object indicating how to use and invoke this driver.
	 */
	private static final DriverUsage USAGE_OBJ = new DriverUsage(false, "-n",
			"--expand", ParamCount.Many, "Expand a player's map.",
			"Ensure a player's map covers all terrain allied villages can see.",
			ExpansionDriver.class);

	/**
	 * @return an object indicating how to use and invoke this driver.
	 */
	@Override
	public DriverUsage usage() {
		return USAGE_OBJ;
	}

	/**
	 * @return what to call the driver in a CLI list.
	 */
	@Override
	public String getName() {
		return usage().getShortDescription();
	}

	/**
	 * @param nomen ignored
	 */
	@Override
	public void setName(final String nomen) {
		throw new IllegalStateException("Can't rename a driver");
	}
	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "ExpansionDriver";
	}
	/**
	 * Run the driver.
	 *
	 * @param args command-line arguments
	 * @throws DriverFailedException on error
	 */
	@Override
	public void startDriver(final String... args) throws DriverFailedException {
		if (args.length < 2) {
			System.err.println("Usage: ExpansionDriver master player [player ...]");
			throw new DriverFailedException("Not enough arguments",
					new IllegalArgumentException("Need at least two arguments"));
		}
		final File masterFile = new File(args[0]);
		final IMapNG masterMap;
		final MapReaderAdapter reader = new MapReaderAdapter();
		try {
			masterMap = reader.readMap(masterFile, Warning.INSTANCE);
		} catch (final MapVersionException except) {
			throw new DriverFailedException("Unsupported map version", except);
		} catch (final IOException except) {
			throw new DriverFailedException(
					"I/O error reading file " + masterFile.getPath(), except);
		} catch (final XMLStreamException except) {
			throw new DriverFailedException("Malformed XML", except);
		} catch (final SPFormatException except) {
			throw new DriverFailedException("SP map format error", except);
		}
		for (final String arg : args) {
			if (arg == null || arg.equals(args[0])) {
				continue;
			}
			final File file = new File(arg);
			final IMapNG map;
			try {
				map = reader.readMap(file, Warning.INSTANCE);
			} catch (final MapVersionException except) {
				throw new DriverFailedException("Unsupported map version", except);
			} catch (final IOException except) {
				throw new DriverFailedException(
						"I/O error reading file " + file.getPath(), except);
			} catch (final XMLStreamException except) {
				throw new DriverFailedException("Malformed XML", except);
			} catch (final SPFormatException except) {
				throw new DriverFailedException("SP map format error", except);
			}
			if (expand(masterMap, map)) {
				try {
					reader.write(file, map);
				} catch (IOException except) {
					throw new DriverFailedException("I/O error writing file "
							+ file.getPath(), except);
				}
			}
		}
	}

	/**
	 * @param master
	 *            the master map
	 * @param map
	 *            a player's map, to be expanded
	 * @return true if the operation succeeded, false if the player's map was
	 *         immutable
	 */
	private static boolean expand(final IMapNG master, final IMapNG map) {
		if (!(map instanceof IMutableMapNG)) {
			return false;
		}
		IMutableMapNG lmap = (IMutableMapNG) map;
		final Player player = map.getCurrentPlayer();
		final Map<Point, TileType> terrainAdditions = new HashMap<>();
		final Map<Point, Set<TileFixture>> fixAdditions = new HashMap<>();
		final IllegalStateException ise =
				new IllegalStateException(
						"Unsupported method called on mock object");
		final IUnit mock = new IUnit() {
			@Override
			public int getZValue() {
				throw ise;
			}
			@Override
			public String plural() {
				throw ise;
			}
			@Override
			public String shortDesc() {
				throw ise;
			}
			@Override
			public int getID() {
				throw ise;
			}
			@Override
			public boolean equalsIgnoringID(final IFixture fix) {
				throw ise;
			}
			@Override
			public int compareTo(@Nullable final TileFixture o) {
				throw ise;
			}
			@Override
			public String getDefaultImage() {
				throw ise;
			}
			@Override
			public void setImage(final String image) {
				throw ise;
			}
			@Override
			public String getImage() {
				throw ise;
			}
			@Override
			public String getKind() {
				throw ise;
			}
			@Override
			public void setKind(final String nKind) {
				throw ise;
			}
			@Override
			public Iterator<UnitMember> iterator() {
				throw ise;
			}
			@Override
			public String getName() {
				throw ise;
			}
			@Override
			public void setName(final String nomen) {
				throw ise;
			}
			@Override
			public Player getOwner() {
				return player;
			}
			@Override
			public void setOwner(final Player playr) {
				throw ise;
			}
			@Override
			public boolean isSubset(final IUnit obj, final Appendable ostream,
					final String context) throws IOException {
				throw ise;
			}
			@Override
			public String getOrders() {
				throw ise;
			}
			@Override
			public void setOrders(final String newOrders) {
				throw ise;
			}
			@Override
			public String verbose() {
				throw ise;
			}
			@Override
			public void addMember(final UnitMember member) {
				throw ise;
			}
			@Override
			public void removeMember(final UnitMember member) {
				throw ise;
			}
			@Override
			public IUnit copy(final boolean zero) {
				throw ise;
			}
		};
		final Set<Point> villagePoints = new ArraySet<>();
		for (final Point point : lmap.locations()) {
			if (point != null && containsSwornVillage(master, point, player)) {
				villagePoints.add(point);
			}
		}
		for (final Point point : villagePoints) {
			if (point == null) {
				continue;
			}
			addSurroundingTerrain(point, master, lmap, terrainAdditions);
			addSurroundingFixtures(point, master, fixAdditions, mock);
		}
		for (final Map.Entry<Point, TileType> entry : terrainAdditions
				.entrySet()) {
			if (entry == null) {
				continue;
			}
			lmap.setBaseTerrain(NullCleaner.assertNotNull(entry.getKey()),
					NullCleaner.assertNotNull(entry.getValue()));
		}
		for (final Map.Entry<Point, Set<TileFixture>> entry : fixAdditions
				.entrySet()) {
			if (entry == null) {
				continue;
			}
			for (final TileFixture fix : entry.getValue()) {
				lmap.addFixture(NullCleaner.assertNotNull(entry.getKey()),
						NullCleaner.assertNotNull(fix));
			}
		}
		return true;
	}

	/**
	 * @param point
	 *            a location
	 * @param master
	 *            the master map
	 * @param additions
	 *            a collection of additions to make (by which they are returned)
	 * @param owned
	 *            a "unit" (probably a mock-object) indicating the player we're
	 *            concerned with.
	 */
	private static void addSurroundingFixtures(final Point point,
			final IMapNG master, final Map<Point, Set<TileFixture>> additions,
			final IUnit owned) {
		final List<TileFixture> possibilities = new ArrayList<>();
		for (final Point neighbor : new SurroundingPointIterable(point,
				master.dimensions())) {
			if (neighbor == null) {
				continue;
			}
			final Set<TileFixture> neighborFixtures =
					getSetFromMap(additions, neighbor);
			possibilities.clear();
			Ground ground = master.getGround(neighbor);
			Forest forest = master.getForest(neighbor);
			if (ground != null) {
				possibilities.add(ground);
			}
			if (forest != null) {
				possibilities.add(forest);
			}
			for (final TileFixture fix : master.getOtherFixtures(neighbor)) {
				if (fix == null || neighborFixtures.contains(fix)) {
					continue;
				} else if (SimpleMovement.shouldAlwaysNotice(owned, fix)) {
					neighborFixtures.add(fix);
				} else if (SimpleMovement.mightNotice(owned, fix)
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
	 * @param map
	 *            a mapping from a key to a set of values
	 * @param key
	 *            the key to query the map for
	 * @return the value at the key, or a new ArraySet (which is added) if there
	 *         is no value there yet.
	 * @param <K> the type of the key
	 * @param <V> the type of members of the set
	 */
	private static <K, V> Set<V> getSetFromMap(final Map<K, Set<V>> map,
			final K key) {
		Set<V> retval = map.get(key);
		if (retval == null) {
			retval = new ArraySet<>();
			map.put(key, retval);
		}
		return retval;
	}
	/**
	 * @param point
	 *            a location
	 * @param master
	 *            the master map
	 * @param map
	 *            a player's map
	 * @param additions
	 *            a collection of additions to make (by which they are returned)
	 */
	private static void addSurroundingTerrain(final Point point, final IMapNG master,
			final IMutableMapNG map, final Map<Point, TileType> additions) {
		for (final Point neighbor : new SurroundingPointIterable(point,
				map.dimensions())) {
			if (neighbor == null) {
				continue;
			}
			if (!additions.containsKey(neighbor)
					&& TileType.NotVisible.equals(map.getBaseTerrain(neighbor))) {
				additions.put(neighbor, master.getBaseTerrain(neighbor));
				if (master.isMountainous(neighbor)) {
					map.setMountainous(neighbor, true);
				}
			}
		}
	}
	/**
	 * @param map
	 *            a map
	 * @param point
	 *            a point in the map
	 * @param player
	 *            a player
	 * @return whether there is a village or town at that location that belongs
	 *         to that player
	 */
	private static boolean containsSwornVillage(final IMapNG map, final Point point,
			final Player player) {
		for (final TileFixture fix : map.getOtherFixtures(point)) {
			if (fix instanceof ITownFixture
					&& ((HasOwner) fix).getOwner().equals(player)) {
				return true;
			}
		}
		return false;
	}
}

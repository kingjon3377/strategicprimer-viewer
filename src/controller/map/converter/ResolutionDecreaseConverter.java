package controller.map.converter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import model.map.IMapNG;
import model.map.MapDimensions;
import model.map.Player;
import model.map.PlayerCollection;
import model.map.Point;
import model.map.PointFactory;
import model.map.River;
import model.map.SPMapNG;
import model.map.TileFixture;
import model.map.TileType;
import model.map.fixtures.Ground;
import model.map.fixtures.RiverFixture;
import model.map.fixtures.terrain.Forest;
import util.EnumCounter;
import util.NullCleaner;

/**
 * A class to convert a map to an equivalent half-resolution one.
 *
 * This is part of the Strategic Primer assistive programs suite developed by
 * Jonathan Lovelace.
 *
 * Copyright (C) 2012-2015 Jonathan Lovelace
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
public class ResolutionDecreaseConverter {
	/**
	 * Convert a map. It needs to have an even number of rows and columns.
	 *
	 * @param old the map to convert.
	 * @return an equivalent MapView.
	 */
	public static SPMapNG convert(final IMapNG old) {
		checkRequirements(old);
		final int newRows = old.dimensions().rows / 2;
		final int newCols = old.dimensions().cols / 2;
		final PlayerCollection players = new PlayerCollection();
		for (final Player player : old.players()) {
			if (player != null) {
				players.add(player);
			}
		}
		final SPMapNG retval =
				new SPMapNG(new MapDimensions(newRows, newCols, 2), players,
						old.getCurrentTurn());
		for (int row = 0; row < newRows; row++) {
			for (int col = 0; col < newCols; col++) {
				final Point point = PointFactory.point(row, col);
				Point one = PointFactory.point(row * 2, col * 2);
				Point two = PointFactory.point(row * 2, col * 2 + 1);
				Point three = PointFactory.point(row * 2 + 1, col * 2);
				Point four = PointFactory.point(row * 2 + 1, col * 2 + 1);
				retval.setBaseTerrain(
						point,
						consensus(old.getBaseTerrain(one),
								old.getBaseTerrain(two),
								old.getBaseTerrain(three),
								old.getBaseTerrain(four)));
				List<Point> oldPoints = new ArrayList<>();
				oldPoints.add(one);
				oldPoints.add(two);
				oldPoints.add(three);
				oldPoints.add(four);
				for (Point oldPoint : oldPoints) {
					if (oldPoint == null) {
						continue;
					} else if (old.isMountainous(oldPoint)) {
						retval.setMountainous(point, true);
					}
					Ground ground = old.getGround(oldPoint);
					if (ground != null) {
						if (retval.getGround(point) == null) {
							retval.setGround(point, ground);
						} else {
							retval.addFixture(point, ground);
						}
					}
					Forest forest = old.getForest(oldPoint);
					if (forest != null) {
						if (retval.getForest(point) == null) {
							retval.setForest(point, forest);
						} else {
							retval.addFixture(point, forest);
						}
					}
					for (TileFixture fixture : old.getOtherFixtures(oldPoint)) {
						if (fixture == null) {
							continue;
						}
						retval.addFixture(point, fixture);
					}
					final Set<River> upperLeftRivers = getRivers(old, one);
					final Set<River> upperRightRivers = getRivers(old, two);
					final Set<River> lowerLeftRivers = getRivers(old, three);
					final Set<River> lowerRightRivers = getRivers(old, four);
					final RiverFixture combined = new RiverFixture();
					removeRivers(upperLeftRivers, River.East, River.South);
					removeRivers(upperRightRivers, River.West, River.South);
					removeRivers(lowerLeftRivers, River.East, River.North);
					removeRivers(lowerRightRivers, River.West, River.North);
					addRivers(combined, upperLeftRivers, upperRightRivers,
							lowerLeftRivers, lowerRightRivers);
					for (River river : combined) {
						retval.addRivers(point, river);
					}
					// FIXME: Rivers
				}
			}
		}
		return retval;
	}

	/**
	 * Check that the map has an even number of rows and columns.
	 *
	 * @param map the map to check.
	 */
	private static void checkRequirements(final IMapNG map) {
		if (map.dimensions().rows % 2 != 0
				|| map.dimensions().cols % 2 != 0) {
			throw new IllegalArgumentException(
					"Can only convert maps with even numbers of rows and columns.");
		}
	}

	/**
	 * @param old a map
	 * @param point a point
	 * @return the rivers there, if any
	 */
	private static Set<River> getRivers(final IMapNG old, final Point point) {
		final Set<River> retval =
				NullCleaner.assertNotNull(EnumSet.noneOf(River.class));
		for (final River river : old.getRivers(point)) {
			retval.add(river);
		}
		return retval;
	}

	/**
	 * @param fix a RiverFixture
	 * @param rivers a series of rivers to add to it
	 */
	@SafeVarargs
	private static void addRivers(final RiverFixture fix,
			final Iterable<River>... rivers) {
		for (final Iterable<River> riverFix : rivers) {
			for (final River river : riverFix) {
				if (river != null) {
					fix.addRiver(river);
				}
			}
		}
	}

	/**
	 * @param set a set of rivers
	 * @param rivers a series of rivers to remove from it
	 */
	private static void removeRivers(final Set<River> set,
			final River... rivers) {
		for (final River river : rivers) {
			if (river != null) {
				set.remove(river);
			}
		}
	}

	/**
	 * @param one one tile-type
	 * @param two a second tile-type
	 * @param three a third tile-type
	 * @param four a fourth tile-type
	 * @return the most common tile of them, or if there are two or four with
	 *         equal representation one selected from among them at random.
	 */
	private static TileType consensus(final TileType one, final TileType two,
			final TileType three, final TileType four) {
		final EnumCounter<TileType> counter = new EnumCounter<>(TileType.class);
		counter.countMany(one, two, three, four);
		final Set<TileType> twos = EnumSet.noneOf(TileType.class);
		for (final TileType type : TileType.values()) {
			assert type != null;
			switch (counter.getCount(type)) {
			case 0:
				// skip
				break;
			case 1:
				// skip
				break;
			case 2:
				twos.add(type);
				break;
			default:
				return type; // NOPMD
			}
		}
		if (twos.size() == 1) {
			return NullCleaner.assertNotNull(twos.iterator().next()); // NOPMD
		} else {
			final List<TileType> list = Arrays.asList(one, two, three, four);
			Collections.shuffle(list);
			return NullCleaner.assertNotNull(list.get(0));
		}
	}

	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "ResolutionDecreaseConverter";
	}
}

package model.map.fixtures.mobile;

import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import model.map.HasOwner;
import model.map.IEvent;
import model.map.TileFixture;
import model.map.TileType;
import model.map.fixtures.Ground;
import model.map.fixtures.RiverFixture;
import model.map.fixtures.UnitMember;
import model.map.fixtures.mobile.worker.IJob;
import model.map.fixtures.mobile.worker.ISkill;
import model.map.fixtures.mobile.worker.WorkerStats;
import model.map.fixtures.terrain.Forest;
import model.map.fixtures.terrain.Hill;
import model.map.fixtures.terrain.Mountain;
import model.map.fixtures.towns.Fortress;
import org.eclipse.jdt.annotation.Nullable;
import util.EqualsAny;

/**
 * A class encapsulating knowledge about movement costs associated with various tile
 * types. FIXME: This ought to be per-unit-type, rather than one centralized set of
 * figures.
 *
 * TODO: tests
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2012-2015 Jonathan Lovelace
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
@SuppressWarnings("UtilityClassCanBeEnum")
public final class SimpleMovement {
	/**
	 * Do not instantiate.
	 */
	private SimpleMovement() {
		// Do not instantiate.
	}

	/**
	 * An exception thrown to signal traversal is impossible.
	 *
	 * FIXME: Ocean isn't impassable to everything, of course.
	 *
	 * @author Jonathan Lovelace
	 */
	public static final class TraversalImpossibleException extends Exception {
		/**
		 * Constructor.
		 */
		public TraversalImpossibleException() {
			super("Traversal is impossible.");
		}
	}

	/**
	 * @param terrain a terrain type
	 * @return whether it's passable by land movement.
	 */
	public static boolean isLandMovementPossible(final TileType terrain) {
		return TileType.Ocean != terrain;
	}

	/**
	 * @param terrain  a terrain type
	 * @param forest   whether the location is forested
	 * @param mountain whether the location is mountainous
	 * @param river    whether the location has a river
	 * @param fixtures the fixtures at the location
	 * @return the movement cost to traverse the location.
	 */
	public static int getMovementCost(final TileType terrain,
	                                  final boolean forest, final boolean mountain,
	                                  final boolean river,
	                                  final Supplier<Stream<TileFixture>> fixtures) {
		if ((TileType.Ocean == terrain)
				    || (TileType.NotVisible == terrain)) {
			return Integer.MAX_VALUE; // NOPMD
		} else if (forest || mountain || isForest(fixtures.get()) ||
				           isHill(fixtures.get()) || (TileType.Desert == terrain)) {
			if (river) {
				return 2; // NOPMD
			} else {
				return 3; // NOPMD
			}
		} else if (TileType.Jungle == terrain) {
			if (river) {
				return 4;
			} else {
				return 6; // NOPMD
			}
		} else if (EqualsAny.equalsAny(terrain, TileType.Steppe,
				TileType.Plains, TileType.Tundra)) {
			if (river) {
				return 1;
			} else {
				return 2;
			}
		} else {
			throw new IllegalArgumentException("Unknown tile type");
		}
	}

	/**
	 * @param fixtures a sequence of fixtures
	 * @return whether any of them is a forest
	 */
	private static boolean isForest(final Stream<TileFixture> fixtures) {
		return StreamSupport.stream(fixtures.spliterator(), false)
				       .anyMatch(fix -> fix instanceof Forest);
	}

	/**
	 * @param fixtures a sequence of fixtures
	 * @return whether any of them is a mountain or a hill
	 */
	private static boolean isHill(final Stream<TileFixture> fixtures) {
		return StreamSupport.stream(fixtures.spliterator(), false)
				       .anyMatch(fix -> (fix instanceof Mountain) ||
						                        (fix instanceof Hill));
	}

	/**
	 * FIXME: *Some* explorers *would* notice even unexposed ground.
	 *
	 * TODO: We now check DCs on Events, but ignore relevant skills other than Perception
	 *
	 * @param unit a unit
	 * @param fix  a fixture
	 * @return whether the unit might notice it. Units do not notice themselves, and do
	 * not notice unexposed ground, and do not notice null fixtures.
	 */
	public static boolean shouldSometimesNotice(final IUnit unit,
	                                            @Nullable final TileFixture fix) {
		if (fix instanceof Ground) {
			if (((Ground) fix).isExposed()) {
				return true;
			} else {
				return false;
			}
		} else if (unit.equals(fix)) {
			return true;
		} else if (fix instanceof IEvent) {
			if (getHighestPerception(unit) + 15 >= ((IEvent) fix).getDC()) {
				return true;
			} else {
				return false;
			}
		} else {
			return true;
		}
	}
	/**
	 * TODO: This does not properly handle the unusual case of a very unobservant unit.
	 * @param unit a unit
	 * @return the highest Perception score of any member, or 0 if no members
	 */
	private static int getHighestPerception(final IUnit unit) {
		int retval = 0;
		for (final UnitMember member : unit) {
			if (member instanceof IWorker) {
				final int percep = getPerception((IWorker) member);
				if (percep > retval) {
					retval = percep;
				}
			}
		}
		return retval;
	}
	/**
	 * @param worker a worker
	 * @return the worker's Perception score
	 */
	private static int getPerception(final IWorker worker) {
		final int ability;
		final WorkerStats stats;
		if (worker instanceof Worker) {
			stats = ((Worker) worker).getStats();
		} else {
			stats = null;
		}
		if (stats != null) {
			// TODO: Make helper method instead of duplicating the arithmetic from getModifierString
			ability = (stats.getWisdom() - 10) / 2;
		} else {
			ability = 0;
		}
		int ranks = 0;
		for (final IJob job : worker) {
			for (final ISkill skill : job) {
				if ("perception".equalsIgnoreCase(skill.getName())) {
					ranks += skill.getLevel();
				}
			}
		}
		return ability + ranks * 2;
	}
	/**
	 * @param unit a unit
	 * @param fix  a fixture
	 * @return whether the unit should always notice it. A null fixture is never noticed
	 */
	public static boolean shouldAlwaysNotice(final HasOwner unit,
	                                         @Nullable final TileFixture fix) {
		return (fix instanceof Mountain) || (fix instanceof RiverFixture)
				       || (fix instanceof Hill) || (fix instanceof Forest)
				       || ((fix instanceof Fortress)
						           &&
						           ((Fortress) fix).getOwner().equals(unit.getOwner()));
	}
}

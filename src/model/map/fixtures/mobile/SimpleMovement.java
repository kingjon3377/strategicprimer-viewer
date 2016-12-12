package model.map.fixtures.mobile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import model.exploration.IExplorationModel.Direction;
import model.exploration.IExplorationModel.Speed;
import model.map.HasOwner;
import model.map.IEvent;
import model.map.River;
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
import model.map.fixtures.towns.ITownFixture;
import org.eclipse.jdt.annotation.Nullable;
import util.EqualsAny;
import util.SingletonRandom;

/**
 * A class encapsulating knowledge about movement costs associated with various tile
 * types.
 *
 * FIXME: This ought to be per-unit-type, rather than one centralized set of figures.
 *
 * TODO: tests
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2012-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
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
	 * @param terrain a terrain type
	 * @return whether it's passable by land movement.
	 */
	public static boolean isLandMovementPossible(final TileType terrain) {
		return TileType.Ocean != terrain;
	}
	/**
	 * @param direction the direction of travel
	 * @param sourceRivers the rivers in the location the mover is traveling from
	 * @param destRivers the rivers in the location the mover is traveling to
	 * @return whether any of those rivers are in the direction of travel
	 */
	public static boolean doRiversApply(final Direction direction,
										final Iterable<River> sourceRivers,
										final Iterable<River> destRivers) {
		final BiPredicate<Iterable<River>, River> matches =
				(iter, river) -> StreamSupport.stream(iter.spliterator(), false)
										 .anyMatch(river::equals);
		final Predicate<Direction> recurse =
				dir -> doRiversApply(dir, sourceRivers, destRivers);
		switch (direction) {
		case North:
			return matches.test(sourceRivers, River.North) || matches.test(destRivers, River.South);
		case Northeast:
			return recurse.test(Direction.North) || recurse.test(Direction.East);
		case East:
			return matches.test(sourceRivers, River.East) || matches.test(destRivers, River.West);
		case Southeast:
			return recurse.test(Direction.South) || recurse.test(Direction.East);
		case South:
			return matches.test(sourceRivers, River.South) || matches.test(destRivers, River.North);
		case Southwest:
			return recurse.test(Direction.South) || recurse.test(Direction.West);
		case West:
			return matches.test(sourceRivers, River.West) || matches.test(destRivers, River.East);
		case Northwest:
			return recurse.test(Direction.North) || recurse.test(Direction.West);
		case Nowhere:
			break;
		}
		return false;
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
		if ((TileType.Ocean == terrain) || (TileType.NotVisible == terrain)) {
			return Integer.MAX_VALUE;
		} else if (forest || mountain || isForest(fixtures.get()) ||
						   isHill(fixtures.get()) || (TileType.Desert == terrain)) {
			if (river) {
				return 2;
			} else {
				return 3;
			}
		} else if (TileType.Jungle == terrain) {
			if (river) {
				return 4;
			} else {
				return 6;
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
	@SuppressWarnings("TypeMayBeWeakened")
	private static boolean isForest(final Stream<TileFixture> fixtures) {
		return StreamSupport.stream(fixtures.spliterator(), false)
					   .anyMatch(fix -> fix instanceof Forest);
	}

	/**
	 * @param fixtures a sequence of fixtures
	 * @return whether any of them is a mountain or a hill
	 */
	@SuppressWarnings("TypeMayBeWeakened")
	private static boolean isHill(final Stream<TileFixture> fixtures) {
		return StreamSupport.stream(fixtures.spliterator(), false).anyMatch(
				fix -> (fix instanceof Mountain) || (fix instanceof Hill));
	}

	/**
	 * FIXME: *Some* explorers *would* notice even unexposed ground.
	 *
	 * TODO: We now check DCs on Events, but ignore relevant skills other than Perception
	 *
	 * @param unit a unit
	 * @param speed how fast the unit is moving
	 * @param fix  a fixture
	 * @return whether the unit might notice it. Units do not notice themselves, and do
	 * not notice unexposed ground, and do not notice null fixtures.
	 */
	public static boolean shouldSometimesNotice(final IUnit unit, final Speed speed,
												@Nullable final TileFixture fix) {
		if (fix instanceof Ground) {
			return ((Ground) fix).isExposed();
		} else if (unit.equals(fix)) {
			return false;
		} else if (fix instanceof IEvent) {
			return (getHighestPerception(unit) + speed.getPerceptionModifier() + 15) >=
						   ((IEvent) fix).getDC();
		} else return fix != null;
	}

	/**
	 * TODO: This does not properly handle the unusual case of a very unobservant unit.
	 *
	 * @param unit a unit
	 * @return the highest Perception score of any member, or 0 if no members
	 */
	private static int getHighestPerception(final Iterable<UnitMember> unit) {
		int retval = 0;
		for (final UnitMember member : unit) {
			if (member instanceof IWorker) {
				final int perception = getPerception((IWorker) member);
				if (perception > retval) {
					retval = perception;
				}
			}
		}
		return retval;
	}

	/**
	 * @param worker a worker
	 * @return the worker's Perception score
	 */
	private static int getPerception(final Iterable<IJob> worker) {
		final int ability;
		if (worker instanceof IWorker) {
			final WorkerStats stats = ((IWorker) worker).getStats();
			if (stats != null) {
				ability = WorkerStats.getModifier(stats.getWisdom());
			} else {
				ability = 0;
			}
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
		return ability + (ranks * 2);
	}

	/**
	 * TODO: Very-observant units should "always" notice some things that others might "sometimes" notice.
	 * @param unit a unit
	 * @param fix  a fixture
	 * @return whether the unit should always notice it. A null fixture is never noticed
	 */
	public static boolean shouldAlwaysNotice(final HasOwner unit,
											 @Nullable final TileFixture fix) {
		return (fix instanceof Mountain) || (fix instanceof RiverFixture) ||
					   (fix instanceof Hill) || (fix instanceof Forest) ||
					   ((fix instanceof ITownFixture) &&
								((ITownFixture) fix).getOwner().equals(unit.getOwner()));
	}
	/**
	 * @param possibilities a list of things the mover might find
	 * @param mover the explorer
	 * @param speed how fast the explorer is moving
	 * @return a list of things from the list of possibilities that the mover should in
	 * fact find
	 */
	public static List<TileFixture> selectNoticed(final List<TileFixture> possibilities, final IUnit mover,
											   final Speed speed) {
		final List<TileFixture> local = new ArrayList<>(possibilities);
		Collections.shuffle(local);
		// Perception gets an extra +1 because our RNG generates 0-19, not 1-20.
		int perception = getHighestPerception(mover) + speed.getPerceptionModifier() + 1;
		final List<TileFixture> retval = new ArrayList<>();
		for (final TileFixture item : local) {
			final int dc;
			// TODO: More kinds of fixtures ought to have DCs---perhaps not per-instance
			if (item instanceof IEvent) {
				dc = ((IEvent) item).getDC();
			} else {
				dc = 10;
			}
			if (SingletonRandom.RANDOM.nextInt(20) + 1 < dc) {
				return retval;
			} else {
				retval.add(item);
				perception -= 5;
			}
		}
		return retval;
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
}

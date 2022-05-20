package exploration.common;

import org.jetbrains.annotations.Nullable;
import java.util.List;
import java.util.ArrayList;

import lovelace.util.SingletonRandom;

import common.map.River;
import common.map.TileType;
import common.map.TileFixture;
import common.map.Direction;
import common.map.HasOwner;

import common.map.fixtures.mobile.worker.WorkerStats;
import common.map.fixtures.mobile.worker.ISkill;

import common.map.fixtures.mobile.IUnit;
import common.map.fixtures.mobile.IWorker;

import common.map.fixtures.terrain.Hill;
import common.map.fixtures.terrain.Forest;

import common.map.fixtures.towns.ITownFixture;

import java.util.Collections;
import java.util.Collection;
import java.util.function.Function;
import java.util.stream.StreamSupport;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.function.Predicate;

/**
 * An encapsulation of the very basic movement model we currently use.
 *
 * <ul>
 * <li>It is possible to move from a land tile to a land tile, and from one
 * {@link TileType#Ocean ocean} tile to another, but not from land to water or
 * vice versa.</li>
 * <li>Each unit has a certain number of "movement points" per turn, and each
 * action it takes costs MP; how many MP it takes to move depends on the
 * terrain type, whether the tile is mountainous, whether it has any forests,
 * and whether there are any rivers going in approximately the same direction.</li>
 * <li>Units notice nearby {@link TileFixture "fixtures"} in the map (only on
 * the tiles they visit, for now) as they move, some automatically and some
 * with a probability dependent on the kind of fixture and on the unit's
 * members' Perception score.</li>
 * </ul>
 */
public final class SimpleMovementModel {
	private SimpleMovementModel() {
	}

	/**
	 * Whether land movement is possible on the given terrain.
	 */
	public static boolean landMovementPossible(final @Nullable TileType terrain) {
		return terrain != null && TileType.Ocean != terrain;
	}

	/**
	 * Whether rivers in either the source or the destination will speed travel in the given direction.
	 */
	public static boolean riversSpeedTravel(final Direction direction, final Collection<River> source,
	                                        final Collection<River> dest) {
		final Predicate<Direction> recurse = partial -> riversSpeedTravel(partial, source, dest);
		switch (direction) {
		case North:
			return source.contains(River.North) || dest.contains(River.South);
		case Northeast:
			return recurse.test(Direction.North) || recurse.test(Direction.East);
		case East:
			return source.contains(River.East) || dest.contains(River.West);
		case Southeast:
			return recurse.test(Direction.South) || recurse.test(Direction.East);
		case South:
			return source.contains(River.South) || dest.contains(River.North);
		case Southwest:
			return recurse.test(Direction.South) || recurse.test(Direction.West);
		case West:
			return source.contains(River.West) || dest.contains(River.East);
		case Northwest:
			return recurse.test(Direction.North) || recurse.test(Direction.West);
		case Nowhere:
			return false;
		default:
			throw new IllegalStateException("Unhandled switch case");
		}
	}

	/**
	 * Get the cost of movement in the given conditions.
	 *
	 * FIXME: Reduce cost when roads present (TODO: rebalance base costs?)
	 *
	 * @param terrain The terrain being traversed. Null if "not visible."
	 * @param forest Whether the location is forested
	 * @param mountain Whether the location is mountainous
	 * @param river Whether the location has a river that reduces cost
	 * @param fixtures The fixtures at the location TODO: Iterable instead of varargs?
	 */
	public static int movementCost(final @Nullable TileType terrain, final boolean forest, final boolean mountain,
	                               final boolean river, final Iterable<TileFixture> fixtures) {
		if (terrain == null) {
			return Integer.MAX_VALUE - 1;
		} else if (TileType.Ocean == terrain) {
			return Integer.MAX_VALUE - 1;
		} else if (TileType.Jungle == terrain || TileType.Swamp == terrain) {
			return (river) ? 4 : 6;
		} else if (forest || mountain || Stream.of(fixtures)
				.anyMatch(fix -> (fix instanceof Forest && !((Forest) fix).isRows()) || fix instanceof Hill) ||
				           TileType.Desert == terrain) {
			return (river) ? 2 : 3;
		} else if (TileType.Steppe == terrain || TileType.Plains == terrain ||
				           TileType.Tundra == terrain) {
			return (river) ? 1 : 2;
		} else {
			throw new IllegalStateException("Unhandled terrain type");
		}
	}

	/**
	 * Check whether a unit moving at the given relative speed might notice
	 * the given fixture. Units do not notice themselves and do not notice
	 * null fixtures.
	 *
	 * TODO: We now check DCs on Events, but ignore relevant skills other
	 * than Perception.  And now a lot more things have DCs for which those
	 * other skills are relevant.
	 *
	 * @param unit The moving unit
	 * @param speed How fast the unit is moving
	 * @param fixture The fixture the unit might be noticing
	 */
	public static boolean shouldSometimesNotice(final HasOwner unit, final Speed speed,
	                                            final @Nullable TileFixture fixture) {
		if (fixture == null) {
			return false;
		} else if (unit.equals(fixture)) {
			return false;
		} else {
			final int perception;
			if (unit instanceof IUnit) {
				perception = highestPerception((IUnit) unit);
			} else {
				perception = 0;
			}
			return (perception + speed.getPerceptionModifier() + 15) >= fixture.getDC();
		}
	}

	/**
	 * Get the highest Perception score of any member of the unit
	 *
	 * TODO: This does not properly handle the unusual case of a very unobservant unit
	 */
	private static int highestPerception(final IUnit unit) {
		return unit.stream().filter(IWorker.class::isInstance)
				.map(IWorker.class::cast).mapToInt(SimpleMovementModel::getPerception)
				.max().orElse(0);
	}

	/**
	 * Get a worker's Perception score.
	 */
	private static int getPerception(final IWorker worker) {
		final int ability = Optional.ofNullable(worker.getStats())
				.map(WorkerStats::getWisdom).orElse(0);
		final int ranks = StreamSupport.stream(worker.spliterator(), true)
				.flatMap(x -> StreamSupport.stream(x.spliterator(), true))
				.filter(s -> "percpetion".equalsIgnoreCase(s.getName()))
				.mapToInt(ISkill::getLevel).sum();
		return ability + (ranks * 2);
	}

	/**
	 * Whether the unit should always notice the given fixture. A null fixture is never noticed.
	 *
	 * TODO: Very-observant units should "always" notice some things that
	 * others might "sometimes" notice.
	 */
	public static boolean shouldAlwaysNotice(final HasOwner unit, final @Nullable TileFixture fixture) {
		if (fixture instanceof ITownFixture) {
			return ((ITownFixture) fixture).getOwner().equals(unit.getOwner());
		} else {
			return fixture instanceof Hill || fixture instanceof Forest;
		}
	}

	/**
	 * Choose what the mover should in fact find from the list of things he
	 * or she might find. Since some callers need to have a list of Pairs
	 * instead of TileFixtures, we take a function for getting the fixtures
	 * out of the list.
	 */
	public static <Element> Iterable<Element> selectNoticed(final List<Element> possibilities,
	                                                        final Function<Element, TileFixture> getter, final IUnit mover, final Speed speed) {
		final List<Element> local = new ArrayList<>(possibilities);
		Collections.shuffle(local);
		int perception = highestPerception(mover) + speed.getPerceptionModifier();
		final List<Element> retval = new ArrayList<>();
		for (final Element item : local) {
			final int dc = getter.apply(item).getDC();
			if (SingletonRandom.SINGLETON_RANDOM.nextInt(20) + 1 + perception >= dc) {
				retval.add(item);
				perception -= 5;
			}
		}
		return Collections.unmodifiableList(retval);
	}
}

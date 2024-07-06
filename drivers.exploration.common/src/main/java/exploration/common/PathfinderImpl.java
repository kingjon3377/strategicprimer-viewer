package exploration.common;

import lovelace.util.LovelaceLogger;
import legacy.map.MapDimensions;
import legacy.map.TileFixture;
import lovelace.util.SimplePair;
import org.jetbrains.annotations.Nullable;
import org.javatuples.Pair;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.Map;
import java.util.HashSet;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;

import legacy.map.ILegacyMap;
import legacy.map.Direction;
import legacy.map.Point;
import legacy.map.fixtures.terrain.Forest;

import java.util.function.Predicate;
import java.util.Collections;
import java.util.Optional;

/* package */ class PathfinderImpl implements Pathfinder {
	public PathfinderImpl(final ILegacyMap map) {
		this.map = map;
		final MapDimensions dims = map.getDimensions();
		size = dims.rows() * dims.columns();
		LovelaceLogger.debug("Map has %d tiles", size);
		tentativeDistances = new HashMap<>(size);
	}

	private final ILegacyMap map;
	private final Map<SimplePair<Point>, Integer> tentativeDistances;
	private final int size;

	private static Predicate<Map.Entry<SimplePair<Point>, Integer>> forUs(final Point base,
																		  final Collection<Point> unvisited) {
		return entry -> entry.getKey().getFirst().equals(base) &&
				unvisited.contains(entry.getKey().getSecond());
	}

	private @Nullable Point nextUnvisited(final Point base, final Collection<Point> unvisited) {
		return tentativeDistances.entrySet().stream().filter(forUs(base, unvisited)).min(Map.Entry.comparingByValue())
				.map(Map.Entry::getKey).map(SimplePair::getSecond).orElse(null);
	}

	private static Direction getDirection(final Point one, final Point two) {
		if (one.row() < two.row()) {
			if (one.column() < two.column()) {
				return Direction.Northeast;
			} else if (one.column() == two.column()) {
				return Direction.North;
			} else {
				return Direction.Northwest;
			}
		} else if (one.row() == two.row()) {
			if (one.column() < two.column()) {
				return Direction.East;
			} else if (one.column() == two.column()) {
				return Direction.Nowhere;
			} else {
				return Direction.West;
			}
		} else {
			if (one.column() < two.column()) {
				return Direction.Southeast;
			} else if (one.column() == two.column()) {
				return Direction.South;
			} else {
				return Direction.Southwest;
			}
		}
	}

	private static int clampAdd(final int one, final int two) {
		if (one == 0 || two == 0 || (one > 0 ^ two > 0)) {
			return one + two;
		} else if (one > 0 && Integer.MAX_VALUE - one < two) {
			return Integer.MAX_VALUE;
		} else if (one < 0 && Integer.MIN_VALUE - one > two) {
			return Integer.MIN_VALUE;
		} else {
			return one + two;
		}
	}

	/**
	 * The shortest-path distance, avoiding obstacles, in MP, between two
	 * points, using Dijkstra's algorithm.
	 */
	@Override
	public Pair<Integer, Iterable<Point>> getTravelDistance(final Point start, final Point end) {
		final Collection<Point> unvisited = new HashSet<>(size);
		for (final Point point : map.getLocations()) {
			unvisited.add(point);
			if (!tentativeDistances.containsKey(SimplePair.of(start, point))) {
				tentativeDistances.put(SimplePair.of(start, point), Integer.MAX_VALUE - 1);
			}
		}
		tentativeDistances.put(SimplePair.of(start, start), 0);
		Point current = start;
		int iterations = 0;
		final Map<Point, Point> retval = new HashMap<>(size);
		final Predicate<TileFixture> isForest = Forest.class::isInstance;
		while (!unvisited.isEmpty()) {
			iterations++;
			if (!tentativeDistances.containsKey(SimplePair.of(start, current))) {
				throw new IllegalStateException("Tentative distance missing");
			}
			final int currentDistance = tentativeDistances.get(SimplePair.of(start, current));
			if (current.equals(end)) {
				LovelaceLogger.debug("Reached the end after %d iterations",
						iterations);
				final List<Point> path = new ArrayList<>(size >> 2);
				path.add(current);
				while (retval.containsKey(current)) {
					path.add(retval.get(current));
					current = retval.get(current);
				}
				Collections.reverse(path);
				return Pair.with(currentDistance, Collections.unmodifiableList(path));
			} else if (currentDistance >= (Integer.MAX_VALUE - 1)) {
				LovelaceLogger.info(
						"Considering an 'infinite-distance' tile after %d iterations",
						iterations);
				return Pair.with(currentDistance, Collections.emptyList());
			}
			for (final Point neighbor : new SurroundingPointIterable(current, map.getDimensions(), 1)) {
				LovelaceLogger.debug("At %s, considering %s", current, neighbor);
				if (!unvisited.contains(neighbor)) {
					LovelaceLogger.debug("Already checked, so skipping.");
					continue;
				}
				if (!tentativeDistances.containsKey(SimplePair.of(start, neighbor))) {
					throw new IllegalStateException("Missing prior estimate");
				}
				final int estimate = tentativeDistances.get(SimplePair.of(start, neighbor));
				final int tentativeDistance = clampAdd(currentDistance,
						SimpleMovementModel.movementCost(map.getBaseTerrain(neighbor),
								map.getFixtures(neighbor).stream().anyMatch(isForest),
								map.isMountainous(neighbor),
								SimpleMovementModel.riversSpeedTravel(
										getDirection(current, neighbor),
										map.getRivers(current),
										map.getRivers(neighbor)),
								map.getFixtures(neighbor)));
				LovelaceLogger.debug("Old estimate %d, new estimate %d", estimate,
						tentativeDistance);
				if (tentativeDistance < estimate) {
					LovelaceLogger.debug("Updating path");
					retval.put(neighbor, current);
					tentativeDistances.put(SimplePair.of(start, neighbor),
							tentativeDistance);
				}
				if (estimate < 0) {
					LovelaceLogger.warning("Old estimate at %s was negative",
							neighbor);
					return Pair.with(Integer.MAX_VALUE - 1, Collections.emptyList());
				} else if (tentativeDistance < 0) {
					LovelaceLogger.warning("Recomputed estimate at %s was negative",
							neighbor);
					return Pair.with(Integer.MAX_VALUE - 1, Collections.emptyList());
				}
			}
			LovelaceLogger.debug("Finished checking neighbors of %s", current);
			unvisited.remove(current);
			final Point next = nextUnvisited(start, unvisited);
			if (Objects.isNull(next)) {
				LovelaceLogger.info(
						"Couldn't find a smallest-estimate unchecked tile after %d iterations",
						iterations);
				return Pair.with(Integer.MAX_VALUE - 1, Collections.emptyList());
			} else {
				current = next;
			}
		}
		LovelaceLogger.info("Apparently ran out of tiles after %d iterations", iterations);
		return Pair.with(Optional.ofNullable(tentativeDistances.get(SimplePair.of(start, end)))
				.orElse(Integer.MAX_VALUE - 1), Collections.emptyList());
	}
}

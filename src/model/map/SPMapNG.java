package model.map;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterators;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import model.map.fixtures.Ground;
import model.map.fixtures.TextFixture;
import model.map.fixtures.mobile.Animal;
import model.map.fixtures.mobile.IUnit;
import model.map.fixtures.resources.CacheFixture;
import model.map.fixtures.terrain.Forest;
import model.viewer.PointIterator;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import util.ArraySet;
import util.EmptyIterator;
import util.IteratorWrapper;
import util.LineEnd;
import util.TypesafeLogger;

import static util.NullCleaner.assertNotNull;
import static util.NullStream.DEV_NULL;

/**
 * A proper implementation of IMapNG.
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
public class SPMapNG implements IMutableMapNG {
	/**
	 * Logger.
	 */
	private static final Logger LOGGER = TypesafeLogger.getLogger(SPMapNG.class);
	/**
	 * The set of mountainous places.
	 */
	private final Collection<Point> mountains = new HashSet<>();
	/**
	 * The base terrain at points in the map.
	 */
	private final Map<Point, TileType> terrain = new HashMap<>();
	/**
	 * The players in the map.
	 */
	private final IMutablePlayerCollection playerCollection;
	/**
	 * The current turn.
	 */
	private int turn;
	/**
	 * The forests in the map. If there's more than one forest, only one goes here, and
	 * the rest go in the "miscellaneous fixtures" pile.
	 */
	private final Map<Point, Forest> forests = new HashMap<>();
	/**
	 * Fixtures at various points, other than the main ground and forest. We specify
	 * Collection rather than Iterable because they need to be explicitly mutable for
	 * unit
	 * motion.
	 *
	 * TODO: Use a multimap once we add the Guava dependency.
	 */
	private final Map<Point, Collection<TileFixture>> fixtures =
			new HashMap<>();
	/**
	 * The dimensions of the map.
	 */
	private final MapDimensions dims;
	/**
	 * The ground under various locations. If there's more than one, others go in the
	 * "other fixtures" collection.
	 */
	private final Map<Point, Ground> ground = new HashMap<>();
	/**
	 * The rivers in the map.
	 */
	private final Map<@NonNull Point, @NonNull EnumSet<@NonNull River>> rivers =
			new HashMap<>();
	/**
	 * Map max version.
	 */
	public static final int MAX_VERSION = 1;

	/**
	 * @param obj     another map
	 * @param ostream     the stream to write verbose results to
	 * @param context a string to print before every line of output, describing the
	 *                context
	 * @return whether the other map is a subset of this one
	 * @throws IOException on I/O error writing output to the stream
	 */
	@Override
	public boolean isSubset(final IMapNG obj, final Appendable ostream,
							final String context) throws IOException {
		if (dimensions().equals(obj.dimensions())) {
			// TODO: We should probably delegate this to the PlayerCollection.
			boolean retval = true;
			for (final Player player : obj.players()) {
				if (!playerCollection.contains(player)) {
					ostream.append(context);
					ostream.append("\tExtra player ");
					ostream.append(player.toString());
					ostream.append(LineEnd.LINE_SEP);
					retval = false;
					// return false;
				}
			}
			// Declared here to avoid object allocations in the loop.
			final Collection<TileFixture> ourFixtures = new ArrayList<>();
			final Map<Integer, SubsettableFixture> ourSubsettables =
					new HashMap<>();
			// Because IUnit is Subsettable<IUnit> and thus incompatible
			// with SubsettableFixture
			final Map<Integer, IUnit> ourUnits = new HashMap<>();
			for (final Point point : locations()) {
				final String localContext =
						context + " At " + Objects.toString(point) + ':';
				if ((getBaseTerrain(point) != obj.getBaseTerrain(point))
							&& (TileType.NotVisible != obj.getBaseTerrain(point))) {
					ostream.append(localContext);
					if (TileType.NotVisible == getBaseTerrain(point)) {
						ostream.append("\tHas terrain information we don't");
					} else {
						ostream.append("\tBase terrain differs");
					}
					ostream.append(LineEnd.LINE_SEP);
					retval = false;
					continue;
					// return false;
				}
				if (obj.isMountainous(point) && !isMountainous(point)) {
					ostream.append(localContext);
					ostream.append("\tHas mountains we don't");
					ostream.append(LineEnd.LINE_SEP);
					retval = false;
					// return false;
				}
				final Forest forest = obj.getForest(point);
				if (!Objects.equals(getForest(point), forest)
							&& (forest != null)) {
					// There are *far* too many false positives if we don't
					// check the "other fixtures," because of the way we
					// represent this in the XML. If we ever start a new
					// campaign with a different data representation---perhaps a
					// database---we should remove this
					// check.
					if (!fixtures.containsKey(point) ||
								!assertNotNull(fixtures.get(point)).contains(forest)) {
						ostream.append(localContext);
						ostream.append(
								"\tHas forest we don't, or different primary forest");
						ostream.append(LineEnd.LINE_SEP);
						retval = false;
					}
					// return false;
				}
				final Ground theirGround = obj.getGround(point);
				final Ground ourGround = getGround(point);
				if (!Objects.equals(ourGround, theirGround)
							&& (theirGround != null)) {
					// There are *far* too many false positives if we don't
					// check the "other fixtures," because of the way we
					// represent this in the XML. If we ever start a new
					// campaign with a different data representation---perhaps a
					// database---we should remove this
					// check. Except for the 'exposed' bit.
					//noinspection StatementWithEmptyBody
					if ((ourGround != null)
								&& ourGround.getKind().equals(theirGround.getKind())
								&& ourGround.isExposed()) {
						// They just don't have the exposed bit set; carry on
						// ...
					} else if ((ourGround == null) || !assertNotNull(fixtures.get(point))
															.contains(theirGround)) {
						ostream.append(localContext);
						ostream.append(
								"\tHas different primary ground, or ground we don't");
						ostream.append(LineEnd.LINE_SEP);
						retval = false;
						// return false;
					}
				}
				ourFixtures.clear();
				ourSubsettables.clear();
				ourUnits.clear();
				for (final TileFixture fix : getOtherFixtures(point)) {
					final Integer idNum =
							assertNotNull(Integer.valueOf(fix.getID()));
					if (fix instanceof IUnit) {
						ourUnits.put(idNum, (IUnit) fix);
					} else if (fix instanceof SubsettableFixture) {
						ourSubsettables.put(idNum,
								(SubsettableFixture) fix);
					} else {
						ourFixtures.add(fix);
					}
				}
				final Iterable<TileFixture> theirFixtures =
						obj.getOtherFixtures(point);
				for (final TileFixture fix : theirFixtures) {
					if (ourFixtures.contains(fix) || shouldSkip(fix) ||
								((fix instanceof Ground) &&
										Objects.equals(fix, getGround(point))) ||
								((fix instanceof Forest) &&
										Objects.equals(fix, getForest(point)))) {
						continue;
					} else if ((fix instanceof IUnit) && ourUnits.containsKey(
							Integer.valueOf(fix.getID()))) {
						retval &= assertNotNull(ourUnits.get(Integer.valueOf(fix.getID())))
										.isSubset(fix, ostream, localContext);
					} else if ((fix instanceof SubsettableFixture) && ourSubsettables
																			.containsKey(
																					Integer.valueOf(
																							fix.getID()))) {
						retval &= assertNotNull(
								ourSubsettables.get(Integer.valueOf(fix.getID())))
										.isSubset(fix, ostream, localContext);
					} else {
						ostream.append(localContext);
						ostream.append(" Extra fixture:\t");
						ostream.append(fix.toString());
						ostream.append(LineEnd.LINE_SEP);
						retval = false;
						break;
						// return false;
					}
				}
				final Set<River> ourRivers = rivers.get(point);
				final Iterable<River> theirRivers = obj.getRivers(point);
				for (final River river : theirRivers) {
					if ((ourRivers == null) || !ourRivers.contains(river)) {
						ostream.append(localContext);
						ostream.append("\tExtra river");
						ostream.append(LineEnd.LINE_SEP);
						retval = false;
						break;
						// return false;
					}
				}
			}
			return retval;
		} else {
			ostream.append(context);
			ostream.append("\tDimension mismatch");
			ostream.append(LineEnd.LINE_SEP);
			return false;
		}
	}

	/**
	 * @param other another map
	 * @return the result of a comparison between us and it.
	 */
	@SuppressWarnings("ParameterNameDiffersFromOverriddenParameter")
	@Override
	public int compareTo(final IMapNG other) {
		if (equals(other)) {
			return 0;
		} else {
			final int ours = hashCode();
			final int theirs = Objects.hashCode(other);
			if (ours > theirs) {
				return 1;
			} else if (ours < theirs) {
				return -1;
			} else {
				return 0;
			}
		}
	}

	/**
	 * Constructor.
	 *
	 * @param dimensions  the dimensions of the map
	 * @param players     the players in the map
	 * @param currentTurn the current turn
	 */
	public SPMapNG(final MapDimensions dimensions,
				final IMutablePlayerCollection players,
				final int currentTurn) {
		dims = dimensions;
		playerCollection = players;
		turn = currentTurn;
	}

	/**
	 * @return the map's dimensions
	 */
	@Override
	public MapDimensions dimensions() {
		return dims;
	}

	/**
	 * @return the players in the map
	 */
	@Override
	public Iterable<@NonNull Player> players() {
		return playerCollection;
	}

	/**
	 * @return the locations in the map
	 */
	@Override
	public Iterable<@NonNull Point> locations() {
		return new IteratorWrapper<>(new PointIterator(dimensions(), null, true, true));
	}
	/**
	 * @return the locations in the map
	 */
	@Override
	public Stream<@NonNull Point> locationStream() {
		return assertNotNull(StreamSupport.stream(
				Spliterators.spliteratorUnknownSize(
						new PointIterator(dimensions(), null, true, true), 0),
				false));
	}
	/**
	 * @param location a location
	 * @return the base terrain at that location
	 */
	@Override
	public TileType getBaseTerrain(final Point location) {
		if (terrain.containsKey(location)) {
			return assertNotNull(terrain.get(location));
		} else {
			return TileType.NotVisible;
		}
	}

	/**
	 * @param location a location
	 * @return whether that location is mountainous
	 */
	@Override
	public boolean isMountainous(final Point location) {
		return mountains.contains(location);
	}

	/**
	 * @param location a location
	 * @return the rivers there
	 */
	@Override
	public Iterable<River> getRivers(final Point location) {
		if (rivers.containsKey(location)) {
			return assertNotNull(rivers.get(location));
		} else {
			return assertNotNull(EnumSet.noneOf(River.class));
		}
	}

	/**
	 * @param location a location
	 * @return any forests there
	 */
	@Override
	@Nullable
	public Forest getForest(final Point location) {
		return forests.get(location);
	}

	/**
	 * @param location a location
	 * @return the ground there
	 */
	@Override
	@Nullable
	public Ground getGround(final Point location) {
		return ground.get(location);
	}

	/**
	 * @param location a location
	 * @return any other fixtures there
	 */
	@Override
	public Iterable<TileFixture> getOtherFixtures(final Point location) {
		if (fixtures.containsKey(location)) {
			return assertNotNull(fixtures.get(location));
		} else {
			return new IteratorWrapper<>(new EmptyIterator<>());
		}
	}
	/**
	 * @param location a location
	 * @return a stream of any other fixtures there
	 */
	@Override
	public Stream<TileFixture> streamOtherFixtures(final Point location) {
		if (fixtures.containsKey(location)) {
			return assertNotNull(assertNotNull(fixtures.get(location)).stream());
		} else {
			return assertNotNull(Stream.empty());
		}
	}
	/**
	 * @return the current turn
	 */
	@Override
	public int getCurrentTurn() {
		return turn;
	}

	/**
	 * @return the current player
	 */
	@Override
	public Player getCurrentPlayer() {
		return playerCollection.getCurrentPlayer();
	}

	/**
	 * @param obj an object
	 * @return whether it's a map equal to this one
	 */
	@Override
	public boolean equals(@Nullable final Object obj) {
		return (obj == this) || ((obj instanceof IMapNG) && equalsImpl((IMapNG) obj));
	}

	/**
	 * @param obj another map
	 * @return whether it equals this one
	 */
	private boolean equalsImpl(final IMapNG obj) {
		if (dimensions().equals(obj.dimensions())
					&& areIterablesEqual(players(), obj.players())
					&& (turn == obj.getCurrentTurn())
					&& getCurrentPlayer().equals(obj.getCurrentPlayer())) {
			return locationStream().allMatch(
					point -> (getBaseTerrain(point) == obj.getBaseTerrain(point)) &&
									(isMountainous(point) == obj.isMountainous(point)) &&
									areIterablesEqual(getRivers(point),
											obj.getRivers(point)) &&
									Objects.equals(getForest(point),
											obj.getForest(point)) &&
									Objects.equals(getGround(point),
											obj.getGround(point)) &&
									areStreamsEqual(streamOtherFixtures(point),
											obj.streamOtherFixtures(point)));
		} else {
			return false;
		}
	}

	/**
	 * FIXME: This is probably very slow ...
	 *
	 * @param firstIterable  one iterable
	 * @param secondIterable another
	 * @param <T>            the type of thing they contain
	 * @return whether they contain the same elements.
	 */
	private static <T> boolean areIterablesEqual(final Iterable<T> firstIterable,
												final Iterable<T> secondIterable) {
		final Collection<T> first = StreamSupport.stream(firstIterable.spliterator(),
				false).collect(Collectors.toList());
		final Collection<T> firstCopy =
				StreamSupport.stream(firstIterable.spliterator(), false)
						.collect(Collectors.toList());
		final Collection<T> second =
				StreamSupport.stream(secondIterable.spliterator(), false)
						.collect(Collectors.toList());
		final Collection<T> secondCopy =
				StreamSupport.stream(secondIterable.spliterator(), false)
						.collect(Collectors.toList());
		firstCopy.removeAll(second);
		secondCopy.removeAll(first);
		return first.containsAll(second) && second.containsAll(first) &&
					secondCopy.isEmpty() && firstCopy.isEmpty();
	}
	/**
	 * Note that this consumes the streams!
	 *
	 * FIXME: This is probably very slow ...
	 *
	 * @param firstStream  one stream
	 * @param secondStream another
	 * @param <T>            the type of thing they contain
	 * @return whether they contain the same elements.
	 */
	private static <T> boolean areStreamsEqual(final Stream<T> firstStream,
											final Stream<T> secondStream) {
		final Collection<T> first = firstStream.collect(Collectors.toList());
		final Collection<T> firstCopy = new ArrayList<>(first);
		final Collection<T> second = secondStream.collect(Collectors.toList());
		firstCopy.removeAll(second);
		final Collection<T> secondCopy = new ArrayList<>(second);
		secondCopy.removeAll(first);
		return first.containsAll(second) && second.containsAll(first) &&
					secondCopy.isEmpty() && firstCopy.isEmpty();
	}

	/**
	 * The hash code is based on the dimensions, the current turn, and the current
	 * player;
	 * basing it on anything else would certainly break any code that placed an IMapNG
	 * into a hash-table.
	 *
	 * @return a hash code for the object
	 */
	@Override
	public int hashCode() {
		return dimensions().hashCode() + (turn << 3) +
					(getCurrentPlayer().hashCode() << 5);
	}

	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder("SPMapNG:");
		builder.append(LineEnd.LINE_SEP);
		builder.append("Map version: ");
		builder.append(dimensions().version);
		builder.append(LineEnd.LINE_SEP);
		builder.append("Rows: ");
		builder.append(dimensions().rows);
		builder.append(LineEnd.LINE_SEP);
		builder.append("Columns: ");
		builder.append(dimensions().cols);
		builder.append(LineEnd.LINE_SEP);
		builder.append("Current Turn: ");
		builder.append(turn);
		builder.append(LineEnd.LINE_SEP);
		builder.append(LineEnd.LINE_SEP);
		builder.append("Players:");
		builder.append(LineEnd.LINE_SEP);
		for (final Player player : players()) {
			builder.append(player);
			if (player.equals(getCurrentPlayer())) {
				builder.append(" (current)");
			}
			builder.append(LineEnd.LINE_SEP);
		}
		builder.append(LineEnd.LINE_SEP);
		builder.append("Contents:");
		builder.append(LineEnd.LINE_SEP);
		for (final Point location : locations()) {
			builder.append("At ");
			builder.append(location);
			builder.append(": ");
			if (getBaseTerrain(location) != TileType.NotVisible) {
				builder.append("terrain: ");
				builder.append(getBaseTerrain(location));
				builder.append(", ");
			}
			if (isMountainous(location)) {
				builder.append("mountains, ");
			}
			if (getGround(location) != null) {
				builder.append("ground: ");
				builder.append(getGround(location));
				builder.append(", ");
			}
			if (getForest(location) != null) {
				builder.append("forest: ");
				builder.append(getForest(location));
				builder.append(", ");
			}
			if (StreamSupport.stream(getRivers(location).spliterator(), false).count() >
						0) {
				builder.append("rivers:");
				for (final River river : getRivers(location)) {
					builder.append(' ');
					builder.append(river);
				}
				builder.append(", ");
			}
			builder.append("other: ");
			for (final TileFixture fixture : getOtherFixtures(location)) {
				builder.append(LineEnd.LINE_SEP);
				builder.append(fixture);
				// builder.append(" (");
				// builder.append(fixture.getClass().getSimpleName());
				// builder.append(")");
			}
			builder.append(LineEnd.LINE_SEP);
		}
		return assertNotNull(builder.toString());
	}

	/**
	 * @param player the player to add
	 */
	@Override
	public void addPlayer(final Player player) {
		playerCollection.add(player);
	}

	/**
	 * @param location a location
	 * @param terrainType    the terrain there
	 */
	@Override
	public void setBaseTerrain(final Point location, final TileType terrainType) {
		terrain.put(location, terrainType);
	}

	/**
	 * @param location a location
	 * @param mtn      whether it's mountainous there
	 */
	@Override
	public void setMountainous(final Point location, final boolean mtn) {
		if (mtn) {
			mountains.add(location);
		} else {
			mountains.remove(location);
		}
	}

	/**
	 * @param location a location
	 * @param addedRivers     rivers to add to that location
	 */
	@Override
	public void addRivers(final Point location,
						final @NonNull River @NonNull ... addedRivers) {
		final EnumSet<@NonNull River> localRivers;
		if (rivers.containsKey(location)) {
			localRivers = rivers.get(location);
		} else {
			localRivers = assertNotNull(EnumSet.noneOf(River.class));
			rivers.put(location, localRivers);
		}
		Collections.addAll(localRivers, addedRivers);
	}

	/**
	 * @param location a location
	 * @param removedRivers     rivers to remove from it
	 */
	@Override
	public void removeRivers(final Point location, final River... removedRivers) {
		if (rivers.containsKey(location)) {
			final Set<River> localRivers = assertNotNull(rivers.get(location));
			Stream.of(removedRivers).forEach(localRivers::remove);
		}
	}

	/**
	 * @param location a location
	 * @param forest   what should be the primary forest there, if any
	 */
	@Override
	public void setForest(final Point location, @Nullable final Forest forest) {
		if (forest == null) {
			forests.remove(location);
		} else {
			forests.put(location, forest);
		}
	}

	/**
	 * @param location a location
	 * @param newGround     what the ground there should be, if any
	 */
	@Override
	public void setGround(final Point location, @Nullable final Ground newGround) {
		if (newGround == null) {
			ground.remove(location);
		} else {
			ground.put(location, newGround);
		}
	}

	/**
	 * @param location a location
	 * @param fix      a fixture to add there
	 */
	@Override
	public void addFixture(final Point location, final TileFixture fix) {
		final Collection<TileFixture> local;
		if (fixtures.containsKey(location)) {
			local = assertNotNull(fixtures.get(location));
		} else {
			local = new ArraySet<>();
			fixtures.put(location, local);
		}
		if (Objects.equals(fix, getForest(location)) ||
					Objects.equals(fix, getGround(location))) {
			return;
		}
		if (fix.getID() >= 0) {
			final Predicate<TileFixture> matcher = item -> item.getID() == fix.getID();
			final boolean found =
					local.stream().anyMatch(matcher);
			if (found) {
				final TileFixture existing = local.stream().filter(matcher).findAny()
													.orElseThrow(
															() -> new IllegalStateException("Fixture vanished"));
				try {
					if (existing.equals(fix) ||
								((existing instanceof SubsettableFixture) &&
										((SubsettableFixture) existing)
												.isSubset(fix, DEV_NULL, "")) ||
								((fix instanceof SubsettableFixture) &&
										((SubsettableFixture) fix)
												.isSubset(existing, DEV_NULL, ""))) {
						local.remove(existing);
						local.add(fix);
					} else {
						local.add(fix);
						LOGGER.log(Level.WARNING,
								"Inserted duplicate-ID fixture at " + location);
						LOGGER.log(Level.FINE, "Stack trace of this location: ", new Throwable());
						LOGGER.fine("Existing fixture was: " + existing.shortDesc());
						LOGGER.fine("Added: " + fix.shortDesc());
					}
				} catch (final IOException except) {
					//noinspection HardcodedFileSeparator
					LOGGER.log(Level.SEVERE, "I/O error on bit bucket", except);
				}
			} else {
				local.add(fix);
			}
		} else {
			local.add(fix);
		}
	}

	/**
	 * @param location a location
	 * @param fix      a fixture to remove from that location
	 */
	@Override
	public void removeFixture(final Point location, final TileFixture fix) {
		if (fixtures.containsKey(location)) {
			assertNotNull(fixtures.get(location)).remove(fix);
		}
	}

	/**
	 * @param player the new current player
	 */
	@Override
	public void setCurrentPlayer(final Player player) {
		playerCollection.getCurrentPlayer().setCurrent(false);
		playerCollection.getPlayer(player.getPlayerId()).setCurrent(true);
	}

	/**
	 * @param currentTurn the new current turn
	 */
	@Override
	public void setCurrentTurn(final int currentTurn) {
		turn = currentTurn;
	}

	/**
	 * @param fix a fixture
	 * @return whether strict-subset calculations should skip it.
	 */
	public static boolean shouldSkip(final TileFixture fix) {
		return (fix instanceof CacheFixture) || (fix instanceof TextFixture) ||
					((fix instanceof Animal) && ((Animal) fix).isTraces());
	}

	/**
	 * @param zero whether to "zero" sensitive data (probably just DCs)
	 * @return a copy of this map
	 */
	@Override
	public IMapNG copy(final boolean zero) {
		final IMutableMapNG retval =
				new SPMapNG(dimensions(), playerCollection.copy(), turn);
		for (final Point point : locations()) {
			retval.setBaseTerrain(point, getBaseTerrain(point));
			final Ground grd = getGround(point);
			if (grd == null) {
				retval.setGround(point, null);
			} else {
				retval.setGround(point, grd.copy(false));
			}
			final Forest forest = getForest(point);
			if (forest == null) {
				retval.setForest(point, null);
			} else {
				retval.setForest(point, forest.copy(false));
			}
			retval.setMountainous(point, isMountainous(point));
			for (final River river : getRivers(point)) {
				retval.addRivers(point, river);
			}
			// TODO: What other fixtures should we zero, or skip?
			for (final TileFixture fixture : getOtherFixtures(point)) {
				if (fixture instanceof IEvent) {
					retval.addFixture(point, fixture.copy(zero));
				} else {
					retval.addFixture(point, fixture.copy(false));
				}
			}
		}
		return retval;
	}
}

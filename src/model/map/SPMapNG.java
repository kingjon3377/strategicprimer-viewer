package model.map;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Formatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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
import util.IteratorWrapper;
import util.MultiMapHelper;
import util.NullStream;
import util.TypesafeLogger;

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
	 * Map max version.
	 */
	public static final int MAX_VERSION = 1;
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
	 * The current turn.
	 */
	private int turn;

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
	 * @param <T>          the type of thing they contain
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
	 * Strict-subset calculations should skip caches, text fixtures, and animal tracks.
	 * @param fix a fixture
	 * @return whether strict-subset calculations should skip it.
	 */
	public static boolean shouldSkip(final TileFixture fix) {
		return (fix instanceof CacheFixture) || (fix instanceof TextFixture) ||
					   ((fix instanceof Animal) && ((Animal) fix).isTraces());
	}

	/**
	 * Whether another map is a "strict" subset of this one.
	 * @param obj     another map
	 * @param ostream the stream to write verbose results to
	 * @param context a string to print before every line of output, describing the
	 *                context
	 * @return whether the other map is a subset of this one
	 */
	@Override
	public boolean isSubset(final IMapNG obj, final Formatter ostream,
							final String context) {
		if (dimensions().equals(obj.dimensions())) {
			// TODO: We should probably delegate this to the PlayerCollection.
			boolean retval = true;
			for (final Player player : obj.players()) {
				if (!playerCollection.contains(player)) {
					ostream.format("%s\tExtra player %s%n", context, player.toString());
					retval = false;
					// return false;
				}
			}
			// Declared here to avoid object allocations in the loop.
			final Collection<TileFixture> ourFixtures = new ArrayList<>();
			final Map<Integer, Collection<SubsettableFixture>> ourSubsettables =
					new HashMap<>();
			// Because IUnit is Subsettable<IUnit> and thus incompatible
			// with SubsettableFixture
			final Map<Integer, IUnit> ourUnits = new HashMap<>();
			for (final Point point : locations()) {
				if ((getBaseTerrain(point) != obj.getBaseTerrain(point))
							&& (TileType.NotVisible != obj.getBaseTerrain(point))) {
					if (TileType.NotVisible == getBaseTerrain(point)) {
						ostream.format("%s At %s:\tHas terrain information we don't%n",
								context, Objects.toString(point));
					} else {
						ostream.format("%s At %s:\tBase terrain differs%n", context,
								Objects.toString(point));
					}
					retval = false;
					continue;
					// return false;
				}
				if (obj.isMountainous(point) && !isMountainous(point)) {
					ostream.format("%s At %s:\tHas mountains we don't%n", context,
							Objects.toString(point));
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
								!fixtures.get(point).contains(forest)) {
						ostream.format(
								"%s At %s:\tHas forest we don't, or different primary " +
										"forest%n",
								context, Objects.toString(point));
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
					} else if ((ourGround == null) || streamOtherFixtures(point)
															  .noneMatch(
																	  theirGround::equals)) {
						ostream.format(
								"%s At %s:\tHas different primary ground, or ground we " +
										"don't%n",
								context, Objects.toString(point));
						retval = false;
						// return false;
					}
				}
				ourFixtures.clear();
				ourSubsettables.clear();
				ourUnits.clear();
				for (final TileFixture fix : getOtherFixtures(point)) {
					final Integer idNum = Integer.valueOf(fix.getID());
					if (fix instanceof IUnit) {
						ourUnits.put(idNum, (IUnit) fix);
					} else if (fix instanceof SubsettableFixture) {
						MultiMapHelper.getMapValue(ourSubsettables, idNum,
								key -> new ArrayList<>()).add((SubsettableFixture) fix);
					} else {
						ourFixtures.add(fix);
					}
				}
				final Iterable<TileFixture> theirFixtures =
						obj.getOtherFixtures(point);
				for (final TileFixture fix : theirFixtures) {
					if (ourFixtures.contains(fix) || shouldSkip(fix) ||
								Objects.equals(fix, getGround(point)) ||
								Objects.equals(fix, getForest(point))) {
						continue;
					} else if ((fix instanceof IUnit) && ourUnits.containsKey(
							Integer.valueOf(fix.getID()))) {
						retval &= ourUnits.get(Integer.valueOf(fix.getID()))
										  .isSubset(fix, ostream,
												  String.format("%s At %s:", context,
														  Objects.toString(point)));
					} else if ((fix instanceof SubsettableFixture) && ourSubsettables
																			  .containsKey(
																					  Integer.valueOf(
																							  fix.getID()))) {
						int count = 0;
						boolean unmatched = true;
						@Nullable SubsettableFixture match = null;
						for (final SubsettableFixture subsettable : ourSubsettables
																			.get(Integer
																						 .valueOf(
																								 fix.getID()))) {
							count++;
							match = subsettable;
							if (subsettable.isSubset(fix, NullStream.DEV_NULL, "")) {
								unmatched = false;
								break;
							} else {
								unmatched = true;
							}
						}
						if (count == 0) {
							ostream.format("%s At %s:Extra fixture:\t%s%n",
									context, Objects.toString(point), fix.toString());
							retval = false;
							break;
						} else if (count == 1) {
							retval &= match.isSubset(fix, ostream,
									String.format("%s At %s:", context, Objects.toString(point)));
						} else if (unmatched) {
							ostream.format(
									"%s At %s:Fixture with ID #%d didn't match any of " +
											"the subsettable fixtures here sharing that " +
											"ID%n",
									context, Objects.toString(point),
									Integer.valueOf(fix.getID()));
							retval = false;
							break;
						}
					} else {
						ostream.format("%s At %s Extra fixture:\t%s%n", context,
								Objects.toString(point), fix.toString());
						retval = false;
						break;
						// return false;
					}
				}
				final Set<River> ourRivers = rivers.get(point);
				final Iterable<River> theirRivers = obj.getRivers(point);
				for (final River river : theirRivers) {
					if ((ourRivers == null) || !ourRivers.contains(river)) {
						ostream.format("%s At %s:\tExtra river%n", context,
								Objects.toString(point));
						retval = false;
						break;
						// return false;
					}
				}
			}
			return retval;
		} else {
			ostream.format("%s\tDimension mismatch%n", context);
			return false;
		}
	}

	/**
	 * Compare another map to this one.
	 * @param other another map
	 * @return the result of a comparison between us and it.
	 */
	@SuppressWarnings("ParameterNameDiffersFromOverriddenParameter")
	@Override
	public int compareTo(final IMapNG other) {
		if (equals(other)) {
			return 0;
		} else {
			return Integer.compare(hashCode(), Objects.hashCode(other));
		}
	}

	/**
	 * The dimensions of the map.
	 * @return the map's dimensions
	 */
	@Override
	public MapDimensions dimensions() {
		return dims;
	}

	/**
	 * The players in the map.
	 * @return the players in the map
	 */
	@Override
	public Iterable<@NonNull Player> players() {
		return playerCollection;
	}

	/**
	 * The locations in the map.
	 * @return the locations in the map
	 */
	@Override
	public Iterable<@NonNull Point> locations() {
		return new IteratorWrapper<>(new PointIterator(dimensions(), null, true, true));
	}

	/**
	 * A stream of the locations in the map.
	 * @return the locations in the map
	 */
	@Override
	public Stream<@NonNull Point> locationStream() {
		return new PointIterator(dimensions(), null, true, true).stream();
	}

	/**
	 * The base terrain at the given location.
	 * @param location a location
	 * @return the base terrain at that location
	 */
	@Override
	public TileType getBaseTerrain(final Point location) {
		if (terrain.containsKey(location)) {
			return terrain.get(location);
		} else {
			return TileType.NotVisible;
		}
	}

	/**
	 * Whether the given location is mountainous.
	 * @param location a location
	 * @return whether that location is mountainous
	 */
	@Override
	public boolean isMountainous(final Point location) {
		return mountains.contains(location);
	}

	/**
	 * The rivers, if any, at the given location.
	 * @param location a location
	 * @return the rivers there
	 */
	@Override
	public Iterable<River> getRivers(final Point location) {
		if (rivers.containsKey(location)) {
			return rivers.get(location);
		} else {
			return EnumSet.noneOf(River.class);
		}
	}

	/**
	 * The primary forest, if any, at the given location.
	 * @param location a location
	 * @return any forests there
	 */
	@Override
	@Nullable
	public Forest getForest(final Point location) {
		return forests.get(location);
	}

	/**
	 * The base ground, if any, at the given location.
	 * @param location a location
	 * @return the ground there
	 */
	@Override
	@Nullable
	public Ground getGround(final Point location) {
		return ground.get(location);
	}

	/**
	 * Any fixtures other than mountain, rivers, primary forest, and primary ground at
	 * the given location.
	 * @param location a location
	 * @return any other fixtures there
	 */
	@Override
	public Iterable<TileFixture> getOtherFixtures(final Point location) {
		if (fixtures.containsKey(location)) {
			return fixtures.get(location);
		} else {
			return Collections.emptyList();
		}
	}

	/**
	 * A stream of fixtures other than mountain, rivers, primary forest, and primary
	 * ground at the given location.
	 *
	 * @param location a location
	 * @return a stream of any other fixtures there
	 */
	@Override
	public Stream<TileFixture> streamOtherFixtures(final Point location) {
		if (fixtures.containsKey(location)) {
			return fixtures.get(location).stream();
		} else {
			return Stream.empty();
		}
	}
	/**
	 * The current turn.
	 * @return the current turn
	 */
	@Override
	public int getCurrentTurn() {
		return turn;
	}

	/**
	 * Set the current turn.
	 * @param currentTurn the new current turn
	 */
	@Override
	public void setCurrentTurn(final int currentTurn) {
		turn = currentTurn;
	}

	/**
	 * The current player.
	 * @return the current player
	 */
	@Override
	public Player getCurrentPlayer() {
		return playerCollection.getCurrentPlayer();
	}

	/**
	 * Set the current player.
	 * @param player the new current player
	 */
	@Override
	public void setCurrentPlayer(final Player player) {
		playerCollection.getCurrentPlayer().setCurrent(false);
		playerCollection.getPlayer(player.getPlayerId()).setCurrent(true);
	}

	/**
	 * Whether an object is an equal map.
	 * @param obj an object
	 * @return whether it's a map equal to this one
	 */
	@Override
	public boolean equals(@Nullable final Object obj) {
		return (obj == this) || ((obj instanceof IMapNG) && equalsImpl((IMapNG) obj));
	}

	/**
	 * Whether a map is equal. TODO: Make a default method on IMapNG?
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
									 (isMountainous(point) == obj.isMountainous(point)
									 ) &&
									 areIterablesEqual(getRivers(point),
											 obj.getRivers(point)) &&
									 areStreamsEqual(streamAllFixtures(point),
											 obj.streamAllFixtures(point)));
		} else {
			return false;
		}
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
	 * A String representation of the map.
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder(2048);
		try (final Formatter formatter = new Formatter(builder)) {
			formatter.format("SPMapNG:%nMap version: %d%nRows: %d%nColumns: %d%n",
					Integer.valueOf(dimensions().version),
					Integer.valueOf(dimensions().rows),
					Integer.valueOf(dimensions().cols));
			formatter.format("Current Turn: %d%n%nPlayers:%n", Integer.valueOf(turn));
			for (final Player player : players()) {
				if (player.isCurrent()) {
					formatter.format("%s (current)%n", player.toString());
				} else {
					formatter.format("%s%n", player.toString());
				}
			}
			formatter.format("%nContents:%n");
			for (final Point location : locations()) {
				if (isLocationEmpty(location)) {
					continue;
				}
				formatter.format("At %s: ", location.toString());
				if (getBaseTerrain(location) != TileType.NotVisible) {
					formatter
							.format("terrain: %s, ", getBaseTerrain(location).toString());
				}
				if (isMountainous(location)) {
					formatter.format("mountains, ");
				}
				if (getGround(location) != null) {
					formatter.format("ground: %s, ",
							Objects.toString(getGround(location)));
				}
				if (getForest(location) != null) {
					formatter.format("forest: %s, ", Objects.toString(getForest(location)));
				}
				if (StreamSupport.stream(getRivers(location).spliterator(), false)
							.count() > 0) {
					formatter.format("rivers: ");
					for (final River river : getRivers(location)) {
						formatter.format(" %s", river.toString());
					}
					formatter.format(", ");
				}
				// TODO: Use fixtures.containsKey() instead of the Stream op here
				if (streamOtherFixtures(location).anyMatch(x -> true)) {
					formatter.format("other: ");
					for (final TileFixture fixture : getOtherFixtures(location)) {
						formatter.format("%n%s", fixture.toString());
						// formatter.format(" (%s)", fixture.getClass().getSimpleName());
					}
				}
				formatter.format("%n");
			}
		}
		return builder.toString();
	}

	/**
	 * Add a player.
	 * @param player the player to add
	 */
	@Override
	public void addPlayer(final Player player) {
		playerCollection.add(player);
	}

	/**
	 * Set the base terrain at a location.
	 * @param location    a location
	 * @param terrainType the terrain there
	 */
	@Override
	public void setBaseTerrain(final Point location, final TileType terrainType) {
		terrain.put(location, terrainType);
	}

	/**
	 * Set whether a location is mountainous.
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
	 * Add rivers at a location.
	 * @param location    a location
	 * @param addedRivers rivers to add to that location
	 */
	@Override
	public void addRivers(final Point location,
						  final @NonNull River @NonNull ... addedRivers) {
		final EnumSet<@NonNull River> localRivers;
		if (rivers.containsKey(location)) {
			localRivers = rivers.get(location);
		} else {
			localRivers = EnumSet.noneOf(River.class);
			rivers.put(location, localRivers);
		}
		Collections.addAll(localRivers, addedRivers);
	}

	/**
	 * Remove rivers from the given location.
	 * @param location      a location
	 * @param removedRivers rivers to remove from it
	 */
	@Override
	public void removeRivers(final Point location, final River... removedRivers) {
		if (rivers.containsKey(location)) {
			final Set<River> localRivers = rivers.get(location);
			Stream.of(removedRivers).forEach(localRivers::remove);
		}
	}

	/**
	 * Set the primary forest at the given location.
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
	 * Set the main ground at a location.
	 * @param location  a location
	 * @param newGround what the ground there should be, if any
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
	 * Add a fixture at a location.
	 * @param location a location
	 * @param fix      a fixture to add there
	 */
	@Override
	public void addFixture(final Point location, final TileFixture fix) {
		// If we use a lambda in anyMatch(), many tests error with
		// BoostrapMethodError caused by LambdaConversionException
		// trying to convert HasMutableImage to IFixture.
		//noinspection Convert2MethodRef
		if (Stream.of(getForest(location), getGround(location))
					.anyMatch(newFix -> fix.equalsIgnoringID(newFix))) {
			return;
		}
		final Collection<TileFixture> local =
				MultiMapHelper.getMapValue(fixtures, location, key -> new ArraySet<>());
		if (fix.getID() >= 0) {
			final Predicate<TileFixture> matcher = item -> item.getID() == fix.getID();
			final boolean found =
					local.stream().anyMatch(matcher);
			if (found) {
				final TileFixture existing = local.stream().filter(matcher).findAny()
													 .orElseThrow(
															 () -> new
																		   IllegalStateException("Fixture vanished"));

				if (existing.equals(fix) ||
							((existing instanceof SubsettableFixture) &&
									 ((SubsettableFixture) existing)
											 .isSubset(fix, NullStream.DEV_NULL,
													 "")) ||
							((fix instanceof SubsettableFixture) &&
									 ((SubsettableFixture) fix)
											 .isSubset(existing, NullStream.DEV_NULL,
													 ""))) {
					local.remove(existing);
					local.add(fix);
				} else {
					local.add(fix);
					LOGGER.log(Level.WARNING,
							"Inserted duplicate-ID fixture at " + location);
					LOGGER.log(Level.FINE, "Stack trace of this location: ",
							new Throwable());
					LOGGER.fine("Existing fixture was: " + existing.shortDesc());
					LOGGER.fine("Added: " + fix.shortDesc());
				}
			} else {
				local.add(fix);
			}
		} else {
			local.add(fix);
		}
	}

	/**
	 * Remove a fixture from a location.
	 * @param location a location
	 * @param fix      a fixture to remove from that location
	 */
	@Override
	public void removeFixture(final Point location, final TileFixture fix) {
		if (fixtures.containsKey(location)) {
			fixtures.get(location).remove(fix);
		}
	}

	/**
	 * Clone a map, possibly for a specific player, who shouldn't see other players'
	 * details.
	 * @param zero whether to "zero" sensitive data (probably just DCs)
	 * @param player the player for whom the map is being prepared, or null for none
	 * @return a copy of this map
	 */
	@Override
	public IMapNG copy(final boolean zero, final @Nullable Player player) {
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
				retval.addFixture(point,
						fixture.copy(zero && shouldZero(fixture, player)));
			}
		}
		return retval;
	}
	/**
	 * Whether the given fixture should be zeroed out if the map is for the given player.
	 * @param player a player
	 * @param fixture a fixture
	 * @return false if the fixture is a HasOwner owned by the player, and true otherwise
	 */
	private static boolean shouldZero(final TileFixture fixture,
									  @Nullable final Player player) {
		if (fixture instanceof HasOwner) {
			return !Objects.equals(player, ((HasOwner) fixture).getOwner());
		} else {
			return true;
		}
	}
}

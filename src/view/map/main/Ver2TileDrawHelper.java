package view.map.main;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import model.map.HasImage;
import model.map.IMapNG;
import model.map.Point;
import model.map.PointFactory;
import model.map.River;
import model.map.TerrainFixture;
import model.map.TileFixture;
import model.map.fixtures.RiverFixture;
import model.viewer.FixtureMatcher;
import model.viewer.TileTypeFixture;
import model.viewer.ZOrderFilter;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import util.ImageLoader;
import util.TypesafeLogger;
import view.util.Coordinate;

/**
 * A TileDrawHelper for the new map version.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2011-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public final class Ver2TileDrawHelper extends AbstractTileDrawHelper {
	/**
	 * The filename of the fallback image.
	 */
	private static final String FALLBACK_FILE = "event_fallback.png";
	/**
	 * Logger.
	 */
	private static final Logger LOGGER =
			TypesafeLogger.getLogger(Ver2TileDrawHelper.class);
	/**
	 * Image cache.
	 */
	private final ImageLoader loader = ImageLoader.getLoader();
	/**
	 * The images we've already determined aren't there.
	 */
	private final Collection<String> missingFiles = new HashSet<>();
	/**
	 * A mapping from river-sets to filenames.
	 */
	private final Map<Set<River>, String> riverFiles = new HashMap<>();
	/**
	 * Comparator to find which fixture to draw.
	 */
	private final Comparator<@NonNull TileFixture> fixComp;
	/**
	 * The observer to be notified when images finish drawing.
	 */
	private final ImageObserver observer;
	/**
	 * The object to query about whether we should display a given tile.
	 */
	private final ZOrderFilter zof;
	/**
	 * The matchers to use to say what's on top.
	 */
	private final Iterable<FixtureMatcher> fixMatchers;
	/**
	 * A fallback image.
	 */
	private Image fallbackImage;
	/**
	 * Constructor. We need to initialize the cache.
	 *
	 * @param imageObserver the class to notify when images finish drawing.
	 * @param filter        the class to query about whether to display a fixture
	 * @param matchers      a series of matchers to use to determine what's on top
	 */
	public Ver2TileDrawHelper(final ImageObserver imageObserver,
							  final ZOrderFilter filter,
							  final Iterable<FixtureMatcher> matchers) {
		observer = imageObserver;
		zof = filter;
		fixMatchers = matchers;
		fixComp = (o1, o2) -> {
			for (final FixtureMatcher matcher : fixMatchers) {
				if (matcher.matches(o1)) {
					if (matcher.matches(o2)) {
						return 0;
					} else {
						return -1;
					}
				} else if (matcher.matches(o2)) {
					return 1;
				}
			}
			return 0;
		};
		createRiverFiles();
		final String[] files = {"trees.png", "mountain.png"};
		for (final String file : files) {
			if (file != null) {
				try {
					loader.loadImage(file);
				} catch (final IOException e) {
					logLoadingError(e, file, false);
				}
			}
		}
		try {
			fallbackImage = loader.loadImage(FALLBACK_FILE);
		} catch (final IOException e) {
			logLoadingError(e, FALLBACK_FILE, true);
			fallbackImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		}
	}

	/**
	 * Log, but otherwise ignore, a file-not-found or other I/O error from loading an
	 * image.
	 *
	 * @param except   the exception we're handling
	 * @param filename the file that we were trying to load from
	 * @param fallback true if this is the fallback image (meaning it's a big problem if
	 *                 it's missing), false otherwise
	 */
	private static void logLoadingError(final IOException except,
										final String filename, final boolean fallback) {
		if (except instanceof FileNotFoundException ||
					except instanceof NoSuchFileException) {
			final String msg = "Image " + filename + " not found";
			if (fallback) {
				LOGGER.log(Level.SEVERE, msg, except);
			} else {
				LOGGER.log(Level.INFO, msg, except);
			}
		} else {
			//noinspection HardcodedFileSeparator
			LOGGER.log(Level.SEVERE,
					"I/O error while loading image " + filename, except);
		}
	}

	/**
	 * Turn a series of rivers into a Set.
	 * @param rivers any number of rivers
	 * @return a set containing them
	 */
	private static Set<River> createRiverSet(final @NonNull River @NonNull ... rivers) {
		final Set<@NonNull River> set =
				EnumSet.noneOf(River.class);
		Collections.addAll(set, rivers);
		return set;
	}

	/**
	 * Use the "Z-order filter"'s hash code.
	 * @return a hash value for the object
	 */
	@Override
	public int hashCode() {
		return zof.hashCode();
	}

	/**
	 * An object is equal iff it is a Ver2TileDrawHelper with an equal list of matchers.
	 * @param obj an object
	 * @return whether it's the same as this
	 */
	@Override
	public boolean equals(@Nullable final Object obj) {
		return (this == obj) ||
					   ((obj instanceof Ver2TileDrawHelper) && fixMatchers.equals(
							   ((Ver2TileDrawHelper) obj).fixMatchers));
	}

	/**
	 * Draw a tile at the specified coordinates. Because this is at present only
	 * called in
	 * a loop that's the last thing before the context is disposed, we alter the state
	 * freely and don't restore it.
	 *
	 * @param pen         the graphics context.
	 * @param map         the map to draw the tile from
	 * @param location    the location to draw
	 * @param coordinates the coordinates of the tile's upper-left corner
	 * @param dimensions  the width (X) and height (Y) of the tile
	 */
	@Override
	public void drawTile(final Graphics pen, final IMapNG map, final Point location,
						 final Coordinate coordinates, final Coordinate dimensions) {
		if (needsFixtureColor(map, location)) {
			pen.setColor(getFixtureColor(map, location));
		} else {
			pen.setColor(getTileColor(2, map.getBaseTerrain(location)));
		}
		pen.fillRect(coordinates.x, coordinates.y, dimensions.x, dimensions.y);
		if (map.getRivers(location).iterator().hasNext()) {
			pen.drawImage(getRiverImage(map.getRivers(location)), coordinates.x,
					coordinates.y, dimensions.x, dimensions.y, observer);
		}
		if (hasFixture(map, location)) {
			pen.drawImage(getImageForFixture(getTopFixture(map, location).orElseThrow(
					() -> new IllegalArgumentException("No top fixture"))),
					coordinates.x, coordinates.y, dimensions.x, dimensions.y,
					observer);
		} else if (map.isMountainous(location)) {
			pen.drawImage(getImage("mountain.png"), coordinates.x, coordinates.y,
					dimensions.x, dimensions.y, observer);
		}
		pen.setColor(Color.black);
		pen.drawRect(coordinates.x, coordinates.y, dimensions.x, dimensions.y);
	}

	/**
	 * Draw a tile at the upper-left corner of the drawing surface.
	 *
	 * @param pen      the graphics context
	 * @param map      the map to draw the tile from
	 * @param location the location to draw
	 * @param width    the width of the drawing area
	 * @param height   the height of the drawing area
	 */
	@Override
	public void drawTileTranslated(final Graphics pen, final IMapNG map,
								   final Point location, final int width,
								   final int height) {
		drawTile(pen, map, location, PointFactory.coordinate(0, 0),
				PointFactory.coordinate(width, height));
	}

	/**
	 * A Stream of the drawable fixtures at the given location.
	 * @param map      a map
	 * @param location a location
	 * @return a Stream of the drawable fixtures there
	 */
	private Stream<TileFixture> getDrawableFixtures(final IMapNG map,
													final Point location) {
		return Stream.concat(
				Stream.of(map.getGround(location), map.getForest(location))
						.filter(Objects::nonNull), map.streamOtherFixtures(location))
					   .filter(fix -> !(fix instanceof TileTypeFixture))
					   .filter(zof::shouldDisplay).sorted(fixComp);
	}

	/**
	 * Whether the map has any fixtures at the given location.
	 * @param map      a map
	 * @param location a location
	 * @return whether there are any fixtures worth drawing there
	 */
	private boolean hasFixture(final IMapNG map, final Point location) {
		return getDrawableFixtures(map, location).anyMatch(x -> true);
	}

	/**
	 * Get the image representing the given configuration of rivers.
	 * @param rivers a collection of rivers
	 * @return an image representing them
	 */
	private Image getRiverImage(final Iterable<River> rivers) {
		if (rivers instanceof Set<?>) {
			return getImage(riverFiles.get(rivers));
		} else {
			return getImage(riverFiles.get(
					StreamSupport.stream(rivers.spliterator(), false)
							.collect(Collectors.toSet())));
		}
	}

	/**
	 * Get the "top" fixture at the given location.
	 * @param map      a map
	 * @param location a location
	 * @return the top fixture there
	 */
	private Optional<TileFixture> getTopFixture(final IMapNG map, final Point location) {
		return getDrawableFixtures(map, location).findFirst();
	}

	/**
	 * Whether we need a different color to show a non-top fixture at the given location.
	 * @param map      a map
	 * @param location a location
	 * @return whether we needs a different color to show a non-top fixture there (like a
	 * forest or mountain)
	 */
	private boolean needsFixtureColor(final IMapNG map, final Point location) {
		if (hasTerrainFixture(map, location)) {
			final Optional<TileFixture> topFixture = getTopFixture(map, location);
			if (topFixture.isPresent()) {
				// getLast() equivalent from http://stackoverflow.com/a/21441634
				final Optional<TileFixture> bottomTerrain = getDrawableFixtures(map,
						location).filter(TerrainFixture.class::isInstance).reduce(
						(first, second) -> second);
				return (bottomTerrain.isPresent() &&
							   !topFixture.get().equals(bottomTerrain.get())) ||
										map.isMountainous(location);
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	/**
	 * Whether there is a "terrain fixture" at the given location.
	 * @param map      a map
	 * @param location a location
	 * @return whether there is a terrain fixture there
	 */
	private boolean hasTerrainFixture(final IMapNG map, final Point location) {
		return getDrawableFixtures(map, location)
					   .anyMatch(TerrainFixture.class::isInstance) ||
					   (getDrawableFixtures(map, location).anyMatch(x -> true) &&
								map.isMountainous(location));
	}

	/**
	 * Get the color representing a "not-on-top" terrain fixture at the given location.
	 * @param map      a map
	 * @param location a location
	 * @return a color to represent the not-on-top terrain feature there.
	 */
	private Color getFixtureColor(final IMapNG map, final Point location) {
		final TileFixture topFixture = getTopFixture(map, location).orElse(null);
		final Optional<Color> retval = getDrawableFixtures(map, location).filter(
				fix -> !Objects.equals(topFixture, fix))
					   .filter(TerrainFixture.class::isInstance)
					   .map(fix -> getHelper().getFeatureColor(fix)).findFirst();
		if (retval.isPresent()) {
			return retval.get();
		} else if (map.isMountainous(location)) {
			return TileUIHelper.MOUNTAIN_COLOR;
		} else {
			return getTileColor(2, map.getBaseTerrain(location));
		}
	}

	/**
	 * Get the image representing the given fixtuer.
	 * @param fix a TileFixture
	 * @return an Image to draw to represent it.
	 */
	private Image getImageForFixture(final TileFixture fix) {
		if (fix instanceof HasImage) {
			final String image = ((HasImage) fix).getImage();
			if (image.isEmpty() || missingFiles.contains(image)) {
				return getImage(((HasImage) fix).getDefaultImage());
			} else {
				return getImage(image);
			}
		} else if (fix instanceof RiverFixture) {
			return getImage(riverFiles.get(((RiverFixture) fix).getRivers()));
		} else {
			LOGGER.warning("Using fallback image for unexpected kind of Fixture.");
			return fallbackImage;
		}
	}

	/**
	 * Return either a loaded image or a generic one.
	 *
	 * @param filename the name of the file containing the image
	 * @return that image, or, if it fails to load, the generic one.
	 */
	@SuppressWarnings("StringConcatenationMissingWhitespace")
	private Image getImage(final String filename) {
		try {
			return loader.loadImage(filename);
		} catch (final FileNotFoundException | NoSuchFileException e) {
			if (!missingFiles.contains(filename)) {
				LOGGER.log(Level.SEVERE,
						"images" + File.separatorChar + filename + " not found");
				LOGGER.log(Level.FINEST, "with stack trace", e);
				missingFiles.add(filename);
			}
			return fallbackImage;
		} catch (final IOException e) {
			//noinspection HardcodedFileSeparator
			LOGGER.log(Level.SEVERE,
					"I/O error reading image images" + File.separatorChar + filename, e);
			return fallbackImage;
		}
	}

	/**
	 * Create the mapping from river-sets to filenames.
	 */
	private void createRiverFiles() {
		riverFiles.put(EnumSet.noneOf(River.class), "riv00.png");
		riverFiles.put(createRiverSet(River.North), "riv01.png");
		riverFiles.put(createRiverSet(River.East), "riv02.png");
		riverFiles.put(createRiverSet(River.South), "riv03.png");
		riverFiles.put(createRiverSet(River.West), "riv04.png");
		riverFiles.put(createRiverSet(River.Lake), "riv05.png");
		riverFiles.put(createRiverSet(River.North, River.East), "riv06.png");
		riverFiles.put(createRiverSet(River.North, River.South), "riv07.png");
		riverFiles.put(createRiverSet(River.North, River.West), "riv08.png");
		riverFiles.put(createRiverSet(River.North, River.Lake), "riv09.png");
		riverFiles.put(createRiverSet(River.East, River.South), "riv10.png");
		riverFiles.put(createRiverSet(River.East, River.West), "riv11.png");
		riverFiles.put(createRiverSet(River.East, River.Lake), "riv12.png");
		riverFiles.put(createRiverSet(River.South, River.West), "riv13.png");
		riverFiles.put(createRiverSet(River.South, River.Lake), "riv14.png");
		riverFiles.put(createRiverSet(River.West, River.Lake), "riv15.png");
		riverFiles.put(createRiverSet(River.North, River.East, River.South),
				"riv16.png");
		riverFiles.put(createRiverSet(River.North, River.East, River.West),
				"riv17.png");
		riverFiles.put(createRiverSet(River.North, River.East, River.Lake),
				"riv18.png");
		riverFiles.put(createRiverSet(River.North, River.South, River.West),
				"riv19.png");
		riverFiles.put(createRiverSet(River.North, River.South, River.Lake),
				"riv20.png");
		riverFiles.put(createRiverSet(River.North, River.West, River.Lake),
				"riv21.png");
		riverFiles.put(createRiverSet(River.East, River.South, River.West),
				"riv22.png");
		riverFiles.put(createRiverSet(River.East, River.South, River.Lake),
				"riv23.png");
		riverFiles.put(createRiverSet(River.East, River.West, River.Lake),
				"riv24.png");
		riverFiles.put(createRiverSet(River.South, River.West, River.Lake),
				"riv25.png");
		riverFiles.put(createRiverSet(River.North, River.East, River.South,
				River.West), "riv26.png");
		riverFiles.put(createRiverSet(River.North, River.South, River.West,
				River.Lake), "riv27.png");
		riverFiles.put(createRiverSet(River.North, River.East, River.West,
				River.Lake), "riv28.png");
		riverFiles.put(createRiverSet(River.North, River.East, River.South,
				River.Lake), "riv29.png");
		riverFiles.put(createRiverSet(River.East, River.South, River.West,
				River.Lake), "riv30.png");
		riverFiles.put(EnumSet.allOf(River.class), "riv31.png");
	}

	/**
	 * A trivial toString().
	 * @return a String representation of the object
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "Ver2TileDrawHelper";
	}
}

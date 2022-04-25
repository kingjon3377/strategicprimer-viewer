package drivers.map_viewer;

import java.nio.file.NoSuchFileException;

import java.util.stream.Stream;
import lovelace.util.LovelaceLogger;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Map;
import java.util.Collections;
import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;

import java.awt.Image;
import java.awt.Graphics;
import java.awt.Color;

import static drivers.map_viewer.ImageLoader.ColorHelper;

import java.awt.image.ImageObserver;
import java.awt.image.BufferedImage;

import java.io.IOException;
import java.io.FileNotFoundException;

import common.map.HasImage;
import common.map.Point;
import common.map.TileFixture;
import common.map.IMapNG;
import common.map.FakeFixture;

import common.map.fixtures.TerrainFixture;

import drivers.common.FixtureMatcher;

import java.util.function.Predicate;
import java.util.List;

import common.map.River;
import common.map.Direction;

/**
 * A {@link TileDrawHelper} for version-2 maps.
 */
public class Ver2TileDrawHelper implements TileDrawHelper {
	/**
	 * The object to arrange to be notified as images finish drawing.
	 */
	private final ImageObserver observer;

	/**
	 * The object to query about whether to display a fixture.
	 */
	private final Predicate<TileFixture> filter;

	/**
	 * A series of matchers to use to determine what's on top.
	 */
	private final List<FixtureMatcher> matchers;

	public Ver2TileDrawHelper(final ImageObserver observer, final Predicate<TileFixture> filter,
	                          final FixtureMatcher... matchers) {
		this.observer = observer;
		this.filter = filter;
		this.matchers = List.of(matchers);
		for (final String file : Arrays.asList("trees.png", "mountain.png")) {
			try {
				ImageLoader.loadImage(file);
			} catch (final FileNotFoundException|NoSuchFileException except) {
				LovelaceLogger.info(except, "Image %s not found", file);
			} catch (final IOException except) {
				LovelaceLogger.error(except, "I/O error while loading image %s", file);
			}
		}
	}

	public Ver2TileDrawHelper(final ImageObserver observer, final Predicate<TileFixture> filter,
	                          final Iterable<FixtureMatcher> matchers) {
		this.observer = observer;
		this.filter = filter;
		final List<FixtureMatcher> temp = new ArrayList<>();
		matchers.forEach(temp::add);
		this.matchers = Collections.unmodifiableList(temp);
		for (final String file : Arrays.asList("trees.png", "mountain.png")) {
			try {
				ImageLoader.loadImage(file);
			} catch (final FileNotFoundException|NoSuchFileException except) {
				LovelaceLogger.info(except, "Image %s not found", file);
			} catch (final IOException except) {
				LovelaceLogger.error(except, "I/O error while loading image %s", file);
			}
		}
	}

	/**
	 * A comparator to put fixtures in order by the order of the first
	 * matcher that matches them.
	 */
	private int compareFixtures(final TileFixture one, final TileFixture two) {
		for (final FixtureMatcher matcher : matchers) {
			if (matcher.matches(one)) {
				if (matcher.matches(two)) {
					return 0;
				} else {
					return -1;
				}
			} else if (matcher.matches(two)) {
				return 1;
			}
		}
		return 0;
	}

	/**
	 * Images we've already determined aren't there.
	 */
	private final Set<String> missingFiles = new HashSet<>();

	/**
	 * Create the fallback image---made a method so the object reference can be immutable
	 */
	private static Image createFallbackImage() {
		final Image fallbackFallback = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		final String filename = "event_fallback.png";
		try {
			return ImageLoader.loadImage(filename);
		} catch (final FileNotFoundException|NoSuchFileException except) {
			LovelaceLogger.error(except,"Image %s not found", filename);
			return fallbackFallback;
		} catch (final IOException except) {
			LovelaceLogger.error(except, "I/O error while loading image %s", filename);
			return fallbackFallback;
		}
	}

	/**
	 * A fallback image for when an image file is missing or fails to load.
	 */
	private final Image fallbackImage = createFallbackImage();

	/**
	 * Get the color representing a "not-on-top" terrain fixture at the given location.
	 */
	private @Nullable Color getFixtureColor(final IMapNG map, final Point location) {
		final TileFixture top = getTopFixture(map, location);
		if (top != null) {
			final Color color = getDrawableFixtures(map, location)
					.filter(f -> !top.equals(f)).filter(TerrainFixture.class::isInstance)
					.map(TerrainFixture.class::cast).findFirst()
					.map(ColorHelper::getFeatureColor).orElse(null);
			if (color != null) {
				return color;
			} else if (map.isMountainous(location)) {
				return ColorHelper.MOUNTAIN_COLOR;
			}
		}

		return ColorHelper.get(map.getDimensions().getVersion(), map.getBaseTerrain(location));
	}

	/**
	 * Return either a loaded image or, if the specified image fails to load, the generic one.
	 */
	private Image getImage(final String filename) {
		try {
			return ImageLoader.loadImage(filename);
		} catch (final FileNotFoundException|NoSuchFileException except) {
			if (!missingFiles.contains(filename)) {
				LovelaceLogger.error("images/%s not found", filename);
				LovelaceLogger.debug(except, "with stack trace");
				missingFiles.add(filename);
			}
			return fallbackImage;
		} catch (final IOException except) {
			LovelaceLogger.error(except, "I/O error reading image images/%s", filename);
			return fallbackImage;
		}
	}

	/**
	 * Get the image representing the given fixture.
	 */
	private Image getImageForFixture(final TileFixture fixture) {
		if (fixture instanceof HasImage) {
			final String image = ((HasImage) fixture).getImage();
			if (image.isEmpty() || missingFiles.contains(image)) {
				return getImage(((HasImage) fixture).getDefaultImage());
			} else {
				return getImage(image);
			}
		} else {
			LovelaceLogger.warning("Using fallback image for unexpected kind of fixture");
			return fallbackImage;
		}
	}

	/**
	 * Draw an icon at the specified coordinates.
	 */
	private void drawIcon(final Graphics pen, final String icon, final Coordinate coordinates, final Coordinate dimensions) {
		final Image image = getImage(icon);
		pen.drawImage(image, coordinates.getX(), coordinates.getY(),
			dimensions.getX(), dimensions.getY(), observer);
	}

	/**
	 * Draw an icon at the specified coordinates.
	 */
	private void drawIcon(final Graphics pen, final Image icon, final Coordinate coordinates, final Coordinate dimensions) {
		pen.drawImage(icon, coordinates.getX(), coordinates.getY(),
			dimensions.getX(), dimensions.getY(), observer);
	}

	/**
	 * Draw a tile at the specified coordinates. Because this is at present
	 * only called in a loop that's the last thing before the graphics
	 * context is disposed, we alter the state freely and don't restore it.
	 */
	@Override
	public void drawTile(final Graphics pen, final IMapNG map, final Point location,
	                     final Coordinate coordinates, final Coordinate dimensions) {
		final Color localColor;
		if (needsFixtureColor(map, location)) {
			localColor = getFixtureColor(map, location);
		} else {
			localColor = ColorHelper.get(map.getDimensions().getVersion(),
				map.getBaseTerrain(location));
		}
		if (localColor != null) {
			pen.setColor(localColor);
			pen.fillRect(coordinates.getX(), coordinates.getY(),
				dimensions.getX(), dimensions.getY());
		}
		for (final River river : map.getRivers(location)) {
			// TODO: Do something to avoid String.format(), which is probably slow
			drawIcon(pen, String.format("river%d.png", river.ordinal()),
				coordinates, dimensions);
		}
		for (final Map.Entry<Direction, Integer> entry : map.getRoads(location).entrySet()) {
			// TODO: Do something to avoid String.format(), which is probably slow
			drawIcon(pen, String.format("road%d.png", entry.getKey().ordinal()),
				coordinates, dimensions);
		}
		final TileFixture top = getTopFixture(map, location);
		if (top != null) {
			drawIcon(pen, getImageForFixture(top), coordinates, dimensions);
		} else if (map.isMountainous(location)) {
			drawIcon(pen, "mountain.png", coordinates, dimensions);
		}
		if (map.getBookmarks().contains(location)) {
			drawIcon(pen, "bookmark.png", coordinates, dimensions);
		}
		pen.setColor(Color.black);
		pen.drawRect(coordinates.getX(), coordinates.getY(), dimensions.getX(), dimensions.getY());
	}

	/**
	 * The drawable fixtures at the given location.
	 */
	private Stream<TileFixture> getDrawableFixtures(final IMapNG map, final Point location) {
		return map.getFixtures(location).stream().filter(f -> !(f instanceof FakeFixture))
			.filter(filter).sorted(this::compareFixtures);
	}

	/**
	 * Get the "top" fixture at the given location
	 */
	private @Nullable TileFixture getTopFixture(final IMapNG map, final Point location) {
		return getDrawableFixtures(map, location).findFirst().orElse(null);
	}

	/**
	 * Whether there is a "terrain fixture" at the gtiven location.
	 */
	private boolean hasTerrainFixture(final IMapNG map, final Point location) {
		// TODO: Should we really return true if there is exactly one drawable fixture that happens to be a terrain fixture?
		if (getDrawableFixtures(map, location).anyMatch(TerrainFixture.class::isInstance)) {
			return true;
		} else {
			return getDrawableFixtures(map, location).anyMatch(x -> true) &&
					       map.isMountainous(location);
		}
	}

	/**
	 * Whether we need a different background color to show a non-top
	 * fixture (forest, for example) at the given location.
	 */
	private boolean needsFixtureColor(final IMapNG map, final Point location) {
		final TileFixture top = getTopFixture(map, location);
		if (hasTerrainFixture(map, location) && top != null) {
			final TileFixture bottom = getDrawableFixtures(map, location)
					.filter(TerrainFixture.class::isInstance)
					.map(TerrainFixture.class::cast)
					.reduce((first, second) -> second)
					.orElse(null);
			if (bottom == null) {
				return map.isMountainous(location);
			} else {
				return !top.equals(bottom);
			}
		} else {
			return false;
		}
	}
}

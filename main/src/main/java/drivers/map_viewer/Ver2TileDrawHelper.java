package drivers.map_viewer;

import java.nio.file.NoSuchFileException;

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
import java.util.stream.StreamSupport;
import java.util.stream.Collectors;
import java.util.logging.Logger;
import java.util.logging.Level;

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

	private static final Logger LOGGER = Logger.getLogger(Ver2TileDrawHelper.class.getName());

	public Ver2TileDrawHelper(final ImageObserver observer, final Predicate<TileFixture> filter,
	                          final FixtureMatcher... matchers) {
		this.observer = observer;
		this.filter = filter;
		this.matchers = Collections.unmodifiableList(Arrays.asList(matchers));
		for (String file : Arrays.asList("trees.png", "mountain.png")) {
			try {
				ImageLoader.loadImage(file);
			} catch (FileNotFoundException|NoSuchFileException except) {
				LOGGER.log(Level.INFO, String.format("Image %s not found", file), except);
			} catch (final IOException except) {
				LOGGER.log(Level.SEVERE, "I/O error while loading image " + file, except);
			}
		}
	}

	public Ver2TileDrawHelper(final ImageObserver observer, final Predicate<TileFixture> filter,
	                          final Iterable<FixtureMatcher> matchers) {
		this.observer = observer;
		this.filter = filter;
		List<FixtureMatcher> temp = new ArrayList<>();
		matchers.forEach(temp::add);
		this.matchers = Collections.unmodifiableList(temp);
		for (String file : Arrays.asList("trees.png", "mountain.png")) {
			try {
				ImageLoader.loadImage(file);
			} catch (FileNotFoundException|NoSuchFileException except) {
				LOGGER.log(Level.INFO, String.format("Image %s not found", file), except);
			} catch (final IOException except) {
				LOGGER.log(Level.SEVERE, "I/O error while loading image " + file, except);
			}
		}
	}

	/**
	 * A comparator to put fixtures in order by the order of the first
	 * matcher that matches them.
	 */
	private int compareFixtures(final TileFixture one, final TileFixture two) {
		for (FixtureMatcher matcher : matchers) {
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
		Image fallbackFallback = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		String filename = "event_fallback.png";
		try {
			return ImageLoader.loadImage(filename);
		} catch (FileNotFoundException|NoSuchFileException except) {
			LOGGER.log(Level.SEVERE, String.format("Image %s not found", filename), except);
			return fallbackFallback;
		} catch (final IOException except) {
			LOGGER.log(Level.SEVERE, "I/O error while loading image " + filename, except);
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
	@Nullable
	private Color getFixtureColor(final IMapNG map, final Point location) {
		TileFixture top = getTopFixture(map, location);
		if (top != null) {
			Color color =
				StreamSupport.stream(getDrawableFixtures(map, location).spliterator(), false)
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
		} catch (FileNotFoundException|NoSuchFileException except) {
			if (!missingFiles.contains(filename)) {
				LOGGER.severe(String.format("images/%s not found", filename));
				LOGGER.log(Level.FINE, "with stack trace", except);
				missingFiles.add(filename);
			}
			return fallbackImage;
		} catch (final IOException except) {
			LOGGER.log(Level.SEVERE, "I/O error reading image images/" + filename, except);
			return fallbackImage;
		}
	}

	/**
	 * Get the image representing the given fixture.
	 */
	private Image getImageForFixture(final TileFixture fixture) {
		if (fixture instanceof HasImage) {
			String image = ((HasImage) fixture).getImage();
			if (image.isEmpty() || missingFiles.contains(image)) {
				return getImage(((HasImage) fixture).getDefaultImage());
			} else {
				return getImage(image);
			}
		} else {
			LOGGER.warning("Using fallback image for unexpected kind of fixture");
			return fallbackImage;
		}
	}

	/**
	 * Draw an icon at the specified coordinates.
	 */
	private void drawIcon(final Graphics pen, final String icon, final Coordinate coordinates, final Coordinate dimensions) {
		Image image = getImage(icon);
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
		Color localColor;
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
		for (River river : map.getRivers(location)) {
			// TODO: Do something to avoid String.format(), which is probably slow
			drawIcon(pen, String.format("river%d.png", river.ordinal()),
				coordinates, dimensions);
		}
		for (Map.Entry<Direction, Integer> entry : map.getRoads(location).entrySet()) {
			// TODO: Do something to avoid String.format(), which is probably slow
			drawIcon(pen, String.format("road%d.png", entry.getKey().ordinal()),
				coordinates, dimensions);
		}
		TileFixture top = getTopFixture(map, location);
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
	 *
	 * TODO: return Stream instead?
	 */
	private Iterable<TileFixture> getDrawableFixtures(final IMapNG map, final Point location) {
		return map.getFixtures(location).stream().filter(f -> !(f instanceof FakeFixture))
			.filter(filter).sorted(this::compareFixtures).collect(Collectors.toList());
	}

	/**
	 * Get the "top" fixture at the given location
	 */
	@Nullable
	private TileFixture getTopFixture(final IMapNG map, final Point location) {
		// if we change getDrawbleFixtures() to return Stream, use findFirst().orElse(null)
		Iterable<TileFixture> fixtures = getDrawableFixtures(map, location);
		if (fixtures.iterator().hasNext()) {
			return fixtures.iterator().next();
		} else {
			return null;
		}
	}

	/**
	 * Whether there is a "terrain fixture" at the gtiven location.
	 */
	private boolean hasTerrainFixture(final IMapNG map, final Point location) {
		// TODO: Should we really return true if there is exactly one drawable fixture that happens to be a terrain fixture?
		if (StreamSupport.stream(getDrawableFixtures(map, location).spliterator(), false)
				.anyMatch(TerrainFixture.class::isInstance)) {
			return true;
		} else if (getDrawableFixtures(map, location).iterator().hasNext() &&
				map.isMountainous(location)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Whether we need a different background color to show a non-top
	 * fixture (forest, for example) at the given location.
	 */
	private boolean needsFixtureColor(final IMapNG map, final Point location) {
		TileFixture top = getTopFixture(map, location);
		if (hasTerrainFixture(map, location) && top != null) {
			TileFixture bottom =
				StreamSupport.stream(getDrawableFixtures(map, location).spliterator(), false)
					.filter(TerrainFixture.class::isInstance)
					.map(TerrainFixture.class::cast)
					.reduce((first, second) -> second)
					.orElse(null);
			if (bottom != null) {
				return !top.equals(bottom);
			} else if (map.isMountainous(location)) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}
}

package view.map.main;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import model.map.HasImage;
import model.map.IFixture;
import model.map.ITile;
import model.map.PointFactory;
import model.map.River;
import model.map.TerrainFixture;
import model.map.TileFixture;
import model.map.fixtures.RiverFixture;
import model.viewer.FixtureComparator;
import model.viewer.ZOrderFilter;

import org.eclipse.jdt.annotation.Nullable;

import util.ImageLoader;
import util.IteratorWrapper;
import util.NullCleaner;
import util.TypesafeLogger;
import view.util.Coordinate;

/**
 * A TileDrawHelper for the new map version.
 *
 * @author Jonathan Lovelace
 *
 */
public class Ver2TileDrawHelper extends AbstractTileDrawHelper {
	/**
	 * Image cache.
	 */
	private final ImageLoader loader = ImageLoader.getLoader();

	/**
	 * The images we've already determined aren't there.
	 */
	private final Set<String> missingFiles = new HashSet<>();
	/**
	 * A mapping from river-sets to filenames.
	 */
	private final Map<Set<River>, String> riverFiles = new HashMap<>();

	/**
	 * The filename of the fallback image.
	 */
	private static final String FALLBACK_FILE = "event_fallback.png";

	/**
	 * Comparator to find which fixture to draw.
	 */
	private final FixtureComparator fixComp = new FixtureComparator();

	/**
	 * @return a hash value forthe object
	 *
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return fixComp.hashCode();
	}

	/**
	 * @param obj an object
	 * @return whether it's the same as this
	 */
	@Override
	public boolean equals(@Nullable final Object obj) {
		return this == obj || obj instanceof Ver2TileDrawHelper
				&& fixComp.equals(((Ver2TileDrawHelper) obj).fixComp);
	}

	/**
	 * The observer to be notified when images finish drawing.
	 */
	private final ImageObserver observer;
	/**
	 * The object to query about whether we should display a given tile.
	 */
	private final ZOrderFilter zof;
	/**
	 * Logger.
	 */
	private static final Logger LOGGER = TypesafeLogger
			.getLogger(Ver2TileDrawHelper.class);
	/**
	 * A fallback image.
	 */
	private Image fallbackImage;

	/**
	 * Constructor. We need to initialize the cache.
	 *
	 * @param iobs the class to notify when images finish drawing.
	 * @param zofilt the class to query about whether to display a fixture
	 */
	public Ver2TileDrawHelper(final ImageObserver iobs,
			final ZOrderFilter zofilt) {
		super();
		observer = iobs;
		zof = zofilt;
		final String[] files = new String[] { "trees.png", "mountain.png" };
		createRiverFiles();
		for (final String file : files) {
			if (file != null) {
				try {
					loader.loadImage(file);
				} catch (IOException e) {
					logLoadingError(e, file, false);
				}
			}
		}
		try {
			fallbackImage = loader.loadImage(FALLBACK_FILE);
		} catch (IOException e) {
			logLoadingError(e, FALLBACK_FILE, true);
			fallbackImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		}
	}

	/**
	 * Log, but otherwise ignore, a file-not-found or other I/O error from
	 * loading an image.
	 *
	 * @param except the exception we're handling
	 * @param filename the file that we were trying to load from
	 * @param fallback true if this is the fallback image (meaning it's a big
	 *        problem if it's missing), false otherwise
	 */
	private static void logLoadingError(final IOException except,
			final String filename, final boolean fallback) {
		if (except instanceof FileNotFoundException) {
			final String msg = "Image " + filename + " not found";
			if (fallback) {
				LOGGER.log(Level.SEVERE, msg, except);
			} else {
				LOGGER.log(Level.INFO, msg, except);
			}
		} else {
			LOGGER.log(Level.SEVERE,
					"I/O eror while loading image " + filename, except);
		}
	}
	/**
	 * Draw a tile at the specified coordinates. Because this is at present only
	 * called in a loop that's the last thing before the context is disposed, we
	 * alter the state freely and don't restore it.
	 *
	 * @param pen the graphics context.
	 * @param tile the tile to draw
	 * @param coordinates the coordinates of the tile's upper-left corner
	 * @param dimensions the width (X) and height (Y) of the tile
	 */
	@Override
	public void drawTile(final Graphics pen, final ITile tile,
			final Coordinate coordinates, final Coordinate dimensions) {
		if (needsFixtureColor(tile)) {
			pen.setColor(getFixtureColor(tile));
		} else {
			pen.setColor(getTileColor(2, tile.getTerrain()));
		}
		pen.fillRect(coordinates.x, coordinates.y, dimensions.x, dimensions.y);
		if (hasRivers(tile)) {
			pen.drawImage(getImageForFixture(getRivers(tile)), coordinates.x,
					coordinates.y, dimensions.x, dimensions.y, observer);
		}
		if (hasFixture(tile)) {
			pen.drawImage(getImageForFixture(getTopFixture(tile)),
					coordinates.x, coordinates.y, dimensions.x, dimensions.y,
					observer);
		}
		pen.setColor(Color.black);
		pen.drawRect(coordinates.x, coordinates.y, dimensions.x, dimensions.y);
	}

	/**
	 * Draw a tile at the upper-left corner of the drawing surface.
	 *
	 * @param pen the graphics context
	 * @param tile the tile to draw
	 * @param width the width of the drawing area
	 * @param height the height of the drawing area
	 */
	@Override
	public void drawTileTranslated(final Graphics pen, final ITile tile,
			final int width, final int height) {
		drawTile(pen, tile, PointFactory.coordinate(0, 0),
				PointFactory.coordinate(width, height));
	}
	/**
	 * @param tile a tile
	 * @return whether that tile has any rivers that are not on top
	 */
	private static boolean hasRivers(final ITile tile) {
		for (final TileFixture fix : tile) {
			if (fix instanceof RiverFixture) {
				return true;
			}
		}
		return false;
	}
	/**
	 * @param tile a tile
	 * @return whether that tile has any fixtures (or any river
	 */
	private boolean hasFixture(final ITile tile) {
		return new FilteredIterator(tile.iterator(), zof).hasNext();
	}

	/**
	 * @param tile
	 *            a tile
	 * @return any RiverFixture on that tile. We assume the tile cannot have
	 *         more than one.
	 */
	private static RiverFixture getRivers(final ITile tile) {
		for (final TileFixture item : tile) {
			if (item instanceof RiverFixture) {
				return (RiverFixture) item;
			}
		}
		throw new IllegalArgumentException("Tile has no non-null fixtures");
	}
	/**
	 * @param tile a tile
	 * @return the top fixture on that tile.
	 */
	private TileFixture getTopFixture(final ITile tile) {
		final Iterable<TileFixture> iter = new IteratorWrapper<>(
				new FilteredIterator(tile.iterator(), zof), fixComp);
		for (final TileFixture item : iter) {
			if (item != null) {
				return item;
			}
		}
		throw new IllegalArgumentException("Tile has no non-null fixtures");
	}

	/**
	 * FIXME: This at present ignores the case of a forest *and* a mountain on a
	 * tile; we can't show both as icons.
	 *
	 * @param tile a tile
	 * @return whether it needs a different color to show a non-top fixture
	 *         (like a forest or mountain)
	 */
	private boolean needsFixtureColor(final ITile tile) {
		if (hasTerrainFixture(tile)) {
			return !(getTopFixture(tile) instanceof TerrainFixture); // NOPMD
		} else {
			return false;
		}
	}

	/**
	 * @param tile a tile
	 * @return whether it has a TerrainFixture.
	 */
	private boolean hasTerrainFixture(final ITile tile) {
		final Iterable<TileFixture> iter =
				new IteratorWrapper<>(
						new FilteredIterator(tile.iterator(), zof));
		for (final TileFixture fix : iter) {
			if (fix instanceof TerrainFixture) {
				return true; // NOPMD
			}
		}
		return false;
	}

	/**
	 * @param tile a tile
	 * @return a color to represent its not-on-top terrain feature.
	 */
	private Color getFixtureColor(final ITile tile) {
		final Iterable<TileFixture> iter =
				new IteratorWrapper<>(
						new FilteredIterator(tile.iterator(), zof));
		for (final TileFixture fix : iter) {
			if (fix instanceof TerrainFixture) {
				return getHelper().getFeatureColor(fix); // NOPMD
			}
		}
		return getTileColor(2, tile.getTerrain());
	}

	/**
	 * @param fix a TileFixture
	 * @return an Image to draw to represent it.
	 */
	private Image getImageForFixture(final TileFixture fix) {
		if (fix instanceof HasImage) {
			String image = ((HasImage) fix).getImage();
			if (image.isEmpty()) {
				image = ((HasImage) fix).getDefaultImage();
				return getImage(image); // NOPMD
			} else if (missingFiles.contains(image)) {
				return getImage(((HasImage) fix).getDefaultImage()); // NOPMD
			} else {
				return getImage(image); // NOPMD
			}
		} else if (fix instanceof RiverFixture) {
			return getImage(NullCleaner.assertNotNull(riverFiles//NOPMD
					.get(((RiverFixture) fix).getRivers())));
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
	private Image getImage(final String filename) {
		try {
			return loader.loadImage(filename); // NOPMD
		} catch (final FileNotFoundException e) {
			if (!missingFiles.contains(filename)) {
				LOGGER.log(Level.SEVERE, "images/" + filename + " not found");
				LOGGER.log(Level.FINEST, "with stack trace", e);
				missingFiles.add(filename);
			}
			return fallbackImage; // NOPMD
		} catch (final IOException e) {
			LOGGER.log(Level.SEVERE, "I/O error reading image images/"
					+ filename, e);
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
		riverFiles
				.put(createRiverSet(River.North, River.East, River.South,
						River.West), "riv26.png");
		riverFiles
				.put(createRiverSet(River.North, River.South, River.West,
						River.Lake), "riv27.png");
		riverFiles
				.put(createRiverSet(River.North, River.East, River.West,
						River.Lake), "riv28.png");
		riverFiles
				.put(createRiverSet(River.North, River.East, River.South,
						River.Lake), "riv29.png");
		riverFiles
				.put(createRiverSet(River.East, River.South, River.West,
						River.Lake), "riv30.png");
		riverFiles.put(EnumSet.allOf(River.class), "riv31.png");
	}

	/**
	 * @param rivers any number of rivers
	 * @return a set containing them
	 */
	private static Set<River> createRiverSet(final River... rivers) {
		final Set<River> set =
				NullCleaner.assertNotNull(EnumSet.noneOf(River.class));
		for (final River river : rivers) {
			set.add(river);
		}
		return set;
	}

	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "Ver2TileDrawHelper";
	}

	/**
	 * A filtered iterator. Only returns items that should be displayed.
	 * @author Jonathan Lovelace
	 */
	private static class FilteredIterator implements Iterator<TileFixture> {
		/**
		 * The wrapped iterator.
		 */
		private final Iterator<TileFixture> wrapped;
		/**
		 * The filter to use.
		 */
		private final ZOrderFilter zof;
		/**
		 * The next item.
		 */
		private TileFixture cached;
		/**
		 * Whether we have a cached next item.
		 */
		private boolean hasCached;

		/**
		 * @return a hash value for the object
		 */
		@Override
		public int hashCode() {
			return wrapped.hashCode() | zof.hashCode();
		}

		/**
		 * @param obj an object
		 * @return whether it's the same as this
		 */
		@Override
		public boolean equals(@Nullable final Object obj) {
			return this == obj || obj instanceof FilteredIterator
					&& wrapped.equals(((FilteredIterator) obj).wrapped)
					&& zof.equals(((FilteredIterator) obj).zof);
		}

		/**
		 * A TileFixture implementation to use instead of null.
		 */
		private static final TileFixture NULL_FIXT = new TileFixture() {
			@Override
			public int hashCode() {
				return -1;
			}
			@Override
			public boolean equals(@Nullable final Object obj) {
				return this == obj;
			}
			@Override
			public int compareTo(@Nullable final TileFixture obj) {
				throw new IllegalStateException(
						"Leak of an all-but-null object");
			}

			@Override
			public int getID() {
				throw new IllegalStateException(
						"Leak of an all-but-null object");
			}

			@Override
			public boolean equalsIgnoringID(final IFixture fix) {
				return fix == this;
			}

			@Override
			public String plural() {
				return "";
			}

			@Override
			public int getZValue() {
				throw new IllegalStateException(
						"Leak of an all-but-null object");
			}
			/**
			 * @return a short description of the fixture
			 */
			@Override
			public String shortDesc() {
				return "null";
			}

		};

		/**
		 * Constructor.
		 *
		 * @param iter the iterator to wrap
		 * @param zofilt the filter to use
		 */
		protected FilteredIterator(final Iterator<TileFixture> iter,
				final ZOrderFilter zofilt) {
			wrapped = iter;
			zof = zofilt;
			hasCached = false;
			cached = NULL_FIXT;
			hasNext();
		}

		/**
		 * @return whether there is a next item in the iterator
		 */
		@Override
		public final boolean hasNext() {
			if (hasCached) {
				return true; // NOPMD
			} else {
				while (wrapped.hasNext()) {
					final TileFixture tempCached = wrapped.next();
					if (tempCached != null && tempCached != NULL_FIXT
							&& zof.shouldDisplay(tempCached)) {
						cached = tempCached;
						hasCached = true;
						return true; // NOPMD
					}
				}
				return false;
			}
		}

		/**
		 * @return the next element
		 */
		@Override
		public TileFixture next() {
			if (hasNext()) {
				hasCached = false;
				return cached;
			} else {
				throw new NoSuchElementException("No next element");
			}
		}

		/**
		 * Implemented only if wrapped iterator does.
		 */
		@Override
		public void remove() {
			wrapped.remove();
		}
		/**
		 * @return a String representation of the object
		 */
		@Override
		public String toString() {
			return "FilteredIterator";
		}
	}
}

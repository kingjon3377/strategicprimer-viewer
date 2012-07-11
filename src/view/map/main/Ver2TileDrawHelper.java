package view.map.main;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import model.map.HasImage;
import model.map.River;
import model.map.TerrainFixture;
import model.map.Tile;
import model.map.TileFixture;
import model.map.events.IEvent;
import model.map.fixtures.RiverFixture;
import model.viewer.FixtureComparator;
import util.ImageLoader;
import view.util.Coordinate;
/**
 * A TileDrawHelper for the new map version.
 * @author Jonathan Lovelace
 *
 */
public class Ver2TileDrawHelper extends AbstractTileDrawHelper {
	/**
	 * The observer to be notified when images finish drawing.
	 */
	private final ImageObserver observer;
	/**
	 * Logger.
	 */
	private static final Logger LOGGER = Logger.getLogger(Ver2TileDrawHelper.class.getName());
	/**
	 * A fallback image.
	 */
	private Image fallbackImage;
	/**
	 * Constructor. We need to initialize the cache.
	 * @param iobs the class to notify when images finish drawing.
	 */
	public Ver2TileDrawHelper(final ImageObserver iobs) {
		super();
		observer = iobs;
		final String[] files = new String[] { "tree.png", "mountain.png" };
		createRiverFiles();
		for (String file : files) {
			try {
				loader.loadImage(file);
			} catch (FileNotFoundException e) {
				LOGGER.log(Level.SEVERE, "Image " + file + " not found", e);
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE, "I/O error while loading image " + file, e);
			}
		}
		try {
			fallbackImage = loader.loadImage("event_fallback.png");
		} catch (FileNotFoundException e) {
			LOGGER.log(Level.SEVERE, "Image event_fallback.png not found", e);
			fallbackImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "I/O error while loading image event_fallback.png", e);
			fallbackImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		}
	}
	
	/**
	 * Draw a tile at the specified coordinates. Because this is at present only
	 * called in a loop that's the last thing before the context is disposed, we
	 * alter the state freely and don't restore it.
	 * 
	 * @param pen
	 *            the graphics context.
	 * @param tile
	 *            the tile to draw
	 * @param coordinates
	 *            the coordinates of the tile's upper-left corner
	 * @param dimensions
	 *            the width (X) and height (Y) of the tile
	 */
	@Override
	public void drawTile(final Graphics pen, final Tile tile,
			final Coordinate coordinates, final Coordinate dimensions) {
		pen.setColor((needFixtureColor(tile) ? getFixtureColor(tile)
				: getTileColor(2, tile.getTerrain())));
		pen.fillRect(coordinates.x, coordinates.y, dimensions.x, dimensions.y);
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
	 * @param pen
	 *            the graphics context
	 * @param tile
	 *            the tile to draw
	 * @param width
	 *            the width of the drawing area
	 * @param height
	 *            the height of the drawing area
	 */
	@Override
	public void drawTile(final Graphics pen, final Tile tile, final int width, final int height) {
		drawTile(pen, tile, new Coordinate(0, 0), new Coordinate(width, height));
	}
	/**
	 * @param tile a tile
	 * @return whether that tile has any fixtures (or any river
	 */
	private static boolean hasFixture(final Tile tile) {
		return (!tile.getContents().isEmpty());
	}
	/**
	 * Comparator to find which fixture to draw.
	 */
	private final FixtureComparator fixComp = new FixtureComparator();
	/**
	 * @param tile a tile
	 * @return the top fixture on that tile.
	 */
	private TileFixture getTopFixture(final Tile tile) {
		return Collections.max(tile.getContents(), fixComp);
	}
	
	/**
	 * FIXME: This at present ignores the case of a forest *and* a mountain on a
	 * tile; we can't show both as icons.
	 * 
	 * @param tile
	 *            a tile
	 * @return whether it needs a different color to show a non-top fixture
	 *         (like a forest or mountain)
	 */
	private boolean needFixtureColor(final Tile tile) {
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
	private static boolean hasTerrainFixture(final Tile tile) {
		for (TileFixture fix : tile.getContents()) {
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
	private static Color getFixtureColor(final Tile tile) {
		for (TileFixture fix : tile.getContents()) {
			if (fix instanceof TerrainFixture) {
				return getHelper().getFeatureColor(fix); // NOPMD
			}
		}
		return getTileColor(2, tile.getTerrain());
	}
	/**
	 * Image cache.
	 */
	private final ImageLoader loader = ImageLoader.getLoader();
	/**
	 * @param fix a TileFixture
	 * @return an Image to draw to represent it.
	 */
	private Image getImageForFixture(final TileFixture fix) {
		if (fix instanceof HasImage) {
			return getImage(((HasImage) fix).getImage()); // NOPMD
		} else if (fix instanceof IEvent) {
			return getImage("event.png"); // NOPMD
		} else if (fix instanceof RiverFixture) { 
			return getImage(riverFiles.get(((RiverFixture) fix).getRivers())); // NOPMD
		} else {
			LOGGER.warning("Using the fallback image because this is an unanticipated kind of Fixture.");
			return fallbackImage;
		}
	}
	/**
	 * Return either a loaded image or a generic one.
	 * @param filename the name of the file containing the image
	 * @return that image, or, if it fails to load, the generic one.
	 */
	private Image getImage(final String filename) {
		try {
			return loader.loadImage(filename); // NOPMD
		} catch (FileNotFoundException e) {
			if (!missingFiles.contains(filename)) {
//				LOGGER.log(Level.SEVERE, filename + " not found", e);
				LOGGER.log(Level.SEVERE, filename + " not found");
				missingFiles.add(filename);
			}
			return fallbackImage; // NOPMD
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "I/O error reading image " + filename, e);
			return fallbackImage;
		}
	}
	/**
	 * The images we've already determined aren't there.
	 */
	private final Set<String> missingFiles = new HashSet<String>();
	/**
	 * A mapping from river-sets to filenames.
	 */
	private final Map<Set<River>, String> riverFiles = new HashMap<Set<River>, String>();
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
		riverFiles.put(createRiverSet(River.North, River.East, River.South), "riv16.png");
		riverFiles.put(createRiverSet(River.North, River.East, River.West), "riv17.png");
		riverFiles.put(createRiverSet(River.North, River.East, River.Lake), "riv18.png");
		riverFiles.put(createRiverSet(River.North, River.South, River.West), "riv19.png");
		riverFiles.put(createRiverSet(River.North, River.South, River.Lake), "riv20.png");
		riverFiles.put(createRiverSet(River.North, River.West, River.Lake), "riv21.png");
		riverFiles.put(createRiverSet(River.East, River.South, River.West), "riv22.png");
		riverFiles.put(createRiverSet(River.East, River.South, River.Lake), "riv23.png");
		riverFiles.put(createRiverSet(River.East, River.West, River.Lake), "riv24.png");
		riverFiles.put(createRiverSet(River.South, River.West, River.Lake), "riv25.png");
		riverFiles.put(createRiverSet(River.North, River.East, River.South, River.West), "riv26.png");
		riverFiles.put(createRiverSet(River.North, River.South, River.West, River.Lake), "riv27.png");
		riverFiles.put(createRiverSet(River.North, River.East, River.West, River.Lake), "riv28.png");
		riverFiles.put(createRiverSet(River.North, River.East, River.South, River.Lake), "riv29.png");
		riverFiles.put(createRiverSet(River.East, River.South, River.West, River.Lake), "riv30.png");
		riverFiles.put(EnumSet.allOf(River.class), "riv31.png");
	}
	/**
	 * @param rivers any number of rivers
	 * @return a set containing them
	 */
	private static Set<River> createRiverSet(final River... rivers) {
		final Set<River> set = EnumSet.noneOf(River.class);
		for (River river : rivers) {
			set.add(river);
		}
		return set;
	}
}

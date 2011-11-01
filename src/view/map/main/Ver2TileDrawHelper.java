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
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import model.map.River;
import model.map.TerrainFixture;
import model.map.Tile;
import model.map.TileFixture;
import model.map.events.AbstractTownEvent;
import model.map.events.Forest;
import model.map.events.IEvent;
import model.map.events.MineralEvent;
import model.map.events.StoneEvent;
import model.map.fixtures.Fortress;
import model.map.fixtures.Ground;
import model.map.fixtures.Mountain;
import model.map.fixtures.RiverFixture;
import model.map.fixtures.Shrub;
import model.map.fixtures.Unit;
import model.viewer.FixtureComparator;
import util.ImageLoader;
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
	 * Draw a tile at the specified coordinates.
	 * 
	 * @param pen
	 *            the graphics context.
	 * @param version the map version
	 * @param tile
	 *            the tile to draw
	 * @param xCoord
	 *            the tile's left boundary
	 * @param yCoord
	 *            the tile's right boundary
	 * @param width
	 *            the tile's width
	 * @param height
	 *            the tile's height
	 */
	// ESCA-JAVA0138:
	@Override
	public void drawTile(final Graphics pen, final int version, final Tile tile, final int xCoord,
			final int yCoord, final int width, final int height) {
		final Color save = pen.getColor();
		pen.setColor((needFixtureColor(tile) ? getFixtureColor(tile) : getTileColor(version, tile.getType())));
		pen.fillRect(xCoord, yCoord, width, height);
		if (hasFixture(tile)) {
			pen.drawImage(getImageForFixture(getTopFixture(tile)), xCoord, yCoord, width, height, observer);
		}
		pen.setColor(Color.black);
		pen.drawRect(xCoord, yCoord, width, height);
		pen.setColor(save);
	}

	/**
	 * Draw a tile at the upper-left corner of the drawing surface.
	 * 
	 * @param pen
	 *            the graphics context
	 * @param version the map version
	 * @param tile
	 *            the tile to draw
	 * @param width
	 *            the width of the drawing area
	 * @param height
	 *            the height of the drawing area
	 */
	@Override
	public void drawTile(final Graphics pen, final int version, final Tile tile, final int width,
			final int height) {
		drawTile(pen, version, tile, 0, 0, width, height);
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
		boolean hasTerrainFixture = false;
		for (TileFixture fix : tile.getContents()) {
			if (fix instanceof TerrainFixture) {
				hasTerrainFixture = true;
			}
		}
		if (hasTerrainFixture) {
			final TileFixture fix = getTopFixture(tile);
			return !(fix instanceof TerrainFixture); // NOPMD
		} else {
			return false;
		}
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
		return getTileColor(2, tile.getType());
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
		if (fix instanceof Mountain) {
			return getImage("mountain.png"); // NOPMD
		} else if (fix instanceof Forest) {
			return getImage("tree.png"); // NOPMD // TODO: Should have different icons depending on the kind of tree.
		} else if (fix instanceof AbstractTownEvent) {
			return getImage("town.png"); // FIXME: Should be more granular // NOPMD:
		} else if (fix instanceof StoneEvent) {
			return getImage("stone.png"); // NOPMD
		} else if (fix instanceof MineralEvent) {
			return getImage("mineral.png"); // NOPMD
		} else if (fix instanceof IEvent) {
			return getImage("event.png"); // NOPMD
		} else if (fix instanceof Fortress) {
			return getImage("fortress.png"); // NOPMD
		} else if (fix instanceof Unit) {
			return getImage("unit.png"); // TODO: Should eventually be more granular // NOPMD 
		} else if (fix instanceof RiverFixture) { 
			return getImage(riverFiles.get(((RiverFixture) fix).getRivers())); // NOPMD
		} else if (fix instanceof Ground && ((Ground) fix).isExposed()) {
			return getImage("expground.png"); // NOPMD)
		} else if (fix instanceof Ground) {
			return getImage("blank.png"); // NOPMD
		} else if (fix instanceof Shrub) {
			return getImage("shrub.png"); // NOPMD
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
			LOGGER.log(Level.SEVERE, filename + " not found", e);
			return fallbackImage; // NOPMD
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "I/O error reading image " + filename, e);
			return fallbackImage;
		}
	}
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

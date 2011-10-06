package view.map.main;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.stream.XMLStreamException;

import model.viewer.Fortress;
import model.viewer.SPMap;
import model.viewer.Tile;
import model.viewer.TileFixture;
import model.viewer.TileType;
import model.viewer.Unit;
import model.viewer.events.AbstractEvent;
import controller.map.simplexml.SPFormatException;
import controller.map.simplexml.SimpleXMLReader;
/**
 * An abstract superclass containing helper methods for TileDrawHelpers.
 * @author Jonathan Lovelace
 *
 */
public abstract class AbstractTileDrawHelper implements TileDrawHelper {

	/**
	 * The color of the icon used to show that a tile has an event or associated text.
	 */
	protected static final Color EVENT_COLOR = Color.pink;
	/**
	 * Eight as a double. Used to make rivers take up 1/8 of the tile in their short dimension.
	 */
	protected static final double EIGHT = 8.0;
	/**
	 * 7/16: where the short side of a river starts, along the edge of the tile.
	 */
	protected static final double SEVEN_SIXTEENTHS = 7.0 / 16.0;

	/**
	 * @param type a tile type
	 * @return the color associated with that tile-type.
	 */
	protected static Color getTileColor(final TileType type) {
		return COLORS.get(type);
	}

	/**
	 * @param tile a tile
	 * @return whether the tile has any forts.
	 */
	protected static boolean hasAnyForts(final Tile tile) {
		for (TileFixture fix : tile.getContents()) {
			if (fix instanceof Fortress) {
				return true; // NOPMD
			}
		}
		return false;
	}

	/**
	 * @param tile a tile
	 * @return whether the tile has any units.
	 */
	protected static boolean hasAnyUnits(final Tile tile) {
		for (TileFixture fix : tile.getContents()) {
			if (fix instanceof Unit) {
				return true; // NOPMD
			}
		}
		return false;
	}

	/**
	 * @param tile a tile
	 * @return whether the tile has any events
	 */
	protected static boolean hasEvent(final Tile tile) {
		if ("".equals(tile.getTileText())) {
			for (TileFixture fix : tile.getContents()) {
				if (fix instanceof AbstractEvent) {
					return true; // NOPMD
				}
			}
			return false; // NOPMD
		} else {
			return true;
		}
	}

	/**
	 * Brown, the color of a fortress.
	 */
	protected static final Color FORT_COLOR = new Color(160, 82, 45);
	/**
	 * Purple, the color of a unit.
	 */
	protected static final Color UNIT_COLOR = new Color(148, 0, 211);
	/**
	 * Mapping from tile types to colors.
	 */
	protected static final Map<TileType, Color> COLORS = new EnumMap<TileType, Color>(
				TileType.class);
	// ESCA-JAVA0076:
	static {
		COLORS.put(TileType.BorealForest, new Color(72, 218, 164));
		COLORS.put(TileType.Desert, new Color(249, 233, 28));
		COLORS.put(TileType.Jungle, new Color(229, 46, 46));
		COLORS.put(TileType.Mountain, new Color(249, 137, 28));
		COLORS.put(TileType.NotVisible, new Color(255, 255, 255));
		COLORS.put(TileType.Ocean, new Color(0, 0, 255));
		COLORS.put(TileType.Plains, new Color(0, 117, 0));
		COLORS.put(TileType.TemperateForest, new Color(72, 250, 72));
		COLORS.put(TileType.Tundra, new Color(153, 153, 153));
	}
	/**
	 * Draw a tile. At present, the graphics context needs to be translated so that its origin is the tile's upper-left-hand corner.
	 * @param pen the graphics context
	 * @param tile the tile to draw
	 * @param width the width of the drawing area
	 * @param height the height of the drawing area
	 */
	@Override
	public abstract void drawTile(final Graphics pen, final Tile tile, final int width, final int height);
	/**
	 * A driver method to compare the two helpers, and the two map-GUI implementations.
	 * @param args the command-line arguments.
	 */
	public static void main(final String[] args) { // NOPMD
		final Logger logger = Logger.getLogger(AbstractTileDrawHelper.class.getName());
		// ESCA-JAVA0177:
		final SPMap map; // NOPMD
		try {
			map = new SimpleXMLReader().readMap(args[0]);
		} catch (IOException e) {
			logger.log(Level.SEVERE, "I/O error reading map", e);
			return; // NOPMD
		} catch (XMLStreamException e) {
			logger.log(Level.SEVERE, "XML error reading map", e);
			return; // NOPMD
		} catch (SPFormatException e) {
			logger.log(Level.SEVERE, "Map format error reading map", e);
			return;
		}
		final TileDrawHelper helperOne = new CachingTileDrawHelper();
		final TileDrawHelper helperTwo = new DirectTileDrawHelper();
		BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_RGB);
		final int reps = 50; // NOPMD
		long start, end;
		Graphics pen;
		System.out.println("About to start");
		start = System.nanoTime();
		for (int rep = 0; rep < reps; rep++) {
			image.flush();
			for (int i = 0; i < map.rows(); i++) {
				for (int j = 0; j < map.cols(); j++) {
					helperOne.drawTile(image.createGraphics(), map.getTile(i, j), 16, 16);
				}
			}
		}
		end = System.nanoTime();
		System.out.print("Caching did first test (all in one place) in ");
		printStats(end - start, reps);
		start = System.nanoTime();
		for (int rep = 0; rep < reps; rep++) {
			image.flush();
			for (int i = 0; i < map.rows(); i++) {
				for (int j = 0; j < map.cols(); j++) {
					helperTwo.drawTile(image.createGraphics(), map.getTile(i, j), 16, 16);
				}
			}
		}
		end = System.nanoTime();
		System.out.print("Direct did first test (all in one place) in ");
		printStats(end - start, reps);
		image = new BufferedImage(16 * map.cols(), 16 * map.rows(), BufferedImage.TYPE_INT_RGB);
		start = System.nanoTime();
		for (int rep = 0; rep < reps; rep++) {
			image.flush();
			for (int i = 0; i < map.rows(); i++) {
				for (int j = 0; j < map.cols(); j++) {
					helperOne.drawTile(image.createGraphics(), map.getTile(i, j), i * 16, j * 16, 16, 16);
				}
			}
		}
		end = System.nanoTime();
		System.out.print("Caching did second test (translating) in ");
		printStats(end - start, reps);
		start = System.nanoTime();
		for (int rep = 0; rep < reps; rep++) {
			image.flush();
			for (int i = 0; i < map.rows(); i++) {
				for (int j = 0; j < map.cols(); j++) {
					helperTwo.drawTile(image.createGraphics(), map.getTile(i, j), i * 16, j * 16, 16, 16);
				}
			}
		}
		end = System.nanoTime();
		System.out.print("Direct did second test (translating) in ");
		printStats(end - start, reps);
		start = System.nanoTime();
		for (int rep = 0; rep < reps; rep++) {
			image.flush();
			pen = image.createGraphics();
			for (int i = 0; i < map.rows(); i++) {
				for (int j = 0; j < map.cols(); j++) {
					helperOne.drawTile(pen, map.getTile(i, j), 16, 16);
				}
			}
		}
		end = System.nanoTime();
		System.out.print("Caching did third test (in-place, reusing Graphics) in ");
		printStats(end - start, reps);
		start = System.nanoTime();
		for (int rep = 0; rep < reps; rep++) {
			image.flush();
			pen = image.createGraphics();
			for (int i = 0; i < map.rows(); i++) {
				for (int j = 0; j < map.cols(); j++) {
					helperTwo.drawTile(pen, map.getTile(i, j), 16, 16);
				}
			}
		}
		end = System.nanoTime();
		System.out.print("Direct did third test (in-place, reusing Graphics) in ");
		printStats(end - start, reps);
		image = new BufferedImage(16 * map.cols(), 16 * map.rows(), BufferedImage.TYPE_INT_RGB);
		start = System.nanoTime();
		for (int rep = 0; rep < reps; rep++) {
			image.flush();
			pen = image.createGraphics();
			for (int i = 0; i < map.rows(); i++) {
				for (int j = 0; j < map.cols(); j++) {
					helperOne.drawTile(pen, map.getTile(i, j), i * 16, j * 16, 16, 16);
				}
			}
		}
		end = System.nanoTime();
		System.out.print("Caching did fourth test (translating, reusing G) in ");
		printStats(end - start, reps);
		start = System.nanoTime();
		for (int rep = 0; rep < reps; rep++) {
			image.flush();
			pen = image.createGraphics();
			for (int i = 0; i < map.rows(); i++) {
				for (int j = 0; j < map.cols(); j++) {
					helperTwo.drawTile(pen, map.getTile(i, j), i * 16, j * 16, 16, 16);
				}
			}
		}
		end = System.nanoTime();
		System.out.print("Direct did fourth test (translating, reusing G) in ");
		printStats(end - start, reps);
	}
	/**
	 * A helper method to reduce repeated strings.
	 * @param total the total time
	 * @param reps how many reps there were
	 */
	private static void printStats(final long total, final int reps) {
		System.out.print('\t');
		System.out.print(total);
		System.out.print(", average of\t");
		System.out.print(Long.toString(total / reps));
		System.out.println(" ns.");
	}
}

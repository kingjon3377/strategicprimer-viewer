package controller.map.drivers;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.stream.XMLStreamException;

import model.map.SPMap;
import view.map.main.CachingTileDrawHelper;
import view.map.main.DirectTileDrawHelper;
import view.map.main.MapComponent;
import view.map.main.TileDrawHelper;
import view.util.SystemOut;
import controller.map.SPFormatException;
import controller.map.misc.MapReaderAdapter;

/**
 * A driver to compare the performance of TileDrawHelpers.
 * 
 * @author Jonathan Lovelace
 * 
 */
public class DrawHelperComparator {
	/**
	 * Label to put before every direct-helper test result.
	 */
	private static final String DIRECT_NAME = "Direct:";
	/**
	 * Label to put before every caching-helper test result.
	 */
	private static final String CACHING_NAME = "Caching:";

	/**
	 * Constructor.
	 * 
	 * @param map
	 *            the map we'll be drawing in the tests
	 * @param repetitions
	 *            how many times to repeat each test
	 */
	public DrawHelperComparator(final SPMap map, final int repetitions) {
		spmap = map;
		rows = spmap.rows();
		cols = spmap.cols();
		reps = repetitions;
	}

	/**
	 * The map.
	 */
	private final SPMap spmap;
	/**
	 * The size of the map in rows.
	 */
	private final int rows;
	/**
	 * The size of the map in columns.
	 */
	private final int cols;
	/**
	 * How many times to repeat each test.
	 */
	private final int reps;
	/**
	 * The size of a tile, factored out to reduce number of lines a line of code
	 * has to use.
	 */
	private final int tsize = MapComponent.getTileSize();

	/**
	 * The first test: all in one place.
	 * 
	 * @param helper
	 *            the helper to test
	 * @return how long the test took, in ns.
	 */
	public long first(final TileDrawHelper helper) {
		final BufferedImage image = new BufferedImage(tsize, tsize,
				BufferedImage.TYPE_INT_RGB);
		final long start = System.nanoTime();
		for (int rep = 0; rep < reps; rep++) {
			image.flush();
			for (int i = 0; i < rows; i++) {
				for (int j = 0; j < cols; j++) {
					helper.drawTile(image.createGraphics(),
							spmap.getTile(i, j), tsize, tsize);
				}
			}
		}
		final long end = System.nanoTime();
		return end - start;
	}

	/**
	 * The second test: Translating.
	 * 
	 * @param helper
	 *            the helper to test
	 * @return how long the test took, in ns.
	 */
	public long second(final TileDrawHelper helper) {
		final BufferedImage image = new BufferedImage(tsize * spmap.cols(),
				tsize * spmap.rows(), BufferedImage.TYPE_INT_RGB);
		final long start = System.nanoTime();
		for (int rep = 0; rep < reps; rep++) {
			image.flush();
			for (int i = 0; i < rows; i++) {
				for (int j = 0; j < cols; j++) {
					helper.drawTile(image.createGraphics(),
							spmap.getTile(i, j), i * tsize, j * tsize, tsize,
							tsize);
				}
			}
		}
		final long end = System.nanoTime();
		return end - start;
	}

	/**
	 * Third test: in-place, reusing Graphics.
	 * 
	 * @param helper
	 *            the helper to test
	 * @return how long the test took, in ns.
	 */
	public long third(final TileDrawHelper helper) {
		final BufferedImage image = new BufferedImage(tsize, tsize,
				BufferedImage.TYPE_INT_RGB);
		final long start = System.nanoTime();
		for (int rep = 0; rep < reps; rep++) {
			image.flush();
			final Graphics pen = image.createGraphics();
			for (int i = 0; i < rows; i++) {
				for (int j = 0; j < cols; j++) {
					helper.drawTile(pen, spmap.getTile(i, j), tsize, tsize);
				}
			}
		}
		final long end = System.nanoTime();
		return end - start;
	}

	/**
	 * Third test: translating, reusing Graphics.
	 * 
	 * @param helper
	 *            the helper to test
	 * @return how long the test took, in ns.
	 */
	public long fourth(final TileDrawHelper helper) {
		final BufferedImage image = new BufferedImage(tsize * spmap.cols(),
				tsize * spmap.rows(), BufferedImage.TYPE_INT_RGB);
		final long start = System.nanoTime();
		for (int rep = 0; rep < reps; rep++) {
			image.flush();
			final Graphics pen = image.createGraphics();
			for (int i = 0; i < rows; i++) {
				for (int j = 0; j < cols; j++) {
					helper.drawTile(pen, spmap.getTile(i, j), i * tsize, j
							* tsize, tsize, tsize);
				}
			}
		}
		final long end = System.nanoTime();
		return end - start;
	}

	/**
	 * A driver method to compare the two helpers, and the two map-GUI
	 * implementations.
	 * 
	 * @param args
	 *            the command-line arguments.
	 */
	public static void main(final String[] args) { // NOPMD
		final Logger logger = Logger.getLogger(DrawHelperComparator.class
				.getName());
		// ESCA-JAVA0177:
		final DrawHelperComparator comp; // NOPMD
		try {
			comp = new DrawHelperComparator(
					new MapReaderAdapter().readMap(args[0]), 50);
		} catch (final IOException e) {
			logger.log(Level.SEVERE, "I/O error reading map", e);
			return; // NOPMD
		} catch (final XMLStreamException e) {
			logger.log(Level.SEVERE, "XML error reading map", e);
			return; // NOPMD
		} catch (final SPFormatException e) {
			logger.log(Level.SEVERE, "Map format error reading map", e);
			return;
		}
		final TileDrawHelper helperOne = new CachingTileDrawHelper();
		final TileDrawHelper helperTwo = new DirectTileDrawHelper();
		SystemOut.SYS_OUT.println("1. All in one place:");
		SystemOut.SYS_OUT.print(CACHING_NAME);
		comp.printStats(comp.first(helperOne));
		SystemOut.SYS_OUT.print(DIRECT_NAME);
		comp.printStats(comp.first(helperTwo));
		SystemOut.SYS_OUT.println("2. Translating:");
		SystemOut.SYS_OUT.print(CACHING_NAME);
		comp.printStats(comp.second(helperOne));
		SystemOut.SYS_OUT.print(DIRECT_NAME);
		comp.printStats(comp.second(helperTwo));
		SystemOut.SYS_OUT.println("3. In-place, reusing Graphics:");
		SystemOut.SYS_OUT.print(CACHING_NAME);
		comp.printStats(comp.third(helperOne));
		SystemOut.SYS_OUT.print(DIRECT_NAME);
		comp.printStats(comp.third(helperTwo));
		SystemOut.SYS_OUT.println("4. Translating, reusing Graphics:");
		SystemOut.SYS_OUT.print(CACHING_NAME);
		comp.printStats(comp.fourth(helperOne));
		SystemOut.SYS_OUT.print(DIRECT_NAME);
		comp.printStats(comp.fourth(helperTwo));
	}

	/**
	 * A helper method to reduce repeated strings.
	 * 
	 * @param total
	 *            the total time
	 */
	public void printStats(final long total) {
		SystemOut.SYS_OUT.print('\t');
		SystemOut.SYS_OUT.print(total);
		SystemOut.SYS_OUT.print(", average of\t");
		SystemOut.SYS_OUT.print(Long.toString(total / reps));
		SystemOut.SYS_OUT.println(" ns.");
	}

	/**
	 * @return a String representation of this object
	 */
	@Override
	public String toString() {
		return "DrawHelperComparator";
	}
}

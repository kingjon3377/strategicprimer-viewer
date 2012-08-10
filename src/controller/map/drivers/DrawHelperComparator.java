package controller.map.drivers;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.stream.XMLStreamException;

import model.map.IMap;
import model.map.Point;
import model.map.PointFactory;
import model.viewer.TileViewSize;
import util.Warning;
import view.map.main.CachingTileDrawHelper;
import view.map.main.DirectTileDrawHelper;
import view.map.main.TileDrawHelper;
import view.map.main.Ver2TileDrawHelper;
import view.util.Coordinate;
import view.util.SystemOut;
import controller.map.SPFormatException;
import controller.map.misc.MapReaderAdapter;

/**
 * A driver to compare the performance of TileDrawHelpers.
 *
 * @author Jonathan Lovelace
 *
 */
public class DrawHelperComparator { // NOPMD
	/**
	 * Label to put before every direct-helper test result.
	 */
	private static final String DIRECT_NAME = "Direct:  ";
	/**
	 * Label to put before every caching-helper test result.
	 */
	private static final String CACHING_NAME = "Caching:";
	/**
	 * Label to put before every version-2 helper test result.
	 */
	private static final String VER_TWO_NAME = "Ver. 2: ";

	/**
	 * Constructor.
	 *
	 * @param map the map we'll be drawing in the tests
	 * @param repetitions how many times to repeat each test
	 */
	public DrawHelperComparator(final IMap map, final int repetitions) {
		spmap = map;
		reps = repetitions;
		tsize = new TileViewSize().getSize(map.getVersion());
	}

	/**
	 * The map.
	 */
	private final IMap spmap;
	/**
	 * How many times to repeat each test.
	 */
	private final int reps;
	/**
	 * The size of a tile, factored out to reduce number of lines a line of code
	 * has to use.
	 */
	private final int tsize;

	/**
	 * The first test: all in one place.
	 *
	 * @param helper the helper to test
	 *
	 * @return how long the test took, in ns.
	 */
	public long first(final TileDrawHelper helper) {
		final BufferedImage image = new BufferedImage(tsize, tsize,
				BufferedImage.TYPE_INT_RGB);
		final long start = System.nanoTime();
		firstBody(helper, image);
		final long end = System.nanoTime();
		return end - start;
	}

	/**
	 * The body of the first test.
	 *
	 * @param helper the helper to test
	 * @param image the image used in the test.
	 */
	private void firstBody(final TileDrawHelper helper,
			final BufferedImage image) {
		for (int rep = 0; rep < reps; rep++) {
			image.flush();
			for (final Point point : spmap.getTiles()) {
				helper.drawTile(image.createGraphics(), spmap.getTile(point),
						tsize, tsize);
			}
		}
	}

	/**
	 * The second test: Translating.
	 *
	 * @param helper the helper to test
	 *
	 * @return how long the test took, in ns.
	 */
	public long second(final TileDrawHelper helper) {
		final BufferedImage image = new BufferedImage(tsize * spmap.cols(),
				tsize * spmap.rows(), BufferedImage.TYPE_INT_RGB);
		final long start = System.nanoTime();
		secondBody(helper, image);
		final long end = System.nanoTime();
		return end - start;
	}

	/**
	 * The body of the second test.
	 *
	 * @param helper the helper to test
	 * @param image the image used in the test.
	 */
	private void secondBody(final TileDrawHelper helper,
			final BufferedImage image) {
		final Coordinate dimensions = PointFactory.coordinate(tsize, tsize);
		for (int rep = 0; rep < reps; rep++) {
			image.flush();
			for (final Point point : spmap.getTiles()) {
				helper.drawTile(image.createGraphics(), spmap.getTile(point),
						PointFactory.coordinate(point.row * tsize, point.col
								* tsize), dimensions);
			}
		}
	}

	/**
	 * Third test: in-place, reusing Graphics.
	 *
	 * @param helper the helper to test
	 *
	 * @return how long the test took, in ns.
	 */
	public long third(final TileDrawHelper helper) {
		final BufferedImage image = new BufferedImage(tsize, tsize, // NOPMD
				BufferedImage.TYPE_INT_RGB);
		final long start = System.nanoTime();
		for (int rep = 0; rep < reps; rep++) {
			image.flush();
			final Graphics pen = image.createGraphics();
			thirdBody(helper, pen);
		}
		final long end = System.nanoTime();
		return end - start;
	}

	/**
	 * The body of the third test.
	 *
	 * @param helper the helper being tested
	 * @param pen the Graphics used to draw to the image
	 */
	private void thirdBody(final TileDrawHelper helper, final Graphics pen) {
		for (final Point point : spmap.getTiles()) {
			helper.drawTile(pen, spmap.getTile(point), tsize, tsize);
		}
	}

	/**
	 * Third test: translating, reusing Graphics.
	 *
	 * @param helper the helper to test
	 *
	 * @return how long the test took, in ns.
	 */
	public long fourth(final TileDrawHelper helper) {
		final BufferedImage image = new BufferedImage(tsize * spmap.cols(), // NOPMD
				tsize * spmap.rows(), BufferedImage.TYPE_INT_RGB);
		final long start = System.nanoTime();
		for (int rep = 0; rep < reps; rep++) {
			image.flush();
			final Graphics pen = image.createGraphics();
			fourthBody(helper, pen);
		}
		final long end = System.nanoTime();
		return end - start;
	}

	/**
	 * The body of the fourth test.
	 *
	 * @param helper the helper being tested
	 * @param pen the Graphics used to draw to the image
	 */
	private void fourthBody(final TileDrawHelper helper, final Graphics pen) {
		final Coordinate dimensions = PointFactory.coordinate(tsize, tsize);
		for (final Point point : spmap.getTiles()) {
			helper.drawTile(pen, spmap.getTile(point),
					PointFactory.coordinate(point.row * tsize, point.col * tsize),
					dimensions);
		}
	}

	/**
	 * The minimum row for the iteration-vs-filtering test.
	 */
	private static final int TEST_MIN_ROW = 20;
	/**
	 * The maximum row for the interation-vs-filtering test.
	 */
	private static final int TEST_MAX_ROW = 40;
	/**
	 * The minimum col for the iteration-vs-filtering test.
	 */
	private static final int TEST_MIN_COL = 55;
	/**
	 * The maximum col for the iteration-vs-filtering test.
	 */
	private static final int TEST_MAX_COL = 82;

	/**
	 * Fifth test, part one: iterating.
	 *
	 * @param helper the helper to test
	 *
	 * @return how long the test took, in ns.
	 */
	public long fifthOne(final TileDrawHelper helper) {
		final BufferedImage image = new BufferedImage(tsize * spmap.cols(), // NOPMD
				tsize * spmap.rows(), BufferedImage.TYPE_INT_RGB);
		final long start = System.nanoTime();
		for (int rep = 0; rep < reps; rep++) {
			image.flush();
			final Graphics pen = image.createGraphics();
			fifthOneBody(helper, pen);
		}
		final long end = System.nanoTime();
		return end - start;
	}

	/**
	 * The body of the first part of the fifth test.
	 *
	 * @param helper the helper being tested
	 * @param pen the Graphics used to draw to the image
	 */
	private void fifthOneBody(final TileDrawHelper helper, final Graphics pen) {
		final Coordinate dimensions = PointFactory.coordinate(tsize, tsize);
		for (int row = TEST_MIN_ROW; row < TEST_MAX_ROW; row++) {
			for (int col = TEST_MIN_COL; col < TEST_MAX_COL; col++) {
				final Point point = PointFactory.point(row, col);
				helper.drawTile(pen, spmap.getTile(point),
						PointFactory.coordinate(row * tsize, col * tsize),
						dimensions);
			}
		}
	}

	/**
	 * Fifth test, part two: filtering.
	 *
	 * @param helper the helper to test
	 *
	 * @return how long the test took, in ns.
	 */
	public long fifthTwo(final TileDrawHelper helper) {
		final BufferedImage image = new BufferedImage(tsize * spmap.cols(), // NOPMD
				tsize * spmap.rows(), BufferedImage.TYPE_INT_RGB);
		final long start = System.nanoTime();
		for (int rep = 0; rep < reps; rep++) {
			image.flush();
			final Graphics pen = image.createGraphics();
			fifthTwoBody(helper, pen);
		}
		final long end = System.nanoTime();
		return end - start;
	}

	/**
	 * The body of the first part of the fifth test.
	 *
	 * @param helper the helper being tested
	 * @param pen the Graphics used to draw to the image
	 */
	private void fifthTwoBody(final TileDrawHelper helper, final Graphics pen) {
		final Coordinate dimensions = PointFactory.coordinate(tsize, tsize);
		for (final Point point : spmap.getTiles()) {
			if (point.row >= TEST_MIN_ROW && point.row < TEST_MAX_ROW
					&& point.col >= TEST_MIN_COL
					&& point.col < TEST_MAX_COL) {
				helper.drawTile(
						pen,
						spmap.getTile(point),
						PointFactory.coordinate(point.row * tsize, point.col * tsize),
						dimensions);
			}
		}
	}

	/**
	 * A driver method to compare the two helpers, and the two map-GUI
	 * implementations.
	 *
	 * @param args the command-line arguments.
	 */
	public static void main(final String[] args) { // NOPMD
		final Random random = new Random();
		for (String filename : args) {
			PointFactory.clearCache();
			if (random.nextBoolean()) {
				PointFactory.shouldUseCache(true);
				System.out.println("Using cache:");
				runAllTests(filename);
				PointFactory.shouldUseCache(false);
				System.out.println("Not using cache:");
				runAllTests(filename);
			} else {
				PointFactory.shouldUseCache(false);
				System.out.println("Not using cache:");
				runAllTests(filename);
				PointFactory.shouldUseCache(true);
				System.out.println("Using cache:");
				runAllTests(filename);
			}
		}
	}

	/**
	 * Run all the tests on the specified file.
	 * @param filename the file to use for the tests.
	 */
	private static void runAllTests(final String filename) {
		final Logger logger = Logger.getLogger(DrawHelperComparator.class
				.getName());
		// ESCA-JAVA0177:
		final DrawHelperComparator comp; // NOPMD
		try {
			comp = new DrawHelperComparator(// NOPMD
					new MapReaderAdapter().readMap(filename, new Warning(
							Warning.Action.Ignore)), 50);
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
		SystemOut.SYS_OUT.print("Testing using ");
		SystemOut.SYS_OUT.println(filename);
		final TileDrawHelper helperOne = new CachingTileDrawHelper();
		final TileDrawHelper helperTwo = new DirectTileDrawHelper();
		final TileDrawHelper helperThree = new Ver2TileDrawHelper(null);
		SystemOut.SYS_OUT.println("1. All in one place:");
		SystemOut.SYS_OUT.print(CACHING_NAME);
		long oneTotal = comp.printStats(comp.first(helperOne));
		SystemOut.SYS_OUT.print(DIRECT_NAME);
		long twoTotal = comp.printStats(comp.first(helperTwo));
		SystemOut.SYS_OUT.print(VER_TWO_NAME);
		long threeTotal = comp.printStats(comp.first(helperThree));
		SystemOut.SYS_OUT.println("2. Translating:");
		SystemOut.SYS_OUT.print(CACHING_NAME);
		oneTotal += comp.printStats(comp.second(helperOne));
		SystemOut.SYS_OUT.print(DIRECT_NAME);
		twoTotal += comp.printStats(comp.second(helperTwo));
		SystemOut.SYS_OUT.print(VER_TWO_NAME);
		threeTotal += comp.printStats(comp.second(helperThree));
		SystemOut.SYS_OUT.println("3. In-place, reusing Graphics:");
		SystemOut.SYS_OUT.print(CACHING_NAME);
		oneTotal += comp.printStats(comp.third(helperOne));
		SystemOut.SYS_OUT.print(DIRECT_NAME);
		twoTotal += comp.printStats(comp.third(helperTwo));
		SystemOut.SYS_OUT.print(VER_TWO_NAME);
		threeTotal += comp.printStats(comp.third(helperThree));
		SystemOut.SYS_OUT.println("4. Translating, reusing Graphics:");
		SystemOut.SYS_OUT.print(CACHING_NAME);
		oneTotal += comp.printStats(comp.fourth(helperOne));
		SystemOut.SYS_OUT.print(DIRECT_NAME);
		twoTotal += comp.printStats(comp.fourth(helperTwo));
		SystemOut.SYS_OUT.print(VER_TWO_NAME);
		threeTotal += comp.printStats(comp.fourth(helperThree));
		SystemOut.SYS_OUT.println("5. Ordered iteration vs filtering:");
		SystemOut.SYS_OUT.print("Iteration, ");
		SystemOut.SYS_OUT.print(CACHING_NAME);
		oneTotal += comp.printStats(comp.fifthOne(helperOne));
		SystemOut.SYS_OUT.print("Iteration, ");
		SystemOut.SYS_OUT.print(DIRECT_NAME);
		twoTotal += comp.printStats(comp.fifthOne(helperTwo));
		SystemOut.SYS_OUT.print("Iteration, ");
		SystemOut.SYS_OUT.print(VER_TWO_NAME);
		threeTotal += comp.printStats(comp.fifthOne(helperThree));
		SystemOut.SYS_OUT.print("Filtering, ");
		SystemOut.SYS_OUT.print(CACHING_NAME);
		oneTotal += comp.printStats(comp.fifthTwo(helperOne));
		SystemOut.SYS_OUT.print("Filtering, ");
		SystemOut.SYS_OUT.print(DIRECT_NAME);
		twoTotal += comp.printStats(comp.fifthTwo(helperTwo));
		SystemOut.SYS_OUT.print("Filtering, ");
		SystemOut.SYS_OUT.print(VER_TWO_NAME);
		threeTotal += comp.printStats(comp.fifthTwo(helperThree));
		SystemOut.SYS_OUT.println("--------------------------------------");
		SystemOut.SYS_OUT.print("Total:");
		SystemOut.SYS_OUT.print(CACHING_NAME);
		comp.printStats(oneTotal);
		SystemOut.SYS_OUT.print(DIRECT_NAME);
		comp.printStats(twoTotal);
		SystemOut.SYS_OUT.print(VER_TWO_NAME);
		comp.printStats(threeTotal);
		SystemOut.SYS_OUT.println();
	}

	/**
	 * A helper method to reduce repeated strings.
	 *
	 * @param total the total time
	 * @return that total
	 */
	public long printStats(final long total) {
		SystemOut.SYS_OUT.print('\t');
		SystemOut.SYS_OUT.print(total);
		SystemOut.SYS_OUT.print(", average of\t");
		SystemOut.SYS_OUT.print(Long.toString(total / reps));
		SystemOut.SYS_OUT.println(" ns.");
		return total;
	}

	/**
	 *
	 * @return a String representation of this object
	 */
	@Override
	public String toString() {
		return "DrawHelperComparator";
	}
}

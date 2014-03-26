package controller.map.drivers;

import static view.util.SystemOut.SYS_OUT;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.IOException;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.stream.XMLStreamException;

import model.map.IMap;
import model.map.MapDimensions;
import model.map.Point;
import model.map.PointFactory;
import model.map.TileFixture;
import model.viewer.TileViewSize;
import model.viewer.ViewerModel;
import model.viewer.ZOrderFilter;

import org.eclipse.jdt.annotation.Nullable;

import util.TypesafeLogger;
import util.Warning;
import util.Warning.Action;
import view.map.main.CachingTileDrawHelper;
import view.map.main.DirectTileDrawHelper;
import view.map.main.TileDrawHelper;
import view.map.main.Ver2TileDrawHelper;
import view.util.Coordinate;
import controller.map.drivers.ISPDriver.DriverUsage.ParamCount;
import controller.map.formatexceptions.SPFormatException;
import controller.map.misc.MapReaderAdapter;

/**
 * A driver to compare the performance of TileDrawHelpers.
 *
 * @author Jonathan Lovelace
 *
 */
public class DrawHelperComparator implements ISPDriver { // NOPMD
	/**
	 * The error message to use when a null Graphics is passed in.
	 */
	private static final String NULL_GRAPHICS_ERR = "image.createGraphics() created null Graphics";

	/**
	 * An object indicating how to use and invoke this driver. We say that this
	 * is graphical, even though it's not, so we can share an option with the
	 * ReaderComparator.
	 */
	private static final DriverUsage USAGE_OBJ = new DriverUsage(true, "-t",
			"--test", ParamCount.Many, "Test drawing performance",
			"Test the performance of the TileDrawHelper classes---which "
					+ "do the heavy lifting of rendering the map\n"
					+ "in the viewer---using a variety of automated tests.",
			DrawHelperComparator.class);

	/**
	 * Label to put before every direct-helper test result.
	 */
	private static final String DIRECT = "Direct:  ";
	/**
	 * Label to put before every caching-helper test result.
	 */
	private static final String CACHING = "Caching:";
	/**
	 * Label to put before every version-2 helper test result.
	 */
	private static final String VER_TWO = "Ver. 2: ";

	/**
	 * The first test: all in one place.
	 *
	 * @param helper the helper to test
	 * @param spmap the map being used for the test
	 * @param reps the number of times to run this test between starting and
	 *        stopping the timer
	 * @param tsize the size to draw each tile
	 *
	 * @return how long the test took, in ns.
	 */
	public static long first(final TileDrawHelper helper, final IMap spmap,
			final int reps, final int tsize) {
		final BufferedImage image = new BufferedImage(tsize, tsize,
				BufferedImage.TYPE_INT_RGB);
		final long start = System.nanoTime();
		firstBody(helper, image, spmap, reps, tsize);
		final long end = System.nanoTime();
		return end - start;
	}

	/**
	 * The body of the first test.
	 *
	 * @param helper the helper to test
	 * @param image the image used in the test.
	 * @param spmap the map being used for the test
	 * @param reps the number of times to run this test between starting and
	 *        stopping the timer
	 * @param tsize the size to draw each tile
	 */
	private static void firstBody(final TileDrawHelper helper,
			final BufferedImage image, final IMap spmap, final int reps,
			final int tsize) {
		for (int rep = 0; rep < reps; rep++) {
			image.flush();
			for (final Point point : spmap.getTiles()) {
				if (point == null) {
					continue;
				}
				final Graphics pen = image.createGraphics();
				if (pen == null) {
					throw new IllegalStateException(
							NULL_GRAPHICS_ERR);
				}
				helper.drawTileTranslated(pen, spmap.getTile(point), tsize, tsize);
			}
		}
	}

	/**
	 * The second test: Translating.
	 *
	 * @param helper the helper to test
	 * @param spmap the map being used for the test
	 * @param reps the number of times to run this test between starting and
	 *        stopping the timer
	 * @param tsize the size to draw each tile
	 * @return how long the test took, in ns.
	 */
	public static long second(final TileDrawHelper helper, final IMap spmap,
			final int reps, final int tsize) {
		final MapDimensions dim = spmap.getDimensions();
		final BufferedImage image = new BufferedImage(tsize * dim.cols, tsize
				* dim.rows, BufferedImage.TYPE_INT_RGB);
		final long start = System.nanoTime();
		secondBody(helper, image, spmap, reps, tsize);
		final long end = System.nanoTime();
		return end - start;
	}

	/**
	 * The body of the second test.
	 *
	 * @param helper the helper to test
	 * @param image the image used in the test.
	 * @param spmap the map being used for the test
	 * @param reps the number of times to run this test between starting and
	 *        stopping the timer
	 * @param tsize the size to draw each tile
	 */
	private static void secondBody(final TileDrawHelper helper,
			final BufferedImage image, final IMap spmap, final int reps,
			final int tsize) {
		final Coordinate dimensions = PointFactory.coordinate(tsize, tsize);
		for (int rep = 0; rep < reps; rep++) {
			image.flush();
			for (final Point point : spmap.getTiles()) {
				if (point == null) {
					continue;
				}
				final Graphics pen = image.createGraphics();
				if (pen == null) {
					throw new IllegalStateException(
							NULL_GRAPHICS_ERR);
				}
				helper.drawTile(
						pen,
						spmap.getTile(point),
						PointFactory.coordinate(point.row * tsize, point.col
								* tsize), dimensions);
			}
		}
	}

	/**
	 * Third test: in-place, reusing Graphics.
	 *
	 * @param helper the helper to test
	 * @param spmap the map being used for the test
	 * @param reps the number of times to run this test between starting and
	 *        stopping the timer
	 * @param tsize the size to draw each tile
	 * @return how long the test took, in ns.
	 */
	public static long third(final TileDrawHelper helper, final IMap spmap,
			final int reps, final int tsize) {
		final BufferedImage image = new BufferedImage(tsize, tsize, // NOPMD
				BufferedImage.TYPE_INT_RGB);
		final long start = System.nanoTime();
		for (int rep = 0; rep < reps; rep++) {
			image.flush();
			final Graphics pen = image.createGraphics();
			if (pen == null) {
				throw new IllegalStateException(
						NULL_GRAPHICS_ERR);
			}
			thirdBody(helper, pen, spmap, tsize);
		}
		final long end = System.nanoTime();
		return end - start;
	}

	/**
	 * The body of the third test.
	 *
	 * @param helper the helper being tested
	 * @param pen the Graphics used to draw to the image
	 * @param spmap the map being used for the test
	 * @param tsize the size to draw each tile
	 */
	private static void thirdBody(final TileDrawHelper helper,
			final Graphics pen, final IMap spmap, final int tsize) {
		for (final Point point : spmap.getTiles()) {
			if (point != null) {
				helper.drawTileTranslated(pen, spmap.getTile(point), tsize, tsize);
			}
		}
	}

	/**
	 * Third test: translating, reusing Graphics.
	 *
	 * @param helper the helper to test
	 * @param spmap the map being used for the test
	 * @param reps the number of times to run this test between starting and
	 *        stopping the timer
	 * @param tsize the size to draw each tile
	 * @return how long the test took, in ns.
	 */
	public static long fourth(final TileDrawHelper helper, final IMap spmap,
			final int reps, final int tsize) {
		final MapDimensions dim = spmap.getDimensions();
		final BufferedImage image = new BufferedImage(tsize * dim.cols, // NOPMD
				tsize * dim.rows, BufferedImage.TYPE_INT_RGB);
		final long start = System.nanoTime();
		for (int rep = 0; rep < reps; rep++) {
			image.flush();
			final Graphics pen = image.createGraphics();
			if (pen == null) {
				throw new IllegalStateException(
						NULL_GRAPHICS_ERR);
			}
			fourthBody(helper, pen, spmap, tsize);
		}
		final long end = System.nanoTime();
		return end - start;
	}

	/**
	 * The body of the fourth test.
	 *
	 * @param helper the helper being tested
	 * @param pen the Graphics used to draw to the image
	 * @param spmap the map being used for the test
	 * @param tsize the size to draw each tile
	 */
	private static void fourthBody(final TileDrawHelper helper,
			final Graphics pen, final IMap spmap, final int tsize) {
		final Coordinate dimensions = PointFactory.coordinate(tsize, tsize);
		for (final Point point : spmap.getTiles()) {
			if (point != null) {
				helper.drawTile(
						pen,
						spmap.getTile(point),
						PointFactory.coordinate(point.row * tsize, point.col
								* tsize), dimensions);
			}
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
	 * @param spmap the map being used for the test
	 * @param reps the number of times to run this test between starting and
	 *        stopping the timer
	 * @param tsize the size to draw each tile
	 * @return how long the test took, in ns.
	 */
	public static long fifthOne(final TileDrawHelper helper, final IMap spmap,
			final int reps, final int tsize) {
		final MapDimensions dim = spmap.getDimensions();
		final BufferedImage image = new BufferedImage(tsize * dim.cols, // NOPMD
				tsize * dim.rows, BufferedImage.TYPE_INT_RGB);
		final long start = System.nanoTime();
		for (int rep = 0; rep < reps; rep++) {
			image.flush();
			final Graphics pen = image.createGraphics();
			if (pen == null) {
				throw new IllegalStateException(
						NULL_GRAPHICS_ERR);
			}
			fifthOneBody(spmap, helper, pen, tsize);
		}
		final long end = System.nanoTime();
		return end - start;
	}

	/**
	 * The body of the first part of the fifth test.
	 *
	 * @param helper the helper being tested
	 * @param pen the Graphics used to draw to the image
	 * @param spmap the map being used for the test
	 * @param tsize the size to draw each tile
	 */
	private static void fifthOneBody(final IMap spmap,
			final TileDrawHelper helper, final Graphics pen, final int tsize) {
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
	 * @param spmap the map being used for the test
	 * @param reps the number of times to run this test between starting and
	 *        stopping the timer
	 * @param tsize the size to draw each tile
	 * @return how long the test took, in ns.
	 */
	public static long fifthTwo(final TileDrawHelper helper, final IMap spmap,
			final int reps, final int tsize) {
		final MapDimensions dim = spmap.getDimensions();
		final BufferedImage image = new BufferedImage(tsize * dim.cols, // NOPMD
				tsize * dim.rows, BufferedImage.TYPE_INT_RGB);
		final long start = System.nanoTime();
		for (int rep = 0; rep < reps; rep++) {
			image.flush();
			final Graphics pen = image.createGraphics();
			if (pen == null) {
				throw new IllegalStateException(
						NULL_GRAPHICS_ERR);
			}
			fifthTwoBody(helper, pen, spmap, tsize);
		}
		final long end = System.nanoTime();
		return end - start;
	}

	/**
	 * The body of the first part of the fifth test.
	 *
	 * @param helper the helper being tested
	 * @param pen the Graphics used to draw to the image
	 * @param spmap the map being used for the test
	 * @param tsize the size to draw each tile
	 */
	private static void fifthTwoBody(final TileDrawHelper helper,
			final Graphics pen, final IMap spmap, final int tsize) {
		final Coordinate dimensions = PointFactory.coordinate(tsize, tsize);
		for (final Point point : spmap.getTiles()) {
			if (point.row >= TEST_MIN_ROW && point.row < TEST_MAX_ROW
					&& point.col >= TEST_MIN_COL && point.col < TEST_MAX_COL) {
				helper.drawTile(
						pen,
						spmap.getTile(point),
						PointFactory.coordinate(point.row * tsize, point.col
								* tsize), dimensions);
			}
		}
	}

	/**
	 * Logger.
	 */
	private static final Logger LOGGER = TypesafeLogger
			.getLogger(DrawHelperComparator.class);

	/**
	 * A driver method to compare the two helpers, and the two map-GUI
	 * implementations.
	 *
	 * @param args the command-line arguments.
	 */
	public static void main(final String[] args) { // NOPMD
		try {
			new DrawHelperComparator().startDriver(args);
		} catch (final DriverFailedException except) {
			LOGGER.log(Level.SEVERE, except.getMessage(), except.getCause());
		}
	}

	/**
	 * Run all the tests on the specified file.
	 *
	 * @param map the map to use for the tests.
	 * @param repetitions how many times to repeat each test (more takes longer,
	 *        but gives more precise result)
	 */
	public static void runAllTests(final IMap map, final int repetitions) {
		final int reps = repetitions;
		final int tsize = TileViewSize.scaleZoom(ViewerModel.DEF_ZOOM_LEVEL,
				map.getDimensions().version);
		final TileDrawHelper hOne = new CachingTileDrawHelper();
		final TileDrawHelper hTwo = new DirectTileDrawHelper();
		final TileDrawHelper hThree = new Ver2TileDrawHelper(
				new ImageObserver() {
					@Override
					public boolean imageUpdate(@Nullable final Image img,
							final int infoflags, final int xCoord,
							final int yCoord, final int width, final int height) {
						return false;
					}
				}, new ZOrderFilter() {
					@Override
					public boolean shouldDisplay(final TileFixture fix) {
						return true;
					}
				});
		SYS_OUT.println("1. All in one place:");
		long oneTotal = printStats(CACHING, first(hOne, map, reps, tsize), reps);
		long twoTotal = printStats(DIRECT, first(hTwo, map, reps, tsize), reps);
		long threeTot = printStats(VER_TWO, first(hThree, map, reps, tsize),
				reps);
		SYS_OUT.println("2. Translating:");
		oneTotal += printStats(CACHING, second(hOne, map, reps, tsize), reps);
		twoTotal += printStats(DIRECT, second(hTwo, map, reps, tsize), reps);
		threeTot += printStats(VER_TWO, second(hThree, map, reps, tsize), reps);
		SYS_OUT.println("3. In-place, reusing Graphics:");
		oneTotal += printStats(CACHING, third(hOne, map, reps, tsize), reps);
		twoTotal += printStats(DIRECT, third(hTwo, map, reps, tsize), reps);
		threeTot += printStats(VER_TWO, third(hThree, map, reps, tsize), reps);
		SYS_OUT.println("4. Translating, reusing Graphics:");
		oneTotal += printStats(CACHING, fourth(hOne, map, reps, tsize), reps);
		twoTotal += printStats(DIRECT, fourth(hTwo, map, reps, tsize), reps);
		threeTot += printStats(VER_TWO, fourth(hThree, map, reps, tsize), reps);
		SYS_OUT.println("5. Ordered iteration vs filtering:");
		SYS_OUT.print("Iteration, ");
		oneTotal += printStats(CACHING, fifthOne(hOne, map, reps, tsize), reps);
		SYS_OUT.print("Iteration, ");
		twoTotal += printStats(DIRECT, fifthOne(hTwo, map, reps, tsize), reps);
		SYS_OUT.print("Iteration, ");
		threeTot += printStats(VER_TWO, fifthOne(hThree, map, reps, tsize),
				reps);
		SYS_OUT.print("Filtering, ");
		oneTotal += printStats(CACHING, fifthTwo(hOne, map, reps, tsize), reps);
		SYS_OUT.print("Filtering, ");
		twoTotal += printStats(DIRECT, fifthTwo(hTwo, map, reps, tsize), reps);
		SYS_OUT.print("Filtering, ");
		threeTot += printStats(VER_TWO, fifthTwo(hThree, map, reps, tsize),
				reps);
		SYS_OUT.println("--------------------------------------");
		SYS_OUT.print("Total:");
		printStats(CACHING, oneTotal, reps);
		printStats(DIRECT, twoTotal, reps);
		printStats(VER_TWO, threeTot, reps);
		SYS_OUT.println();
	}

	/**
	 * A helper method to reduce repeated strings.
	 *
	 * @param prefix what to print before the total
	 * @param total the total time
	 * @param reps how many times the test ran
	 * @return that total
	 */
	public static long printStats(final String prefix, final long total,
			final int reps) {
		SYS_OUT.print(prefix);
		SYS_OUT.print('\t');
		SYS_OUT.print(total);
		SYS_OUT.print(", average of\t");
		SYS_OUT.print(Long.toString(total / reps));
		SYS_OUT.println(" ns.");
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

	/**
	 * Run the driver.
	 *
	 * @param args command-line arguments
	 * @throws DriverFailedException on error
	 */
	@Override
	public void startDriver(final String... args) throws DriverFailedException {
		final Random random = new Random();
		final MapReaderAdapter adapter = new MapReaderAdapter();
		final int reps = 50; // NOPMD
		final Warning warner = new Warning(Action.Ignore);
		for (final String filename : args) {
			if (filename == null) {
				continue;
			}
			// ESCA-JAVA0177:
			final IMap map; // NOPMD
			try {
				map = adapter.readMap(filename, warner);
			} catch (final IOException e) {
				LOGGER.log(Level.SEVERE, "I/O error reading map", e);
				continue; // NOPMD
			} catch (final XMLStreamException e) {
				LOGGER.log(Level.SEVERE, "XML error reading map", e);
				continue; // NOPMD
			} catch (final SPFormatException e) {
				LOGGER.log(Level.SEVERE, "Map format error reading map", e);
				continue;
			}
			SYS_OUT.print("Testing using ");
			SYS_OUT.println(filename);
			PointFactory.clearCache();
			if (random.nextBoolean()) {
				PointFactory.shouldUseCache(true);
				SYS_OUT.println("Using cache:");
				runAllTests(map, reps);
				PointFactory.shouldUseCache(false);
				SYS_OUT.println("Not using cache:");
				runAllTests(map, reps);
			} else {
				PointFactory.shouldUseCache(false);
				SYS_OUT.println("Not using cache:");
				runAllTests(map, reps);
				PointFactory.shouldUseCache(true);
				SYS_OUT.println("Using cache:");
				runAllTests(map, reps);
			}
		}
	}

	/**
	 * @return an object indicating how to use and invoke this driver.
	 */
	@Override
	public DriverUsage usage() {
		return USAGE_OBJ;
	}

	/**
	 * @return what to call the driver in a CLI list.
	 */
	@Override
	public String getName() {
		return USAGE_OBJ.getShortDescription();
	}

	/**
	 * @param nomen ignored
	 */
	@Override
	public void setName(final String nomen) {
		throw new IllegalStateException("Can't rename a driver");
	}
}

package controller.map.drivers;

import controller.map.drivers.DriverUsage.ParamCount;
import controller.map.misc.MapReaderAdapter;
import model.map.IMapNG;
import model.map.IMutableMapNG;
import model.map.MapDimensions;
import model.map.Point;
import model.map.PointFactory;
import model.misc.IDriverModel;
import model.misc.IMultiMapModel;
import model.viewer.TileViewSize;
import model.viewer.ViewerModel;
import util.NullCleaner;
import util.Pair;
import util.Warning;
import util.Warning.Action;
import view.map.main.CachingTileDrawHelper;
import view.map.main.DirectTileDrawHelper;
import view.map.main.TileDrawHelper;
import view.map.main.Ver2TileDrawHelper;
import view.util.Coordinate;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Random;

import static view.util.SystemOut.SYS_OUT;

/**
 * A driver to compare the performance of TileDrawHelpers.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2011-2015 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program. If not, see
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public final class DrawHelperComparator implements ISPDriver { // NOPMD
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
	 * An object indicating how to use and invoke this driver. We say that this is
	 * graphical, even though it's not, so we can share an option with the
	 * ReaderComparator.
	 */
	private static final DriverUsage USAGE_OBJ = new DriverUsage(true, "-t",
			                                                            "--test",
			                                                            ParamCount.Many,
			                                                            "Test drawing " +
					                                                            "performance",

			                                                            "Test the " +
					                                                            "performance of the TileDrawHelper classes---which "
					                                                            +
					                                                            "do the " +
					                                                            "heavy " +
					                                                            "lifting" +
					                                                            " of " +
					                                                            "rendering the map\n"
					                                                            +
					                                                            "in the " +
					                                                            "viewer---using a variety of automated tests.",
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
	 * @param spmap  the map being used for the test
	 * @param reps   the number of times to run this test between starting and stopping
	 *               the timer
	 * @param tsize  the size to draw each tile
	 * @return how long the test took, in ns.
	 */
	private static long first(final TileDrawHelper helper, final IMapNG spmap,
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
	 * @param image  the image used in the test.
	 * @param spmap  the map being used for the test
	 * @param reps   the number of times to run this test between starting and stopping
	 *               the timer
	 * @param tsize  the size to draw each tile
	 */
	private static void firstBody(final TileDrawHelper helper,
	                              final BufferedImage image, final IMapNG spmap,
	                              final int reps,
	                              final int tsize) {
		for (int rep = 0; rep < reps; rep++) {
			image.flush();
			for (final Point point : spmap.locations()) {
				helper.drawTileTranslated(
						NullCleaner.assertNotNull(image.createGraphics()),
						spmap, point, tsize, tsize);
			}
		}
	}

	/**
	 * The second test: Translating.
	 *
	 * @param helper the helper to test
	 * @param map    the map being used for the test
	 * @param reps   the number of times to run this test between starting and stopping
	 *               the timer
	 * @param tsize  the size to draw each tile
	 * @return how long the test took, in ns.
	 */
	private static long second(final TileDrawHelper helper, final IMapNG map,
	                           final int reps, final int tsize) {
		final MapDimensions dim = map.dimensions();
		final BufferedImage image = new BufferedImage(tsize * dim.cols, tsize
				                                                                *
				                                                                dim.rows,
				                                             BufferedImage.TYPE_INT_RGB);
		final long start = System.nanoTime();
		secondBody(helper, image, map, reps, tsize);
		final long end = System.nanoTime();
		return end - start;
	}

	/**
	 * The body of the second test.
	 *
	 * @param helper the helper to test
	 * @param image  the image used in the test.
	 * @param map    the map being used for the test
	 * @param reps   the number of times to run this test between starting and stopping
	 *               the timer
	 * @param tsize  the size to draw each tile
	 */
	private static void secondBody(final TileDrawHelper helper,
	                               final BufferedImage image, final IMapNG map,
	                               final int reps,
	                               final int tsize) {
		final Coordinate dimensions = PointFactory.coordinate(tsize, tsize);
		for (int rep = 0; rep < reps; rep++) {
			image.flush();
			for (final Point point : map.locations()) {
				helper.drawTile(
						NullCleaner.assertNotNull(image.createGraphics()),
						map, point,
						PointFactory.coordinate(point.row * tsize, point.col
								                                           * tsize),
						dimensions);
			}
		}
	}

	/**
	 * Third test: in-place, reusing Graphics.
	 *
	 * @param helper the helper to test
	 * @param spmap  the map being used for the test
	 * @param reps   the number of times to run this test between starting and stopping
	 *               the timer
	 * @param tsize  the size to draw each tile
	 * @return how long the test took, in ns.
	 */
	private static long third(final TileDrawHelper helper, final IMapNG spmap,
	                          final int reps, final int tsize) {
		final BufferedImage image = new BufferedImage(tsize, tsize, // NOPMD
				                                             BufferedImage.TYPE_INT_RGB);
		final long start = System.nanoTime();
		for (int rep = 0; rep < reps; rep++) {
			image.flush();
			thirdBody(helper,
					NullCleaner.assertNotNull(image.createGraphics()), spmap,
					tsize);
		}
		final long end = System.nanoTime();
		return end - start;
	}

	/**
	 * The body of the third test.
	 *
	 * @param helper the helper being tested
	 * @param pen    the Graphics used to draw to the image
	 * @param spmap  the map being used for the test
	 * @param tsize  the size to draw each tile
	 */
	private static void thirdBody(final TileDrawHelper helper,
	                              final Graphics pen, final IMapNG spmap,
	                              final int tsize) {
		for (final Point point : spmap.locations()) {
			helper.drawTileTranslated(pen, spmap, point, tsize, tsize);
		}
	}

	/**
	 * Third test: translating, reusing Graphics.
	 *
	 * @param helper the helper to test
	 * @param spmap  the map being used for the test
	 * @param reps   the number of times to run this test between starting and stopping
	 *               the timer
	 * @param tsize  the size to draw each tile
	 * @return how long the test took, in ns.
	 */
	private static long fourth(final TileDrawHelper helper, final IMapNG spmap,
	                           final int reps, final int tsize) {
		final MapDimensions dim = spmap.dimensions();
		final BufferedImage image = new BufferedImage(tsize * dim.cols, // NOPMD
				                                             tsize * dim.rows,
				                                             BufferedImage.TYPE_INT_RGB);
		final long start = System.nanoTime();
		for (int rep = 0; rep < reps; rep++) {
			image.flush();
			fourthBody(helper,
					NullCleaner.assertNotNull(image.createGraphics()), spmap,
					tsize);
		}
		final long end = System.nanoTime();
		return end - start;
	}

	/**
	 * The body of the fourth test.
	 *
	 * @param helper the helper being tested
	 * @param pen    the Graphics used to draw to the image
	 * @param spmap  the map being used for the test
	 * @param tsize  the size to draw each tile
	 */
	private static void fourthBody(final TileDrawHelper helper,
	                               final Graphics pen, final IMapNG spmap,
	                               final int tsize) {
		final Coordinate dimensions = PointFactory.coordinate(tsize, tsize);
		for (final Point point : spmap.locations()) {
			helper.drawTile(pen, spmap, point,
					PointFactory.coordinate(point.row * tsize, point.col * tsize),
					dimensions);
		}
	}

	/**
	 * Fifth test, part one: iterating.
	 *
	 * @param helper the helper to test
	 * @param spmap  the map being used for the test
	 * @param reps   the number of times to run this test between starting and stopping
	 *               the timer
	 * @param tsize  the size to draw each tile
	 * @return how long the test took, in ns.
	 */
	private static long fifthOne(final TileDrawHelper helper, final IMapNG spmap,
	                             final int reps, final int tsize) {
		final MapDimensions dim = spmap.dimensions();
		final BufferedImage image = new BufferedImage(tsize * dim.cols, // NOPMD
				                                             tsize * dim.rows,
				                                             BufferedImage.TYPE_INT_RGB);
		final long start = System.nanoTime();
		for (int rep = 0; rep < reps; rep++) {
			image.flush();
			fifthOneBody(spmap, helper,
					NullCleaner.assertNotNull(image.createGraphics()), tsize);
		}
		final long end = System.nanoTime();
		return end - start;
	}

	/**
	 * The body of the first part of the fifth test.
	 *
	 * @param helper the helper being tested
	 * @param pen    the Graphics used to draw to the image
	 * @param spmap  the map being used for the test
	 * @param tsize  the size to draw each tile
	 */
	private static void fifthOneBody(final IMapNG spmap,
	                                 final TileDrawHelper helper, final Graphics pen,
	                                 final int tsize) {
		final Coordinate dimensions = PointFactory.coordinate(tsize, tsize);
		for (int row = TEST_MIN_ROW; row < TEST_MAX_ROW; row++) {
			for (int col = TEST_MIN_COL; col < TEST_MAX_COL; col++) {
				final Point point = PointFactory.point(row, col);
				helper.drawTile(pen, spmap, point,
						PointFactory.coordinate(row * tsize, col * tsize),
						dimensions);
			}
		}
	}

	/**
	 * Fifth test, part two: filtering.
	 *
	 * @param helper the helper to test
	 * @param spmap  the map being used for the test
	 * @param reps   the number of times to run this test between starting and stopping
	 *               the timer
	 * @param tsize  the size to draw each tile
	 * @return how long the test took, in ns.
	 */
	private static long fifthTwo(final TileDrawHelper helper, final IMapNG spmap,
	                             final int reps, final int tsize) {
		final MapDimensions dim = spmap.dimensions();
		final BufferedImage image = new BufferedImage(tsize * dim.cols, // NOPMD
				                                             tsize * dim.rows,
				                                             BufferedImage.TYPE_INT_RGB);
		final long start = System.nanoTime();
		for (int rep = 0; rep < reps; rep++) {
			image.flush();
			fifthTwoBody(helper,
					NullCleaner.assertNotNull(image.createGraphics()), spmap,
					tsize);
		}
		final long end = System.nanoTime();
		return end - start;
	}

	/**
	 * The body of the first part of the fifth test.
	 *
	 * @param helper the helper being tested
	 * @param pen    the Graphics used to draw to the image
	 * @param spmap  the map being used for the test
	 * @param tsize  the size to draw each tile
	 */
	private static void fifthTwoBody(final TileDrawHelper helper,
	                                 final Graphics pen, final IMapNG spmap,
	                                 final int tsize) {
		final Coordinate dimensions = PointFactory.coordinate(tsize, tsize);
		for (final Point point : spmap.locations()) {
			if (point.row >= TEST_MIN_ROW && point.row < TEST_MAX_ROW
					    && point.col >= TEST_MIN_COL && point.col < TEST_MAX_COL) {
				helper.drawTile(
						pen,
						spmap, point,
						PointFactory.coordinate(point.row * tsize, point.col
								                                           * tsize),
						dimensions);
			}
		}
	}

	/**
	 * Run all the tests on the specified file.
	 *
	 * @param map         the map to use for the tests.
	 * @param repetitions how many times to repeat each test (more takes longer, but
	 *                       gives
	 *                    more precise result)
	 */
	private static void runAllTests(final IMapNG map, final int repetitions) {
		final int tsize = TileViewSize.scaleZoom(ViewerModel.DEF_ZOOM_LEVEL,
				map.dimensions().version);
		final TileDrawHelper hThree = new Ver2TileDrawHelper(
				                                                    (img, infoflags,
				                                                     xCoord, yCoord,
				                                                     width, height) ->
						                                                    false,
				                                                    fix -> true);
		SYS_OUT.println("1. All in one place:");
		final TileDrawHelper hOne = new CachingTileDrawHelper();
		long oneTotal =
				printStats(CACHING, first(hOne, map, repetitions, tsize), repetitions);
		final TileDrawHelper hTwo = new DirectTileDrawHelper();
		long twoTotal =
				printStats(DIRECT, first(hTwo, map, repetitions, tsize), repetitions);
		long threeTot = printStats(VER_TWO, first(hThree, map, repetitions, tsize),
				repetitions);
		SYS_OUT.println("2. Translating:");
		oneTotal +=
				printStats(CACHING, second(hOne, map, repetitions, tsize), repetitions);
		twoTotal +=
				printStats(DIRECT, second(hTwo, map, repetitions, tsize), repetitions);
		threeTot +=
				printStats(VER_TWO, second(hThree, map, repetitions, tsize),
						repetitions);
		SYS_OUT.println("3. In-place, reusing Graphics:");
		oneTotal +=
				printStats(CACHING, third(hOne, map, repetitions, tsize), repetitions);
		twoTotal += printStats(DIRECT, third(hTwo, map, repetitions, tsize),
				repetitions);
		threeTot +=
				printStats(VER_TWO, third(hThree, map, repetitions, tsize), repetitions);
		SYS_OUT.println("4. Translating, reusing Graphics:");
		oneTotal +=
				printStats(CACHING, fourth(hOne, map, repetitions, tsize), repetitions);
		twoTotal +=
				printStats(DIRECT, fourth(hTwo, map, repetitions, tsize), repetitions);
		threeTot +=
				printStats(VER_TWO, fourth(hThree, map, repetitions, tsize),
						repetitions);
		SYS_OUT.println("5. Ordered iteration vs filtering:");
		SYS_OUT.print("Iteration, ");
		oneTotal +=
				printStats(CACHING, fifthOne(hOne, map, repetitions, tsize),
						repetitions);
		SYS_OUT.print("Iteration, ");
		twoTotal +=
				printStats(DIRECT, fifthOne(hTwo, map, repetitions, tsize), repetitions);
		SYS_OUT.print("Iteration, ");
		threeTot += printStats(VER_TWO, fifthOne(hThree, map, repetitions, tsize),
				repetitions);
		SYS_OUT.print("Filtering, ");
		oneTotal +=
				printStats(CACHING, fifthTwo(hOne, map, repetitions, tsize),
						repetitions);
		SYS_OUT.print("Filtering, ");
		twoTotal +=
				printStats(DIRECT, fifthTwo(hTwo, map, repetitions, tsize), repetitions);
		SYS_OUT.print("Filtering, ");
		threeTot += printStats(VER_TWO, fifthTwo(hThree, map, repetitions, tsize),
				repetitions);
		SYS_OUT.println("--------------------------------------");
		SYS_OUT.print("Total:");
		printStats(CACHING, oneTotal, repetitions);
		printStats(DIRECT, twoTotal, repetitions);
		printStats(VER_TWO, threeTot, repetitions);
		SYS_OUT.println();
	}

	/**
	 * A helper method to reduce repeated strings.
	 *
	 * @param prefix what to print before the total
	 * @param total  the total time
	 * @param reps   how many times the test ran
	 * @return that total
	 */
	private static long printStats(final String prefix, final long total,
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
	 * @return a String representation of this object
	 */
	@Override
	public String toString() {
		return "DrawHelperComparator";
	}

	/**
	 * Start the driver.
	 *
	 * @param model the driver model to run on
	 * @throws DriverFailedException if the driver fails for some reason
	 */
	@Override
	public void startDriver(final IDriverModel model) throws DriverFailedException {
		final Random random = new Random();
		final int reps = 50; // NOPMD
		if (model instanceof IMultiMapModel) {
			for (final Pair<IMutableMapNG, File> pair : ((IMultiMapModel) model)
					                                            .getAllMaps()) {
				SYS_OUT.print("Testing using ");
				SYS_OUT.println(pair.second().getName());
				final IMapNG map = pair.first();
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
		} else {
			SYS_OUT.print("Testing using ");
			SYS_OUT.println(model.getMapFile().getName());
			final IMapNG map = model.getMap();
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
	 * Run the driver.
	 *
	 * @param args command-line arguments
	 * @throws DriverFailedException on error
	 */
	@Override
	public void startDriver(final String... args) throws DriverFailedException {
		startDriver(new MapReaderAdapter().readMultiMapModel(new Warning(Action.Ignore),
				new File(args[0]), MapReaderAdapter.namesToFiles(true, args)));
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

package controller.map.drivers;

import controller.map.misc.ICLIHelper;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import model.map.IMapNG;
import model.map.IMutableMapNG;
import model.map.MapDimensions;
import model.map.Point;
import model.map.PointFactory;
import model.misc.IDriverModel;
import model.misc.IMultiMapModel;
import model.viewer.FixtureMatcher;
import model.viewer.TileViewSize;
import model.viewer.ViewerModel;
import util.Pair;
import view.map.main.CachingTileDrawHelper;
import view.map.main.DirectTileDrawHelper;
import view.map.main.TileDrawHelper;
import view.map.main.Ver2TileDrawHelper;
import view.util.Coordinate;

/**
 * A driver to compare the performance of TileDrawHelpers.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2011-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public final class DrawHelperComparator implements SimpleDriver {
	/**
	 * The minimum row for the iteration-vs-filtering test.
	 */
	private static final int TEST_MIN_ROW = 20;
	/**
	 * The maximum row for the iteration-vs-filtering test.
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
	private static final IDriverUsage USAGE =
			new DriverUsage(true, "-t", "--test", ParamCount.AtLeastOne,
								   "Test drawing performance",
								   String.format(
										   "Test the performance of the TileDrawHelper" +
												   " classes---which do the heavy " +
												   "lifting of rendering the map%n" +
												   "%nin the viewer---using a " +
												   "variety of automated tests."));

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
	 * List of pairs of tests with descriptions of them.
	 */
	private static final List<Pair<String, DrawingTest>>
			TESTS =
			Arrays.asList(Pair.of("1. All in one place", DrawHelperComparator::first),
					Pair.of("2. Translating", DrawHelperComparator::second),
					Pair.of("3. In-place, reusing Graphics",
							DrawHelperComparator::third),
					Pair.of("4. Translating, reusing Graphics",
							DrawHelperComparator::fourth),
					Pair.of("5a. Ordered iteration vs filtering: Iteration",
							DrawHelperComparator::fifthOne),
					Pair.of("5b. Ordered iteration vs filtering: Filtering",
							DrawHelperComparator::fifthTwo));
	/**
	 * Instances of the classes being tested.
	 */
	private static final List<Triple<TileDrawHelper, String, LongAccumulator>> HELPERS =
			Arrays.asList(triple(new CachingTileDrawHelper(), CACHING, new LongAccumulator()),
					triple(new DirectTileDrawHelper(), DIRECT, new LongAccumulator()),
					triple(new Ver2TileDrawHelper(
							(img, infoFlags, xCoordinate, yCoordinate, width, height) ->
									false, fix -> true, Collections.singleton(
											new FixtureMatcher(fix -> true, "test"))),
							VER_TWO, new LongAccumulator()));

	/**
	 * The first test: all in one place.
	 *
	 * @param helper   the helper to test
	 * @param map      the map being used for the test
	 * @param reps     the number of times to run this test between starting and stopping
	 *                 the timer
	 * @param tileSize the size to draw each tile
	 * @return how long the test took, in ns.
	 */
	private static long first(final TileDrawHelper helper, final IMapNG map,
							  final int reps, final int tileSize) {
		final BufferedImage image = new BufferedImage(tileSize, tileSize,
															 BufferedImage.TYPE_INT_RGB);
		final long start = System.nanoTime();
		firstBody(helper, image, map, reps, tileSize);
		final long end = System.nanoTime();
		return end - start;
	}

	/**
	 * The body of the first test.
	 *
	 * @param helper   the helper to test
	 * @param image    the image used in the test.
	 * @param map      the map being used for the test
	 * @param reps     the number of times to run this test between starting and stopping
	 *                 the timer
	 * @param tileSize the size to draw each tile
	 */
	private static void firstBody(final TileDrawHelper helper,
								  final BufferedImage image, final IMapNG map,
								  final int reps,
								  final int tileSize) {
		for (int rep = 0; rep < reps; rep++) {
			image.flush();
			for (final Point point : map.locations()) {
				helper.drawTileTranslated(image.createGraphics(), map, point, tileSize,
						tileSize);
			}
		}
	}

	/**
	 * The second test: Translating.
	 *
	 * @param helper   the helper to test
	 * @param map      the map being used for the test
	 * @param reps     the number of times to run this test between starting and stopping
	 *                 the timer
	 * @param tileSize the size to draw each tile
	 * @return how long the test took, in ns.
	 */
	private static long second(final TileDrawHelper helper, final IMapNG map,
							   final int reps, final int tileSize) {
		final MapDimensions dim = map.dimensions();
		final BufferedImage image = new BufferedImage(tileSize * dim.cols,
															 tileSize * dim.rows,
															 BufferedImage.TYPE_INT_RGB);
		final long start = System.nanoTime();
		secondBody(helper, image, map, reps, tileSize);
		final long end = System.nanoTime();
		return end - start;
	}

	/**
	 * The body of the second test.
	 *
	 * @param helper   the helper to test
	 * @param image    the image used in the test.
	 * @param map      the map being used for the test
	 * @param reps     the number of times to run this test between starting and stopping
	 *                 the timer
	 * @param tileSize the size to draw each tile
	 */
	private static void secondBody(final TileDrawHelper helper,
								   final BufferedImage image, final IMapNG map,
								   final int reps,
								   final int tileSize) {
		final Coordinate dimensions = PointFactory.coordinate(tileSize, tileSize);
		for (int rep = 0; rep < reps; rep++) {
			image.flush();
			for (final Point point : map.locations()) {
				helper.drawTile(image.createGraphics(), map, point,
						PointFactory.coordinate(point.getRow() * tileSize,
								point.getCol() * tileSize), dimensions);
			}
		}
	}

	/**
	 * Third test: in-place, reusing Graphics.
	 *
	 * @param helper   the helper to test
	 * @param map      the map being used for the test
	 * @param reps     the number of times to run this test between starting and stopping
	 *                 the timer
	 * @param tileSize the size to draw each tile
	 * @return how long the test took, in ns.
	 */
	private static long third(final TileDrawHelper helper, final IMapNG map,
							  final int reps, final int tileSize) {
		final BufferedImage image =
				new BufferedImage(tileSize, tileSize, BufferedImage.TYPE_INT_RGB);
		final long start = System.nanoTime();
		for (int rep = 0; rep < reps; rep++) {
			image.flush();
			thirdBody(helper, image.createGraphics(), map, tileSize);
		}
		final long end = System.nanoTime();
		return end - start;
	}

	/**
	 * The body of the third test.
	 *
	 * @param helper   the helper being tested
	 * @param pen      the Graphics used to draw to the image
	 * @param map      the map being used for the test
	 * @param tileSize the size to draw each tile
	 */
	private static void thirdBody(final TileDrawHelper helper,
								  final Graphics pen, final IMapNG map,
								  final int tileSize) {
		for (final Point point : map.locations()) {
			helper.drawTileTranslated(pen, map, point, tileSize, tileSize);
		}
	}

	/**
	 * Third test: translating, reusing Graphics.
	 *
	 * @param helper   the helper to test
	 * @param map      the map being used for the test
	 * @param reps     the number of times to run this test between starting and stopping
	 *                 the timer
	 * @param tileSize the size to draw each tile
	 * @return how long the test took, in ns.
	 */
	private static long fourth(final TileDrawHelper helper, final IMapNG map,
							   final int reps, final int tileSize) {
		final MapDimensions dim = map.dimensions();
		final BufferedImage image =
				new BufferedImage(tileSize * dim.cols, tileSize * dim.rows,
										 BufferedImage.TYPE_INT_RGB);
		final long start = System.nanoTime();
		for (int rep = 0; rep < reps; rep++) {
			image.flush();
			fourthBody(helper, image.createGraphics(), map, tileSize);
		}
		final long end = System.nanoTime();
		return end - start;
	}

	/**
	 * The body of the fourth test.
	 *
	 * @param helper   the helper being tested
	 * @param pen      the Graphics used to draw to the image
	 * @param map      the map being used for the test
	 * @param tileSize the size to draw each tile
	 */
	private static void fourthBody(final TileDrawHelper helper,
								   final Graphics pen, final IMapNG map,
								   final int tileSize) {
		final Coordinate dimensions = PointFactory.coordinate(tileSize, tileSize);
		for (final Point point : map.locations()) {
			helper.drawTile(pen, map, point, PointFactory.coordinate(
					point.getRow() * tileSize, point.getCol() * tileSize), dimensions);
		}
	}

	/**
	 * Fifth test, part one: iterating.
	 *
	 * @param helper   the helper to test
	 * @param map      the map being used for the test
	 * @param reps     the number of times to run this test between starting and stopping
	 *                 the timer
	 * @param tileSize the size to draw each tile
	 * @return how long the test took, in ns.
	 */
	private static long fifthOne(final TileDrawHelper helper, final IMapNG map,
								 final int reps, final int tileSize) {
		final MapDimensions dim = map.dimensions();
		final BufferedImage image = new BufferedImage(tileSize * dim.cols,
															 tileSize * dim.rows,
															 BufferedImage.TYPE_INT_RGB);
		final long start = System.nanoTime();
		for (int rep = 0; rep < reps; rep++) {
			image.flush();
			fifthOneBody(map, helper, image.createGraphics(), tileSize);
		}
		final long end = System.nanoTime();
		return end - start;
	}

	/**
	 * The body of the first part of the fifth test.
	 *
	 * @param helper   the helper being tested
	 * @param pen      the Graphics used to draw to the image
	 * @param map      the map being used for the test
	 * @param tileSize the size to draw each tile
	 */
	private static void fifthOneBody(final IMapNG map,
									 final TileDrawHelper helper, final Graphics pen,
									 final int tileSize) {
		final Coordinate dimensions = PointFactory.coordinate(tileSize, tileSize);
		for (int row = TEST_MIN_ROW; row < TEST_MAX_ROW; row++) {
			for (int col = TEST_MIN_COL; col < TEST_MAX_COL; col++) {
				final Point point = PointFactory.point(row, col);
				helper.drawTile(pen, map, point,
						PointFactory.coordinate(row * tileSize, col * tileSize),
						dimensions);
			}
		}
	}

	/**
	 * Fifth test, part two: filtering.
	 *
	 * @param helper   the helper to test
	 * @param map      the map being used for the test
	 * @param reps     the number of times to run this test between starting and stopping
	 *                 the timer
	 * @param tileSize the size to draw each tile
	 * @return how long the test took, in ns.
	 */
	private static long fifthTwo(final TileDrawHelper helper, final IMapNG map,
								 final int reps, final int tileSize) {
		final MapDimensions dim = map.dimensions();
		final BufferedImage image = new BufferedImage(tileSize * dim.cols,
															 tileSize * dim.rows,
															 BufferedImage.TYPE_INT_RGB);
		final long start = System.nanoTime();
		for (int rep = 0; rep < reps; rep++) {
			image.flush();
			fifthTwoBody(helper, image.createGraphics(), map, tileSize);
		}
		final long end = System.nanoTime();
		return end - start;
	}

	/**
	 * The body of the first part of the fifth test.
	 *
	 * @param helper   the helper being tested
	 * @param pen      the Graphics used to draw to the image
	 * @param map      the map being used for the test
	 * @param tileSize the size to draw each tile
	 */
	private static void fifthTwoBody(final TileDrawHelper helper,
									 final Graphics pen, final IMapNG map,
									 final int tileSize) {
		final Coordinate dimensions = PointFactory.coordinate(tileSize, tileSize);
		for (final Point point : map.locations()) {
			if ((point.getRow() >= TEST_MIN_ROW) && (point.getRow() < TEST_MAX_ROW) &&
						(point.getCol() >= TEST_MIN_COL) &&
						(point.getCol() < TEST_MAX_COL)) {
				helper.drawTile(pen, map, point, PointFactory.coordinate(
						point.getRow() * tileSize, point.getCol() * tileSize),
						dimensions);
			}
		}
	}

	/**
	 * Run all the tests on the specified file.
	 *
	 * @param cli the interface for user I/O
	 * @param map         the map to use for the tests.
	 * @param repetitions how many times to repeat each test (more takes longer, but
	 *                       gives
	 *                    more precise result)
	 */
	private static void runAllTests(final ICLIHelper cli, final IMapNG map,
									final int repetitions) {
		for (final Pair<String, DrawingTest> pair : TESTS) {
			cli.printf("%s:%n", pair.first());
			for (final Triple<TileDrawHelper, String, LongAccumulator> testCase :
					HELPERS) {
				testCase.third.add(printStats(cli, testCase.second,
						pair.second().runTest(testCase.first, map, repetitions,
								TileViewSize.scaleZoom(ViewerModel.DEF_ZOOM_LEVEL,
										map.dimensions().version)),
						repetitions));
			}
		}
		cli.printf("--------------------------------------%nTotal:");
		for (final Triple<TileDrawHelper, String, LongAccumulator> testCase : HELPERS) {
			printStats(cli, testCase.second, testCase.third.getValue(), repetitions);
		}
		cli.printf("%n");
	}

	/**
	 * A helper method to reduce repeated strings.
	 *
	 * @param cli the interface for user I/O
	 * @param prefix what to print before the total
	 * @param total  the total time
	 * @param reps   how many times the test ran
	 * @return that total
	 */
	private static long printStats(final ICLIHelper cli, final String prefix,
								   final long total, final int reps) {
		cli.printf("%s\t%d, average of %d ns.%n", prefix, Long.valueOf(total),
				Long.valueOf(total / reps));
		return total;
	}

	/**
	 * A trivial toString().
	 * @return a String representation of this object
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "DrawHelperComparator";
	}
	/**
	 * Get a diagnostic message.
	 * @param caching whether we're caching Points
	 * @return a String saying so
	 */
	private static String getCachingMessage(final boolean caching) {
		if (caching) {
			return "Using cache:";
		} else {
			return "Not using cache:";
		}
	}
	/**
	 * The test procedure. Extracted from startDriver() to reduce duplication.
	 * @param cli the interface for user I/O
	 * @param map the map to use for the tests
	 * @param filename the name of the file it was loaded from, for user I/O only
	 * @param rng the Random instance to use
	 */
	private void runTestProcedure(final ICLIHelper cli, final IMapNG map,
								  final Optional<Path> filename, final Random rng) {
		cli.printf("Testing using%s%n",
				filename.map(Path::toString).orElse("an unsaved map"));
		PointFactory.clearCache();
		final boolean startCaching = rng.nextBoolean();
		PointFactory.shouldUseCache(startCaching);
		cli.println(getCachingMessage(startCaching));
		final int reps = 50;
		runAllTests(cli, map, reps);
		PointFactory.shouldUseCache(!startCaching);
		cli.println(getCachingMessage(!startCaching));
		runAllTests(cli, map, reps);

	}
	/**
	 * Start the driver.
	 *
	 * @param cli the interface for user I/O
	 * @param options command-line options passed in
	 * @param model   the driver model to run on
	 */
	@Override
	public void startDriver(final ICLIHelper cli, final SPOptions options,
							final IDriverModel model) {
		final Random random = new Random();
		if (model instanceof IMultiMapModel) {
			for (final Pair<IMutableMapNG, Optional<Path>> pair :
					((IMultiMapModel) model).getAllMaps()) {
				runTestProcedure(cli, pair.first(), pair.second(), random);
			}
		} else {
			runTestProcedure(cli, model.getMap(), model.getMapFile(), random);
		}
	}

	/**
	 * The usage object.
	 * @return an object indicating how to use and invoke this driver.
	 */
	@Override
	public IDriverUsage usage() {
		return USAGE;
	}

	/**
	 * Interface for the tests.
	 */
	@FunctionalInterface
	private interface DrawingTest {
		/**
		 * Run the test.
		 * @param helper the drawing helper being tested
		 * @param map the map to draw
		 * @param repetitions how many times to repeat the test
		 * @param tileSize how big to draw each tile
		 * @return how long it took to run the test
		 */
		long runTest(final TileDrawHelper helper, final IMapNG map,
					 final int repetitions, final int tileSize);
	}

	/**
	 * A Long accumulator.
	 */
	private static final class LongAccumulator {
		/**
		 * The value.
		 */
		private long value = 0;
		/**
		 * Get the value.
		 * @return the value
		 */
		public long getValue() {
			return value;
		}
		/**
		 * Add a quantity to the stored value.
		 * @param addend how much to add
		 */
		@SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
		public void add(final long addend) {
			value += addend;
		}
	}

	/**
	 * A simple triple.
	 * @param <T> the first type
	 * @param <U> the second type
	 * @param <V> the third type
	 */
	private static class Triple<T, U, V> {
		/**
		 * The first item in the triple.
		 */
		protected final T first;
		/**
		 * The second item in the triple.
		 */
		protected final U second;
		/**
		 * The third item in the triple.
		 */
		protected final V third;

		/**
		 * Constructor. Protected so it can be accessed from enclosing class without
		 * synthetic method.
		 * @param one the first item in the triple.
		 * @param two the second item in the triple.
		 * @param three the third item in the triple.
		 */
		@SuppressWarnings("QuestionableName")
		protected Triple(final T one, final U two, final V three) {
			first = one;
			second = two;
			third = three;
		}

	}
	/**
	 * Triple factory method.
	 * @param one the first item in the triple
	 * @param two the second item in the triple.
	 * @param three the third item in the triple.
	 * @param <T> the type of the first item
	 * @param <U> the type of the second item
	 * @param <V> the type of the third item.
	 * @return the triple
	 */
	@SuppressWarnings("QuestionableName")
	private static <T, U, V> Triple<T, U, V> triple(final T one, final U two,
													final V three) {
		return new Triple<>(one, two, three);
	}
}

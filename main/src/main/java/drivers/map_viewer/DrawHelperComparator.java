package drivers.map_viewer;

import common.xmlio.SPFormatException;
import drivers.common.DriverFailedException;
import drivers.common.FixtureMatcher;
import java.awt.Image;
import java.awt.Graphics;

import java.awt.image.BufferedImage;

import drivers.common.SPOptions;
import drivers.common.UtilityDriver;
import drivers.common.IncorrectUsageException;

import drivers.common.cli.ICLIHelper;

import common.map.MapDimensions;
import common.map.TileFixture;
import common.map.Point;
import common.map.IMapNG;

import impl.xmlio.MapIOHelper;

import common.xmlio.Warning;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.sql.Driver;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import lovelace.util.LongAccumulator;
import lovelace.util.MalformedXMLException;
import lovelace.util.MissingFileException;
import lovelace.util.Range;
import org.javatuples.Pair;
import org.javatuples.Triplet;
import org.jetbrains.annotations.Nullable;

import static drivers.map_viewer.TileViewSize.scaleZoom;

/**
 * A driver to compare the performance of TileDrawHelpers.
 */
public class DrawHelperComparator implements UtilityDriver {
	/**
	 * The first test: Basic drawing.
	 */
	private static long first(final TileDrawHelper helper, final IMapNG map, final int reps,
	                          final int tileSize) {
		MapDimensions mapDimensions = map.getDimensions();
		BufferedImage image = new BufferedImage(tileSize * mapDimensions.getColumns(),
			tileSize * mapDimensions.getRows(), BufferedImage.TYPE_INT_RGB);
		long start = System.currentTimeMillis();
		Coordinate dimensions = new Coordinate(tileSize, tileSize);
		for (int rep = 0; rep < reps; rep++) {
			image.flush();
			for (Point point : map.getLocations()) {
				helper.drawTile(image.createGraphics(), map, point,
					new Coordinate(point.getRow() * tileSize, point.getColumn() * tileSize),
					dimensions);
				}
		}
		long end = System.currentTimeMillis();
		return end - start;
	}

	/**
	 * Second test: Basic drawing, reusing Graphics.
	 */
	private static long second(final TileDrawHelper helper, final IMapNG map, final int reps, final int tileSize) {
		MapDimensions mapDimensions = map.getDimensions();
		BufferedImage image = new BufferedImage(tileSize * mapDimensions.getColumns(),
			tileSize * mapDimensions.getRows(), BufferedImage.TYPE_INT_RGB);
		long start = System.currentTimeMillis();
		for (int rep = 0; rep < reps; rep++) {
			image.flush();
			Graphics pen = image.createGraphics();
			Coordinate dimensions = new Coordinate(tileSize, tileSize);
			for (Point point : map.getLocations()) {
				helper.drawTile(pen, map, point, new Coordinate(point.getRow() * tileSize,
					point.getColumn() * tileSize), dimensions);
			}
			pen.dispose();
		}
		long end = System.currentTimeMillis();
		return end - start;
	}

	private static Range testRowSpan = new Range(20, 40); // TODO: randomize these a bit?
	private static Range testColSpan = new Range(55, 82);

	/**
	 * Third test, part one: iterating.
	 */
	private static long thirdOne(final TileDrawHelper helper, final IMapNG map, final int reps, final int tileSize) {
		MapDimensions mapDimensions = map.getDimensions();
		BufferedImage image = new BufferedImage(tileSize * mapDimensions.getColumns(),
			tileSize * mapDimensions.getRows(), BufferedImage.TYPE_INT_RGB);
		long start = System.currentTimeMillis();
		for (int rep = 0; rep < reps; rep++) {
			image.flush();
			Graphics pen = image.createGraphics();
			Coordinate dimensions = new Coordinate(tileSize, tileSize);
			for (Integer row : testRowSpan) {
				for (Integer col : testColSpan) {
					helper.drawTile(pen, map, new Point(row, col),
						new Coordinate(row * tileSize, col * tileSize),
						dimensions);
				}
			}
			pen.dispose();
		}
		long end = System.currentTimeMillis();
		return end - start;
	}

	private static long thirdTwo(final TileDrawHelper helper, final IMapNG map, final int reps, final int tileSize) {
		MapDimensions mapDimensions = map.getDimensions();
		BufferedImage image = new BufferedImage(tileSize * mapDimensions.getColumns(),
			tileSize * mapDimensions.getRows(), BufferedImage.TYPE_INT_RGB);
		long start = System.currentTimeMillis();
		for (int rep = 0; rep < reps; rep++) {
			image.flush();
			Graphics pen = image.createGraphics();
			Coordinate dimensions = new Coordinate(tileSize, tileSize);
			for (Point point : map.getLocations()) {
				if (testRowSpan.contains(point.getRow()) &&
						testColSpan.contains(point.getColumn())) {
					helper.drawTile(pen, map, point,
						new Coordinate(point.getRow() * tileSize, point.getColumn() * tileSize),
						dimensions);
				}
			}
			pen.dispose();
		}
		long end = System.currentTimeMillis();
		return end - start;
	}

	@FunctionalInterface
	private static interface TestInterface {
		long runTest(TileDrawHelper tdh, IMapNG map, int reps, int tileSize);
	}

	private static final List<Pair<String, TestInterface>> TESTS =
		Collections.unmodifiableList(Arrays.asList(
			Pair.with("1. Basic Drawing", DrawHelperComparator::first),
			Pair.with("2. Basic Drawing, reusing Graphics", DrawHelperComparator::second),
			Pair.with("3a. Ordered iteration vs filtering: Iteration", DrawHelperComparator::thirdOne),
			Pair.with("3b. Ordered iteration vs filtering: Filtering", DrawHelperComparator::thirdTwo)));

	private static boolean dummyObserver(@Nullable final Image image, final int infoflags,
	                                     final int xCoordinate, final int yCoordinate, final int width, final int height) {
		return false;
	}

	private static boolean dummyFilter(@Nullable final TileFixture fix) {
		return true;
	}

	private final List<Pair<TileDrawHelper, String>> helpers =
		Collections.unmodifiableList(Arrays.asList(
			Pair.with(new Ver2TileDrawHelper(DrawHelperComparator::dummyObserver,
					DrawHelperComparator::dummyFilter,
					Collections.singleton(new FixtureMatcher(DrawHelperComparator::dummyFilter, "test"))),
				"Ver 2:")));

	private final Map<Triplet<String, String, String>, LongAccumulator> results = new HashMap<>();

	private final LongAccumulator getResultsAccumulator(final String file, final String testee, final String test) {
		final Triplet<String, String, String> tuple = Triplet.with(file, testee, test);
		if (results.containsKey(tuple)) {
			return results.get(tuple);
		} else {
			LongAccumulator retval = new LongAccumulator(0);
			results.put(tuple, retval);
			return retval;
		}
	}

	private final ICLIHelper cli;
	private final SPOptions options;

	@Override
	public SPOptions getOptions() {
		return options;
	}

	public DrawHelperComparator(final ICLIHelper cli, final SPOptions options) {
		this.cli = cli;
		this.options = options;
	}

	private final long printStats(final String prefix, final long total, final int reps) {
		cli.println(String.format("%s\t%d, average of %d ms.", prefix, total, total / reps)); // TODO: quotient should give sub-decimal precision
		return total;
	}

	/**
	 * Run all the tests on the specified map.
	 */
	private void runAllTests(final IMapNG map, final String fileName, final int repetitions) {
		for (Pair<String, TestInterface> pair : TESTS) {
			String testDesc = pair.getValue0();
			TestInterface test = pair.getValue1();
			cli.println(testDesc + ":");
			for (Pair<TileDrawHelper, String> inner : helpers) {
				TileDrawHelper testCase = inner.getValue0();
				String caseDesc = inner.getValue1();
				LongAccumulator accumulator = getResultsAccumulator(fileName, caseDesc, testDesc);
				accumulator.add(printStats(caseDesc, test.runTest(testCase, map, repetitions,
								scaleZoom(ViewerModel.DEFAULT_ZOOM_LEVEL, map.getDimensions().getVersion())),
						repetitions));
			}
		}
		cli.println("----------------------------------------");
		cli.print("Total:");
		for (Pair<TileDrawHelper, String> inner : helpers) {
			TileDrawHelper testCase = inner.getValue0();
			String caseDesc = inner.getValue1();
			printStats(caseDesc, results.entrySet().stream()
				.filter(entry -> fileName.equals(entry.getKey().getValue0()) &&
					caseDesc.equals(entry.getKey().getValue1()))
				.map(Map.Entry::getValue).mapToLong(LongAccumulator::getSum).sum(), repetitions);
		}
		cli.println("");
	}

	private static final int REPS = 50;

	/**
	 * Run the tests.
	 */
	@Override
	public void startDriver(final String... args) throws DriverFailedException {
		if (args.length == 0) {
			throw new IncorrectUsageException(DrawHelperComparatorFactory.USAGE);
		}
		Map<String, Integer> mapSizes = new HashMap<>();
		for (String arg : args) {
			Path path = Paths.get(arg);
			IMapNG map = null;
			try {
				map = MapIOHelper.readMap(path, Warning.IGNORE);
			} catch (final SPFormatException except) {
				throw new DriverFailedException(except, "SP map format error in " + arg);
			} catch (final MissingFileException|FileNotFoundException|NoSuchFileException except) {
				throw new DriverFailedException(except, arg + " not found");
			} catch (final IOException except) {
				throw new DriverFailedException(except, "I/O error while reading " + arg);
			} catch (final MalformedXMLException except) {
				throw new DriverFailedException(except, "Malformed XML in " + arg);
			}
			mapSizes.put(arg, (int) map.streamLocations().count());
			String filename = path.getFileName().toString();
			cli.println("Testing using " + filename);
			runAllTests(map, filename, REPS);
		}
		String reportFilename = options.getArgument("--report");
		if (!reportFilename.equals("false")) {
			try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(Paths.get(reportFilename)))) {
				writer.println("Filename,# Tile,DrawHelper Tested,Test Case,Repetitions,Time (ns)");
				for (Map.Entry<Triplet<String, String, String>, LongAccumulator> entry :
						results.entrySet()) {
					String file = entry.getKey().getValue0();
					String helper = entry.getKey().getValue1();
					String test = entry.getKey().getValue2();
					LongAccumulator total = entry.getValue();
					writer.println(String.join(",", file,
						Optional.ofNullable(mapSizes.get(file)).map(x -> x.toString()).orElse(""),
						helper, test, Integer.toString(REPS), total.getSum().toString()));
				}
			} catch (final IOException except) {
				throw new DriverFailedException(except, "I/O error while writing results");
			}
		}
	}
}

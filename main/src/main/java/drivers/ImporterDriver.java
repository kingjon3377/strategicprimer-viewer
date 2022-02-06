package drivers;

import drivers.common.DriverFailedException;
import drivers.common.UtilityDriver;
import drivers.common.SPOptions;

import java.awt.image.BufferedImage;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import drivers.common.cli.ICLIHelper;

import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import lovelace.util.MalformedXMLException;
import lovelace.util.Range;
import lovelace.util.ResourceInputStream;

import javax.imageio.ImageIO;

import lovelace.util.EnumCounter;

import common.map.TileType;
import common.map.Point;
import common.map.IMutableMapNG;
import common.map.SPMapNG;
import common.map.MapDimensionsImpl;
import common.map.PlayerCollection;
import common.map.HasName;
import common.map.IMapNG;

import impl.xmlio.MapIOHelper;

import common.idreg.IDRegistrar;
import common.idreg.IDFactory;

import common.map.fixtures.terrain.Forest;

import exploration.common.SurroundingPointIterable;
import org.javatuples.Pair;
import org.jetbrains.annotations.Nullable;

/**
 * An app to let the user create a map from an image.
 */
/* package */ class ImporterDriver implements UtilityDriver {
	private static final Logger LOGGER = Logger.getLogger(ImporterDriver.class.getName());

	private enum ImportableTerrain implements HasName {
		Mountain("mountain"),
		BorealForest("boreal forest"),
		TemperateForest("temperate forest");

		private ImportableTerrain(final String name) {
			this.name = name;
		}

		private final String name;

		@Override
		public String getName() {
			return name;
		}
	}

	private static String pixelString(final int pixel) {
		return String.format("(%d, %d, %d)", (pixel >> 16) & 0xFF, (pixel >> 8) & 0xFF, pixel & 0xFF);
	}

	private final ICLIHelper cli;
	private final SPOptions options;

	private final List<HasName> terrains;

	@Override
	public SPOptions getOptions() {
		return options;
	}

	public ImporterDriver(final ICLIHelper cli, final SPOptions options) {
		this.cli = cli;
		this.options = options;
		terrains = Collections.unmodifiableList(Stream.concat(Stream.of(TileType.values()),
				Stream.of(ImportableTerrain.values())).collect(Collectors.toList()));
	}

	@Nullable
	private /*TileType|ImportableTerrain?*/ HasName askFor(final int color) {
		return cli.chooseFromList(terrains, "Tile type represented by " + pixelString(color),
				"No tile types found to choose from", "Tile type:", false).getValue1();
	}

	@Nullable
	private static Range customRange(final int base, final int span, final int max) {
		if (base + span > max + 1) {
			return new Range(base, max - 1);
		} else {
			return new Range(base, base + span - 1); // TODO: Implement Ceylon's ranges more fully so we can use a class where we would use the 'base:span' format in Ceylon
		}
	}

	private final IDRegistrar idf = new IDFactory();

	@Nullable
	private String findAdjacentForest(final IMapNG map, final Point location) {
		List<Forest> forests = StreamSupport.stream(
						new SurroundingPointIterable(location, map.getDimensions(), 1).spliterator(), false)
				.flatMap(l -> map.getFixtures(l).stream())
				.filter(Forest.class::isInstance).map(Forest.class::cast)
				.collect(Collectors.toList());
		Collections.shuffle(forests);
		return forests.isEmpty() ? null : forests.get(0).getKind();
	}

	@Override
	public void startDriver(final String... args) throws DriverFailedException {
		int size;
		try {
			size = Integer.parseInt(options.getArgument("--size"));
		} catch (final NumberFormatException except) {
			throw new DriverFailedException(except, "--size argument must be numeric");
		}
		LOGGER.fine("--size parameter is " + size);
		for (String arg : args) {
			ResourceInputStream res = null;
			try {
				res = new ResourceInputStream(arg, ImporterDriver.class);
			} catch (final FileNotFoundException except) {
				throw new DriverFailedException(except, "Image file not found");
			}
			BufferedImage image = null;
			try {
				image = ImageIO.read(res);
			} catch (final IOException except) {
				throw new DriverFailedException(except, "I/O error reading image");
			}
			final int width = image.getWidth();
			final int height = image.getHeight();
			LOGGER.fine(String.format("Image is %dx%d", width, height));
			int baseRow = 0;
			final Map<Integer, /*TileType|ImportableTerrain*/HasName> mapping =
					new HashMap<>();
			int mapRow = 0;
			final Map<Point, /*TileType|ImportableTerrain*/HasName> retval =
					new HashMap<>();
			while (baseRow < height) {
				int baseColumn = 0;
				int mapColumn = 0;
				while (baseColumn < width) {
					EnumCounter<Integer> counter = new EnumCounter<>();
					for (int row : customRange(baseRow, size, height)) {
						for (int column : customRange(baseColumn, size, width)) {
							counter.countMany(image.getRGB(row, column));
						}
					}

					Pair<Integer, Integer> dominant =
							StreamSupport.stream(counter.getAllCounts().spliterator(), false)
									.max(Comparator.comparing(Pair::getValue1)).orElse(null);
					if (dominant != null) {
						if (mapping.containsKey(dominant.getValue0())) {
							HasName type = mapping.get(dominant.getValue0());
							LOGGER.fine(String.format("Type for (%d, %d) deduced to be %s",
									mapRow, mapColumn, type));
							retval.put(new Point(mapRow, mapColumn), type);
						} else {
							HasName type = askFor(dominant.getValue0());
							if (type != null) {
								mapping.put(dominant.getValue0(), type);
								retval.put(new Point(mapRow, mapColumn), type);
							}
						}
					}
					baseColumn += size;
					mapColumn++;
				}
				baseRow += size;
				mapRow++;
			}
			IMutableMapNG finalRetval = new SPMapNG(new MapDimensionsImpl(
					retval.keySet().stream().mapToInt(Point::getRow).max().orElse(0) + 1,
					retval.keySet().stream().mapToInt(Point::getColumn).max().orElse(0) + 1, 2),
					new PlayerCollection(), -1);
			for (Map.Entry<Point, HasName> entry : retval.entrySet()) {
				Point point = entry.getKey();
				HasName type = entry.getValue();
				LOGGER.finer(String.format("Setting %s to %s", point, type));
				if (type instanceof TileType) {
					finalRetval.setBaseTerrain(point, (TileType) type);
				} else {
					ImportableTerrain terr = (ImportableTerrain) type;
					switch (terr) {
						case Mountain:
							finalRetval.setBaseTerrain(point, TileType.Plains);
							finalRetval.setMountainous(point, true);
							break;
						case TemperateForest:
							finalRetval.setBaseTerrain(point, TileType.Plains);
							String foundTForest = findAdjacentForest(finalRetval, point);
							if (foundTForest != null) {
								finalRetval.addFixture(point, new Forest(foundTForest, false,
										idf.createID()));
								continue;
							}
							String inputTForest = cli.inputString("Kind of tree for a temperate forest: ");
							if (inputTForest == null) {
								return;
							} else {
								finalRetval.addFixture(point, new Forest(inputTForest, false,
										idf.createID()));
							}
							break;
						case BorealForest:
							finalRetval.setBaseTerrain(point, TileType.Steppe);
							String foundBForest = findAdjacentForest(finalRetval, point);
							if (foundBForest != null) {
								finalRetval.addFixture(point, new Forest(foundBForest, false,
										idf.createID()));
								continue;
							}
							String inputBForest = cli.inputString("Kind of tree for a boreal forest: ");
							if (inputBForest == null) {
								return;
							} else {
								finalRetval.addFixture(point, new Forest(inputBForest, false, idf.createID()));
							}
							break;
						default:
							throw new IllegalStateException("Exhaustive switch wasn't");
					}
				}
				try {
					MapIOHelper.writeMap(Paths.get(arg + ".xml"), finalRetval);
				} catch (final IOException except) {
					throw new DriverFailedException(except, "I/O error while writing map");
				} catch (final MalformedXMLException except) {
					throw new DriverFailedException(except, "Map writer produced invalid XML");
				}
			}
		}
	}
}

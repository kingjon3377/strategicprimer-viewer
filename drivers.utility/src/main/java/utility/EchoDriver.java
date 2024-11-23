package utility;

import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.io.IOException;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import javax.xml.stream.XMLStreamException;

import legacy.map.Point;
import legacy.map.TileFixture;
import legacy.idreg.IDRegistrar;
import legacy.idreg.IDFactoryFiller;
import legacy.map.IMutableLegacyMap;
import legacy.map.fixtures.Ground;
import legacy.map.fixtures.terrain.Forest;
import legacy.xmlio.MapIOHelper;
import common.xmlio.Warning;
import common.xmlio.SPFormatException;
import drivers.common.UtilityDriver;
import drivers.common.DriverFailedException;
import drivers.common.IncorrectUsageException;
import drivers.common.SPOptions;

/**
 * A driver that reads in maps and then writes them out again---this is
 * primarily to make sure that the map format is properly read, but is also
 * useful for correcting deprecated syntax. (Because of that usage, warnings
 * are disabled.)
 */
public final class EchoDriver implements UtilityDriver {
	/**
	 * If a Forest doesn't have an ID in the input map, assign it one based on its
	 * location, starting from this number.
	 */
	private static final int FOREST_ID_SEED = 1147200;
	/**
	 * If a Ground doesn't have an ID in the input map, assign it one based on its
	 * location, starting from this number.
	 */
	private static final int GROUND_ID_SEED = 1171484;

	public EchoDriver(final SPOptions options) {
		this.options = options;
	}

	private final SPOptions options;

	@Override
	public SPOptions getOptions() {
		return options;
	}

	/**
	 * Run the driver: read the map, then write it, correcting deprecated
	 * syntax and forest and Ground IDs.
	 */
	@Override
	public void startDriver(final String... args) throws DriverFailedException {
		if (args.length == 2) {
			final String inArg = args[0];
			final String outArg = args[1];
			final IMutableLegacyMap map;
			try {
				map = MapIOHelper.readMap(Paths.get(inArg), Warning.IGNORE);
			} catch (final NoSuchFileException except) {
				throw new DriverFailedException(except, "No such file " + inArg);
			} catch (final IOException except) {
				throw new DriverFailedException(except, "I/O error reading file " + inArg);
			} catch (final XMLStreamException except) {
				throw new DriverFailedException(except, "Malformed XML in " + inArg);
			} catch (final SPFormatException except) {
				throw new DriverFailedException(except, "SP map format error in " + inArg);
			}
			final IDRegistrar idFactory = IDFactoryFiller.createIDFactory(map);
			final int columnCount = map.getDimensions().columns();
			final Predicate<Object> isForest = Forest.class::isInstance;
			final Function<Object, Forest> forestCast = Forest.class::cast;
			final Predicate<Object> isGround = Ground.class::isInstance;
			final Function<Object, Ground> groundCast = Ground.class::cast;
			for (final Point location : map.getLocations()) {
				final Forest mainForest = map.getFixtures(location).stream()
						.filter(isForest).map(forestCast)
						.findFirst().orElse(null);
				if (Objects.nonNull(mainForest) && mainForest.getId() < 0) {
					mainForest.setId(idFactory.register(
							FOREST_ID_SEED + location.row() * columnCount +
									location.column()));
				}
				final Ground mainGround = map.getFixtures(location).stream()
						.filter(isGround).map(groundCast)
						.findFirst().orElse(null);
				if (Objects.nonNull(mainGround) && mainGround.getId() < 0) {
					mainGround.setId(idFactory.register(
							GROUND_ID_SEED + location.row() * columnCount +
									location.column()));
				}
				for (final TileFixture fixture : map.getFixtures(location)) {
					switch (fixture) {
						case final Forest f when fixture.getId() < 0 -> f.setId(idFactory.createID());
						case final Ground g when fixture.getId() < 0 -> g.setId(idFactory.createID());
						default -> {
						}
					}
				}
			}

			if (options.hasOption("--current-turn")) {
				try {
					final int currentTurn = Integer.parseInt(
							options.getArgument("--current-turn"));
					map.setCurrentTurn(currentTurn);
				} catch (final NumberFormatException except) {
					Warning.getDefaultHandler().handle(new Exception(
							"--current-turn must be an integer"));
				}
			}

			try {
				MapIOHelper.writeMap(Paths.get(outArg), map);
			} catch (final XMLStreamException except) {
				throw new DriverFailedException(except, "Malformed XML writing " + outArg);
			} catch (final IOException except) {
				throw new DriverFailedException(except, "I/O error writing " + outArg);
			}
		} else {
			throw new IncorrectUsageException(EchoDriverFactory.USAGE);
		}
	}
}

package utility;

import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.io.IOException;
import java.util.function.Function;
import java.util.function.Predicate;
import javax.xml.stream.XMLStreamException;

import common.map.Point;
import common.map.TileFixture;
import common.idreg.IDRegistrar;
import common.idreg.IDFactoryFiller;
import common.map.IMutableMapNG;
import common.map.fixtures.Ground;
import common.map.fixtures.terrain.Forest;
import impl.xmlio.MapIOHelper;
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
public class EchoDriver implements UtilityDriver {
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
			final IMutableMapNG map;
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
				if (mainForest != null && mainForest.getId() < 0) {
					mainForest.setId(idFactory.register(
						1147200 + location.row() * columnCount +
							location.column()));
				}
				final Ground mainGround = map.getFixtures(location).stream()
					.filter(isGround).map(groundCast)
					.findFirst().orElse(null);
				if (mainGround != null && mainGround.getId() < 0) {
					mainGround.setId(idFactory.register(
						1171484 + location.row() * columnCount +
							location.column()));
				}
				for (final TileFixture fixture : map.getFixtures(location)) {
					if (fixture instanceof Forest f && fixture.getId() < 0) {
						f.setId(idFactory.createID());
					} else if (fixture instanceof Ground g && fixture.getId() < 0) {
						g.setId(idFactory.createID());
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

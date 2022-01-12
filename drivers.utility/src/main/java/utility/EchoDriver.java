package utility;

import lovelace.util.MalformedXMLException;
import java.nio.file.Paths;
import java.io.IOException;
import lovelace.util.MissingFileException;

import java.text.ParseException;
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
import drivers.common.ParamCount;
import drivers.common.UtilityDriver;
import drivers.common.DriverFailedException;
import drivers.common.IncorrectUsageException;
import drivers.common.SPOptions;
import drivers.common.DriverUsage;
import drivers.common.IDriverUsage;
import drivers.common.DriverFactory;
import drivers.common.UtilityDriverFactory;
import drivers.common.cli.ICLIHelper;
import lovelace.util.MalformedXMLException;

import com.google.auto.service.AutoService;

/**
 * A driver that reads in maps and then writes them out again---this is
 * primarily to make sure that the map format is properly read, but is also
 * useful for correcting deprecated syntax. (Because of that usage, warnings
 * are disabled.)
 */
public class EchoDriver implements UtilityDriver {
	public EchoDriver(SPOptions options) {
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
	public void startDriver(String... args) throws DriverFailedException {
		if (args.length == 2) {
			String inArg = args[0];
			String outArg = args[1];
			IMutableMapNG map;
			try {
				map = MapIOHelper.readMap(Paths.get(inArg), Warning.IGNORE);
			} catch (MissingFileException except) {
				throw new DriverFailedException(except, "No such file " + inArg);
			} catch (IOException except) {
				throw new DriverFailedException(except, "I/O error reading file " + inArg);
			} catch (MalformedXMLException except) {
				throw new DriverFailedException(except, "Malformed XML in " + inArg);
			} catch (SPFormatException except) {
				throw new DriverFailedException(except, "SP map format error in " + inArg);
			}
			IDRegistrar idFactory = new IDFactoryFiller().createIDFactory(map);
			int columnCount = map.getDimensions().getColumns();
			for (Point location : map.getLocations()) {
				Forest mainForest = map.getFixtures(location).stream()
					.filter(Forest.class::isInstance).map(Forest.class::cast)
					.findFirst().orElse(null);
				if (mainForest != null && mainForest.getId() < 0) {
					mainForest.setId(idFactory.register(
						1147200 + location.getRow() * columnCount +
							location.getColumn()));
				}
				Ground mainGround = map.getFixtures(location).stream()
					.filter(Ground.class::isInstance).map(Ground.class::cast)
					.findFirst().orElse(null);
				if (mainGround != null && mainGround.getId() < 0) {
					mainGround.setId(idFactory.register(
						1171484 + location.getRow() * columnCount +
							location.getColumn()));
				}
				for (TileFixture fixture : map.getFixtures(location)) {
					if (fixture instanceof Forest && fixture.getId() < 0) {
						((Forest) fixture).setId(idFactory.createID());
					} else if (fixture instanceof Ground && fixture.getId() < 0) {
						((Ground) fixture).setId(idFactory.createID());
					}
				}
			}

			if (options.hasOption("--current-turn")) {
				try {
					int currentTurn = Integer.parseInt(
						options.getArgument("--current-turn"));
					map.setCurrentTurn(currentTurn);
				} catch (NumberFormatException except) {
					Warning.getDefaultHandler().handle(new Exception(
						"--current-turn must be an integer"));
				}
			}

			try {
				MapIOHelper.writeMap(Paths.get(outArg), map);
			} catch (MalformedXMLException except) {
				throw new DriverFailedException(except, "Malformed XML writing " + outArg);
			} catch (IOException except) {
				throw new DriverFailedException(except, "I/O error writing " + outArg);
			}
		} else {
			throw new IncorrectUsageException(EchoDriverFactory.USAGE);
		}
	}
}

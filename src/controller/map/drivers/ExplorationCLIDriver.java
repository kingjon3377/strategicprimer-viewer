package controller.map.drivers;

import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import model.exploration.ExplorationModel;
import model.exploration.IExplorationModel;
import model.map.IMap;
import model.map.MapView;
import model.map.Player;
import model.map.fixtures.mobile.Unit;
import util.Pair;
import util.Warning;
import view.exploration.ExplorationCLI;
import view.util.SystemOut;
import controller.map.formatexceptions.SPFormatException;
import controller.map.misc.CLIHelper;
import controller.map.misc.DriverUsage;
import controller.map.misc.DriverUsage.ParamCount;
import controller.map.misc.MapReaderAdapter;

/**
 * A CLI to help running exploration.
 *
 * @author Jonathan Lovelace
 *
 */
public class ExplorationCLIDriver implements ISPDriver {
	/**
	 * Driver. Takes as its parameters the map files to use.
	 * @param args the command-line arguments
	 */
	public static void main(final String[] args)  {
		try {
			new ExplorationCLIDriver().startDriver(args);
		} catch (DriverFailedException except) {
			System.err.print(except.getMessage());
			System.err.println(':');
			System.err.println(except.getCause().getLocalizedMessage());
		}
	}
	/**
	 * Read maps.
	 * @param filenames the files to read from
	 * @return an exploration-model containing all of them
	 * @throws SPFormatException on SP format problems
	 * @throws XMLStreamException on malformed XML
	 * @throws IOException on basic file I/O error
	 */
	private static ExplorationModel readMaps(final String[] filenames)
			throws IOException, XMLStreamException, SPFormatException {
		final MapReaderAdapter reader = new MapReaderAdapter();
		final MapView master = reader.readMap(filenames[0], Warning.INSTANCE);
		final ExplorationModel model = new ExplorationModel(master, filenames[0]);
		for (final String filename : filenames) {
			if (filename.equals(filenames[0])) {
				continue;
			}
			final IMap map = reader.readMap(filename, Warning.INSTANCE);
			if (!map.getDimensions().equals(master.getDimensions())) {
				throw new IllegalArgumentException("Size mismatch between " + filenames[0] + " and " + filename);
			}
			model.addSubordinateMap(map, filename);
		}
		return model;
	}

	/**
	 * Run the driver.
	 * @param args the command-line arguments
	 * @throws DriverFailedException on error.
	 */
	@Override
	public void startDriver(final String... args) throws DriverFailedException {
		if (args.length == 0) {
			SystemOut.SYS_OUT.println("Usage: ExplorationCLI master-map [player-map ...]");
			System.exit(1);
		}
		// ESCA-JAVA0177:
		final ExplorationModel model; // NOPMD
		try {
			model = readMaps(args);
		} catch (IOException except) {
			throw new DriverFailedException("I/O error reading maps", except);
		} catch (XMLStreamException except) {
			throw new DriverFailedException("Malformed XML in map file", except);
		} catch (SPFormatException except) {
			throw new DriverFailedException("SP format error in map file", except);
		}
		final ExplorationCLI cli = new ExplorationCLI(model, new CLIHelper());
		try {
			final Player player = cli.choosePlayer();
			if (player.getPlayerId() < 0) {
				return; // NOPMD
			}
			final Unit unit = cli.chooseUnit(player);
			if (unit.getID() < 0) {
				return; // NOPMD
			}
			model.selectUnit(unit);
			cli.moveUntilDone();
		} catch (IOException except) {
			throw new DriverFailedException("I/O error interacting with user", except);
		}
		try {
			writeMaps(model);
		} catch (IOException except) {
			throw new DriverFailedException("I/O error writing to a map file", except);
		}
	}
	/**
	 * Write maps to disk.
	 * @param model the model containing all the maps
	 * @throws IOException on I/O error
	 */
	private static void writeMaps(final IExplorationModel model) throws IOException {
		final MapReaderAdapter reader = new MapReaderAdapter();
		for (Pair<IMap, String> pair : model.getAllMaps()) {
			reader.write(pair.second(), pair.first());
		}
	}
	/**
	 * An object indicating how to use and invoke this driver.
	 */
	private static final DriverUsage USAGE_OBJ = new DriverUsage(false, "-x",
			"--explore", ParamCount.Many, "Run exploration.",
			"Move a unit around the map, "
					+ "updating the player's map with what it sees.",
			ExplorationCLIDriver.class);

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

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
import controller.map.misc.MapHelper;
import controller.map.misc.MapReaderAdapter;

/**
 * A CLI to help running exploration. TODO: Some of this should be made more
 * usable from other UIs.
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
	 * TODO: Move much of this logic into class methods, so we don't need as many parameters.
	 * @param unit the unit in motion
	 * @param cli the interface object that does most of this for us.
	 * @param totalMP the unit's total MP (to start with)
	 * @throws IOException on I/O error getting input
	 */
	private static void movementREPL(final ExplorationCLI cli,
			final Unit unit, final int totalMP)
			throws IOException {
		int movement = totalMP;
		while (movement > 0) {
			SystemOut.SYS_OUT.printC(movement).printC(" MP of ")
					.printC(totalMP).println(" remaining.");
			SystemOut.SYS_OUT
					.print("0 = N, 1 = NE, 2 = E, 3 = SE, 4 = S, 5 = SW, ");
			SystemOut.SYS_OUT.println("6 = W, 7 = NW, 8 = Quit.");
			movement -= cli.move(unit);
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
	private static IExplorationModel readMaps(final String[] filenames)
			throws IOException, XMLStreamException, SPFormatException {
		final MapReaderAdapter reader = new MapReaderAdapter();
		final MapView master = reader.readMap(filenames[0], Warning.INSTANCE);
		final IExplorationModel model = new ExplorationModel(master, filenames[0]);
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
		final MapHelper helper = new MapHelper();
		final IExplorationModel model;
		try {
			model = readMaps(args);
		} catch (IOException except) {
			throw new DriverFailedException("I/O error reading maps", except);
		} catch (XMLStreamException except) {
			throw new DriverFailedException("Malformed XML in map file", except);
		} catch (SPFormatException except) {
			throw new DriverFailedException("SP format error in map file", except);
		}
		final ExplorationCLI cli = new ExplorationCLI(model, helper);
		try {
			final Player player = cli.choosePlayer();
			if (player.getPlayerId() < 0) {
				return; // NOPMD
			}
			final Unit unit = cli.chooseUnit(player);
			if (unit.getID() < 0) {
				return; // NOPMD
			}
			SystemOut.SYS_OUT.println("Details of that unit:");
			SystemOut.SYS_OUT.println(unit.verbose());
			movementREPL(cli, unit, helper.inputNumber("MP that unit has: "));
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
}

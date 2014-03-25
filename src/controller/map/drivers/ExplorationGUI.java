package controller.map.drivers;

import static view.util.SystemOut.SYS_OUT;

import java.io.IOException;

import javax.swing.SwingUtilities;
import javax.xml.stream.XMLStreamException;

import model.exploration.ExplorationModel;
import model.map.IMap;
import model.map.MapView;
import util.Warning;
import view.exploration.ExplorationFrame;
import view.map.main.MapFileFilter;
import view.util.FilteredFileChooser;
import controller.map.drivers.ISPDriver.DriverUsage.ParamCount;
import controller.map.formatexceptions.SPFormatException;
import controller.map.misc.MapReaderAdapter;
import controller.map.misc.MultiIOHandler;
import controller.map.misc.WindowThread;

/**
 * A class to start the exploration GUI.
 *
 * @author Jonathan Lovelace
 */
public class ExplorationGUI implements ISPDriver {
	/**
	 * An object indicating how to use and invoke this driver.
	 */
	private static final DriverUsage USAGE_OBJ = new DriverUsage(true, "-x",
			"--explore", ParamCount.Many, "Run exploration.",
			"Move a unit around the map, "
					+ "updating the player's map with what it sees.",
			ExplorationGUI.class);

	/**
	 * Driver. Takes as its parameters the map files to use.
	 *
	 * @param args the command-line arguments
	 */
	public static void main(final String[] args) {
		try {
			new ExplorationGUI().startDriver(args);
		} catch (final DriverFailedException except) {
			System.err.print(except.getMessage());
			System.err.println(':');
			System.err.println(except.getCause().getLocalizedMessage());
		}
	}

	/**
	 * Read maps.
	 *
	 * @param filenames the files to read from
	 * @return an exploration-model containing all of them
	 * @throws SPFormatException on SP format problems
	 * @throws XMLStreamException on malformed XML
	 * @throws IOException on basic file I/O error
	 */
	private static ExplorationModel readMaps(final String[] filenames)
			throws IOException, XMLStreamException, SPFormatException {
		final MapReaderAdapter reader = new MapReaderAdapter();
		final String firstFile = filenames[0];
		assert firstFile != null;
		final MapView master = reader.readMap(firstFile, Warning.INSTANCE);
		final ExplorationModel model = new ExplorationModel(master,
				firstFile);
		for (final String filename : filenames) {
			if (filename == null || filename.equals(firstFile)) {
				continue;
			}
			final IMap map = reader.readMap(filename, Warning.INSTANCE);
			if (!map.getDimensions().equals(master.getDimensions())) {
				throw new IllegalArgumentException("Size mismatch between "
						+ firstFile + " and " + filename);
			}
			model.addSubordinateMap(map, filename);
		}
		return model;
	}

	/**
	 * Run the driver.
	 *
	 * @param args command-line arguments
	 * @throws DriverFailedException if the driver failed to run
	 */
	@Override
	public void startDriver(final String... args) throws DriverFailedException {
		if (args.length == 0) {
			SYS_OUT.println("Usage: " + getClass().getSimpleName()
					+ " master-map [player-map ...]");
			System.exit(1);
		}
		try {
			final ExplorationModel model = readMaps(args);
			SwingUtilities.invokeLater(new WindowThread(new ExplorationFrame(
					model, new MultiIOHandler(model, new FilteredFileChooser(
							".", new MapFileFilter())))));
		} catch (final IOException except) {
			throw new DriverFailedException("I/O error reading maps", except);
		} catch (final XMLStreamException except) {
			throw new DriverFailedException("Malformed XML in map file", except);
		} catch (final SPFormatException except) {
			throw new DriverFailedException("SP format error in map file",
					except);
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
		return usage().getShortDescription();
	}

	/**
	 * @param nomen ignored
	 */
	@Override
	public void setName(final String nomen) {
		throw new IllegalStateException("Can't rename a driver");
	}
	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "ExplorationGUI";
	}
}

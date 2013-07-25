package controller.map.drivers;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;
import javax.xml.stream.XMLStreamException;

import model.workermgmt.IWorkerModel;
import model.workermgmt.WorkerModel;
import util.Warning;
import view.map.main.MapFileFilter;
import view.map.main.ViewerFrame;
import view.util.ErrorShower;
import view.util.FilteredFileChooser;
import view.util.SystemOut;
import view.worker.WorkerMgmtFrame;
import controller.map.formatexceptions.SPFormatException;
import controller.map.misc.DriverUsage;
import controller.map.misc.DriverUsage.ParamCount;
import controller.map.misc.FileChooser;
import controller.map.misc.FileChooser.ChoiceInterruptedException;
import controller.map.misc.IOHandler;
import controller.map.misc.MapReaderAdapter;
/**
 * A class to start the user worker management GUI.
 * @author Jonathan Lovelace
 *
 */
public class WorkerStart implements ISPDriver {
	/**
	 * An object indicating how to use and invoke this driver.
	 */
	private static final DriverUsage USAGE_OBJ = new DriverUsage(
			true, "-w", "--worker", ParamCount.One,
			"Manage a player's workers in units",
			"Organize the members of a player's units.", WorkerStart.class);
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
	/**
	 * Run the driver.
	 * @param args Command-line arguments.
	 * @throws DriverFailedException if the driver failed to run.
	 *
	 * @see controller.map.drivers.ISPDriver#startDriver(java.lang.String[])
	 */
	@Override
	public void startDriver(final String... args) throws DriverFailedException {
		// ESCA-JAVA0177:
		final String filename; // NOPMD
		try {
			filename = new FileChooser(args.length == 0 ? "" : args[0]).getFilename();
		} catch (ChoiceInterruptedException except) {
			SystemOut.SYS_OUT.println("Choice was interrupted or user declined to choose, aborting ...");
			return;
		}
		final IWorkerModel model; // NOPMD
		try {
			model = new WorkerModel(new MapReaderAdapter().readMap(
					filename, new Warning(Warning.Action.Warn)), filename);
		} catch (final XMLStreamException e) {
			throw new DriverFailedException(XML_ERROR_STRING + ' ' + filename, e);
		} catch (final FileNotFoundException e) {
			throw new DriverFailedException("File " + filename + NOT_FOUND_ERROR, e);
		} catch (final IOException e) {
			throw new DriverFailedException("I/O error reading " + filename, e);
		} catch (final SPFormatException e) {
			throw new DriverFailedException(INV_DATA_ERROR, e);
		}
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				new WorkerMgmtFrame(model,
						new IOHandler(model, new FilteredFileChooser(".",
								new MapFileFilter()))).setVisible(true);
			}
		});
	}
	/**
	 * Run the app.
	 *
	 * @param args Command-line arguments. args[0] is the map filename, others
	 *        are ignored. TODO: add option handling.
	 */
	public static void main(final String[] args) {
		try {
			new WorkerStart().startDriver(args);
		} catch (DriverFailedException except) {
			LOGGER.log(Level.SEVERE, except.getMessage(), except.getCause());
			ErrorShower.showErrorDialog(null, except.getMessage());
		}
	}
	/**
	 * An error message refactored from at least four uses.
	 */
	private static final String XML_ERROR_STRING = "Error reading XML file";
	/**
	 * Logger.
	 */
	private static final Logger LOGGER = Logger.getLogger(ViewerFrame.class
			.getName());
	/**
	 * Error message fragment when file not found.
	 */
	private static final String NOT_FOUND_ERROR = " not found";
	/**
	 * Error message when the map contains invalid data.
	 */
	private static final String INV_DATA_ERROR = "Map contained invalid data";
}

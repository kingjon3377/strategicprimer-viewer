package controller.map.drivers;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.xml.stream.XMLStreamException;

import model.viewer.MapModel;
import util.Warning;
import view.map.main.MapFileFilter;
import view.map.main.SPMenu;
import view.map.main.ViewerFrame;
import view.util.ErrorShower;
import view.util.SystemOut;
import controller.map.formatexceptions.SPFormatException;
import controller.map.misc.FileChooser;
import controller.map.misc.FileChooser.ChoiceInterruptedException;
import controller.map.misc.IOHandler;
import controller.map.misc.MapReaderAdapter;

/**
 * A class to start the viewer, to reduce circular dependencies between
 * packages.
 *
 * @author Jonathan Lovelace
 *
 */
public final class ViewerStart implements ISPDriver {
	/**
	 * Do not instantiate.
	 */
	private ViewerStart() {
		// Do not use.
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

	/**
	 * Run the app.
	 *
	 * @param args Command-line arguments: args[0] is the map filename, others
	 *        are ignored. TODO: Add option handling.
	 *
	 */
	public static void main(final String[] args) {
		try {
			new ViewerStart().startDriver(args);
		} catch (DriverFailedException except) {
			LOGGER.log(Level.SEVERE, except.getMessage(), except.getCause());
			ErrorShower.showErrorDialog(null, except.getMessage());
		}
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
		final MapModel model; // NOPMD
		try {
			model = new MapModel(new MapReaderAdapter().readMap(
					filename, new Warning(Warning.Action.Warn)));
			if (args.length > 1) {
				model.setSecondaryMap(new MapReaderAdapter().readMap(args[1],
						new Warning(Warning.Action.Warn)));
			}
		} catch (final XMLStreamException e) {
			throw new DriverFailedException(XML_ERROR_STRING + ' ' + filename, e);
		} catch (final FileNotFoundException e) {
			throw new DriverFailedException("File " + filename + NOT_FOUND_ERROR, e);
		} catch (final IOException e) {
			throw new DriverFailedException("I/O error reading " + filename, e);
		} catch (final SPFormatException e) {
			throw new DriverFailedException(INV_DATA_ERROR, e);
		}
		final JFileChooser chooser = new JFileChooser(".");
		chooser.setFileFilter(new MapFileFilter());
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				final ViewerFrame frame = new ViewerFrame(model);
				frame.setJMenuBar(new SPMenu(new IOHandler(model, chooser),
						frame, model));
				frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
				frame.setVisible(true);
			}
		});
	}
}

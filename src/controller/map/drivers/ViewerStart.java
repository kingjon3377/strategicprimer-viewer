package controller.map.drivers;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.xml.stream.XMLStreamException;

import model.map.MapView;
import model.viewer.IViewerModel;
import model.viewer.ViewerModel;
import util.TypesafeLogger;
import util.Warning;
import view.map.main.MapFileFilter;
import view.map.main.ViewerFrame;
import view.util.ErrorShower;
import view.util.FilteredFileChooser;
import view.util.SystemOut;
import controller.map.drivers.ISPDriver.DriverUsage.ParamCount;
import controller.map.formatexceptions.SPFormatException;
import controller.map.misc.FileChooser;
import controller.map.misc.FileChooser.ChoiceInterruptedException;
import controller.map.misc.IOHandler;
import controller.map.misc.MapReaderAdapter;
import controller.map.misc.WindowThread;

/**
 * A class to start the viewer, to reduce circular dependencies between
 * packages.
 *
 * @author Jonathan Lovelace
 *
 */
public final class ViewerStart implements ISPDriver {
	/**
	 * An object indicating how to use and invoke this driver.
	 */
	private static final DriverUsage USAGE_OBJ = new DriverUsage(true, "-m",
			"--map", ParamCount.One, "Map viewer",
			"Look at the map visually. This is probably the app you want.",
			ViewerStart.class);

	/**
	 * An error message refactored from at least four uses.
	 */
	private static final String XML_ERROR_STRING = "Error reading XML file";
	/**
	 * Logger.
	 */
	private static final Logger LOGGER = TypesafeLogger
			.getLogger(ViewerFrame.class);
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
		} catch (final DriverFailedException except) {
			LOGGER.log(Level.SEVERE, except.getMessage(), except.getCause());
			ErrorShower.showErrorDialog(null, except.getMessage());
		}
	}

	/**
	 * Run the driver.
	 *
	 * @param args Command-line arguments.
	 * @throws DriverFailedException if the driver failed to run.
	 *
	 * @see controller.map.drivers.ISPDriver#startDriver(java.lang.String[])
	 */
	@Override
	public void startDriver(final String... args) throws DriverFailedException {
		if (args.length == 0) {
			try {
				final String filename = new FileChooser("").getFilename();
				startDriver(filename);
			} catch (final ChoiceInterruptedException except) {
				SystemOut.SYS_OUT
						.println("Choice was interrupted or user declined to choose, aborting ...");
				return;
			}
		} else {
			final MapReaderAdapter reader = new MapReaderAdapter();
			final Warning warner = new Warning(Warning.Action.Warn);
			final FilteredFileChooser chooser = new FilteredFileChooser(".",
					new MapFileFilter());
			for (final String filename : args) {
				try {
					startFrame(reader.readMap(filename, warner), filename,
							chooser);
				} catch (final XMLStreamException e) {
					throw new DriverFailedException(XML_ERROR_STRING + ' '
							+ filename, e);
				} catch (final FileNotFoundException e) {
					throw new DriverFailedException("File " + filename
							+ NOT_FOUND_ERROR, e);
				} catch (final IOException e) {
					throw new DriverFailedException("I/O error reading "
							+ filename, e);
				} catch (final SPFormatException e) {
					throw new DriverFailedException(INV_DATA_ERROR, e);
				}
			}
		}
	}

	/**
	 * Start a viewer frame based on the given map.
	 *
	 * @param map the map object
	 * @param filename the file it was loaded from
	 * @param chooser the file-chooser to pass to the frame
	 */
	private static void startFrame(final MapView map, final String filename,
			final JFileChooser chooser) {
		final IViewerModel model = new ViewerModel(map, filename);
		SwingUtilities.invokeLater(new WindowThread(new ViewerFrame(model,
				new IOHandler(model, chooser))));
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

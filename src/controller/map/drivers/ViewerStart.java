package controller.map.drivers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.xml.stream.XMLStreamException;

import controller.map.drivers.ISPDriver.DriverUsage.ParamCount;
import controller.map.formatexceptions.SPFormatException;
import controller.map.misc.FileChooser;
import controller.map.misc.FileChooser.ChoiceInterruptedException;
import controller.map.misc.IOHandler;
import controller.map.misc.MapReaderAdapter;
import controller.map.misc.WindowThread;
import model.map.IMutableMapNG;
import model.viewer.IViewerModel;
import model.viewer.ViewerModel;
import util.TypesafeLogger;
import util.Warning;
import util.Warning.Action;
import view.map.main.MapFileFilter;
import view.map.main.ViewerFrame;
import view.util.FilteredFileChooser;

/**
 * A class to start the viewer, to reduce circular dependencies between
 * packages.
 *
 * This is part of the Strategic Primer assistive programs suite developed by
 * Jonathan Lovelace.
 *
 * Copyright (C) 2011-2015 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of version 3 of the GNU General Public License as published by the
 * Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
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
	 * Run the driver. TODO: Somehow unify similar code between this and other
	 * similar drivers.
	 *
	 * @param args
	 *            Command-line arguments.
	 * @throws DriverFailedException
	 *             if the driver failed to run.
	 *
	 * @see controller.map.drivers.ISPDriver#startDriver(java.lang.String[])
	 */
	@Override
	public void startDriver(final String... args) throws DriverFailedException {
		if (args.length == 0) {
			try {
				startDriver(new FileChooser(new File("")).getFile().getPath());
			} catch (final ChoiceInterruptedException except) {
				LOGGER.log(
						Level.INFO,
						"Choice was interrupted or user didn't choose; aborting",
						except);
				return;
			}
		} else {
			final MapReaderAdapter reader = new MapReaderAdapter();
			final Warning warner = new Warning(Action.Warn);
			final FilteredFileChooser chooser = new FilteredFileChooser(".",
					new MapFileFilter());
			for (final String filename : args) {
				if (filename == null) {
					continue;
				}
				final File file = new File(filename);
				try {
					startFrame(reader.readMap(file, warner), file, chooser);
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
	 * @param file the file it was loaded from
	 * @param chooser the file-chooser to pass to the frame
	 */
	private static void startFrame(final IMutableMapNG map, final File file,
			final JFileChooser chooser) {
		final IViewerModel model = new ViewerModel(map, file);
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
	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "ViewerStart";
	}
}

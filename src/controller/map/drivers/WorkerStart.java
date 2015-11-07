package controller.map.drivers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;
import javax.xml.stream.XMLStreamException;

import controller.map.drivers.ISPDriver.DriverUsage.ParamCount;
import controller.map.formatexceptions.SPFormatException;
import controller.map.misc.FileChooser;
import controller.map.misc.FileChooser.ChoiceInterruptedException;
import controller.map.misc.IOHandler;
import controller.map.misc.MapReaderAdapter;
import controller.map.misc.WindowThread;
import model.workermgmt.IWorkerModel;
import model.workermgmt.WorkerModel;
import util.TypesafeLogger;
import util.Warning;
import util.Warning.Action;
import view.map.main.MapFileFilter;
import view.map.main.ViewerFrame;
import view.util.FilteredFileChooser;
import view.worker.WorkerMgmtFrame;

/**
 * A class to start the user worker management GUI.
 *
 * This is part of the Strategic Primer assistive programs suite developed by
 * Jonathan Lovelace.
 *
 * Copyright (C) 2013-2015 Jonathan Lovelace
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
 * this program. If not, see <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 *
 */
public class WorkerStart implements ISPDriver {
	/**
	 * An object indicating how to use and invoke this driver.
	 */
	private static final DriverUsage USAGE_OBJ = new DriverUsage(true, "-w",
			"--worker", ParamCount.Many, "Manage a player's workers in units",
			"Organize the members of a player's units.", WorkerStart.class);

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
	 *
	 * @param args Command-line arguments.
	 * @throws DriverFailedException if the driver failed to run.
	 *
	 * @see controller.map.drivers.ISPDriver#startDriver(java.lang.String[])
	 */
	@Override
	public void startDriver(final String... args) throws DriverFailedException {
		final File file; // NOPMD
		try {
			if (args.length == 0) {
				file = new FileChooser(new File("")).getFile();
			} else {
				file = new FileChooser(new File(args[0])).getFile();
			}
		} catch (final ChoiceInterruptedException except) {
			LOGGER.log(
					Level.INFO,
					"Choice was interrupted or user declined to choose; aborting.",
					except);
			return;
		}
		try {
			final MapReaderAdapter reader = new MapReaderAdapter();
			final Warning warner = new Warning(Action.Warn);
			final IWorkerModel model =
					new WorkerModel(reader.readMap(file,
							warner), file);
			for (String arg : args) {
				if (arg != null && !arg.equals(args[0])) {
					final File newFile = new File(arg);
					model.addSubordinateMap(reader.readMap(newFile, warner),
							newFile);
				}
			}
			SwingUtilities.invokeLater(new WindowThread(new WorkerMgmtFrame(
					model, new IOHandler(model, new FilteredFileChooser(".",
							new MapFileFilter())))));
		} catch (final XMLStreamException e) {
			throw new DriverFailedException(XML_ERROR_STRING + ' '
					+ file.getPath(), e);
		} catch (final FileNotFoundException e) {
			throw new DriverFailedException("File " + file.getPath()
					+ NOT_FOUND_ERROR, e);
		} catch (final IOException e) {
			throw new DriverFailedException("I/O error reading "
					+ file.getPath(), e);
		} catch (final SPFormatException e) {
			throw new DriverFailedException(INV_DATA_ERROR, e);
		}
	}
	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "WorkerStart";
	}
}

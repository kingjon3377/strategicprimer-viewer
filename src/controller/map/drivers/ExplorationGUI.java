package controller.map.drivers;

import static view.util.SystemOut.SYS_OUT;

import java.io.File;
import java.io.IOException;

import javax.swing.SwingUtilities;
import javax.xml.stream.XMLStreamException;

import controller.map.drivers.ISPDriver.DriverUsage.ParamCount;
import controller.map.formatexceptions.SPFormatException;
import controller.map.misc.IOHandler;
import controller.map.misc.MapReaderAdapter;
import controller.map.misc.WindowThread;
import model.exploration.ExplorationModel;
import model.map.IMutableMapNG;
import model.misc.IDriverModel;
import util.Warning;
import view.exploration.ExplorationFrame;
import view.map.main.MapFileFilter;
import view.util.FilteredFileChooser;

/**
 * A class to start the exploration GUI.
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
	 * Read maps.
	 *
	 * @param filenames the files to read from
	 * @return an exploration-model containing all of them
	 * @throws SPFormatException on SP format problems
	 * @throws XMLStreamException on malformed XML
	 * @throws IOException on basic file I/O error
	 * TODO: Should probably take Files rather than names.
	 */
	private static ExplorationModel readMaps(final String[] filenames)
			throws IOException, XMLStreamException, SPFormatException {
		final MapReaderAdapter reader = new MapReaderAdapter();
		final File firstFile = new File(filenames[0]);
		final IMutableMapNG master =
				reader.readMap(firstFile, Warning.INSTANCE);
		final ExplorationModel model = new ExplorationModel(master,
				firstFile);
		for (final String filename : filenames) {
			if (filename == null || filename.equals(filenames[0])) {
				continue;
			}
			final File file = new File(filename);
			final IMutableMapNG map = reader.readMap(file, Warning.INSTANCE);
			if (!map.dimensions().equals(master.dimensions())) {
				throw new IllegalArgumentException("Size mismatch between "
						+ firstFile + " and " + filename);
			}
			model.addSubordinateMap(map, file);
		}
		return model;
	}
	/**
	 * Run the driver.
	 * @param dmodel the driver model
	 * @throws DriverFailedException on error
	 */
	@Override
	public void startDriver(final IDriverModel dmodel) throws DriverFailedException {
		ExplorationModel model;
		if (dmodel instanceof ExplorationModel) {
			model = (ExplorationModel) dmodel;
		} else {
			model = new ExplorationModel(dmodel);
		}
		SwingUtilities.invokeLater(
				new WindowThread(new ExplorationFrame(model, new IOHandler(model,
						new FilteredFileChooser(".", new MapFileFilter())))));
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
			SYS_OUT.print("Usage: ");
			SYS_OUT.print(getClass().getSimpleName());
			SYS_OUT.println(" master-map [player-map ...]");
			System.exit(1);
		}
		try {
			final ExplorationModel model = readMaps(args);
			SwingUtilities.invokeLater(new WindowThread(new ExplorationFrame(
					model, new IOHandler(model, new FilteredFileChooser(
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

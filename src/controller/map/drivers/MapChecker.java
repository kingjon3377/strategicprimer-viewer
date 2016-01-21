package controller.map.drivers;

import controller.map.drivers.DriverUsage.ParamCount;
import controller.map.formatexceptions.MapVersionException;
import controller.map.formatexceptions.SPFormatException;
import controller.map.misc.MapReaderAdapter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import javax.xml.stream.XMLStreamException;
import util.TypesafeLogger;
import util.Warning;

import static view.util.SystemOut.SYS_OUT;

/**
 * A driver to check every map file in a list for errors.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2011-2014 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program. If not, see
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public final class MapChecker implements UtilityDriver {
	/**
	 * An object indicating how to use and invoke this driver.
	 */
	private static final DriverUsage USAGE =
			new DriverUsage(false, "-k", "--check", ParamCount.One,
					               "Check map for errors",
					               "Check a map file for errors, deprecated syntax, etc.",
					               MapChecker.class);

	/**
	 * Logger.
	 */
	private static final Logger LOGGER = TypesafeLogger
			                                     .getLogger(MapChecker.class);
	/**
	 * The map reader we'll use.
	 */
	private final MapReaderAdapter reader = new MapReaderAdapter();

	/**
	 * Run the driver.
	 *
	 * @param args command-line arguments
	 * @throws DriverFailedException if not enough arguments
	 */
	@Override
	public void startDriver(final String... args) throws DriverFailedException {
		if (args.length < 1) {
			throw new DriverFailedException("Need at least one argument",
					                               new IllegalArgumentException("Need at least one argument"));
		}
		Stream.of(args).map(arg -> new File(arg)).forEach(this::check);
	}

	/**
	 * Check a map.
	 *
	 * @param file the file to check
	 */
	private void check(final File file) {
		SYS_OUT.print("Starting ");
		SYS_OUT.println(file.getPath());
		boolean retval = true;
		try {
			reader.readMap(file, Warning.DEFAULT);
		} catch (final MapVersionException e) {
			LOGGER.log(Level.SEVERE, "Map version in " + file.getPath()
					                         + " not acceptable to reader", e);
			retval = false;
		} catch (final FileNotFoundException e) {
			LOGGER.log(Level.SEVERE, file.getPath() + " not found", e);
			retval = false;
		} catch (final IOException e) {
			LOGGER.log(Level.SEVERE, "I/O error reading " + file.getPath(), e);
			retval = false;
		} catch (final XMLStreamException e) {
			LOGGER.log(Level.SEVERE,
					"XML stream error reading " + file.getPath(), e);
			retval = false;
		} catch (final SPFormatException e) {
			LOGGER.log(Level.SEVERE,
					"SP map format error reading " + file.getPath(), e);
			retval = false;
		}
		if (retval) {
			SYS_OUT.print("No errors in ");
			SYS_OUT.println(file.getPath());
		}
	}

	/**
	 * @return an object indicating how to use and invoke this driver.
	 */
	@Override
	public DriverUsage usage() {
		return USAGE;
	}

	/**
	 * @return a String representation of the object
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "MapChecker";
	}
}

package controller.map.drivers;

import controller.map.converter.ResolutionDecreaseConverter;
import controller.map.drivers.DriverUsage.ParamCount;
import controller.map.formatexceptions.MapVersionException;
import controller.map.formatexceptions.SPFormatException;
import controller.map.misc.MapReaderAdapter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.stream.XMLStreamException;
import model.map.IMapNG;
import util.TypesafeLogger;
import util.Warning;

import static view.util.SystemOut.SYS_OUT;

/**
 * A driver to convert maps to the new format.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2011-2015 Jonathan Lovelace
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
public final class ConverterDriver implements UtilityDriver {
	/**
	 * An object indicating how to use and invoke this driver.
	 */
	private static final DriverUsage USAGE =
			new DriverUsage(false, "-v", "--convert", ParamCount.One,
					               "Convert a map's format",
					               "Convert a map. At present, this means reducing its resolution.",
					               ConverterDriver.class);

	/**
	 * Logger.
	 */
	private static final Logger LOGGER = TypesafeLogger
			                                     .getLogger(ConverterDriver.class);
	/**
	 * The map reader we'll use.
	 */
	private static final MapReaderAdapter READER = new MapReaderAdapter();

	/**
	 * Run the driver.
	 *
	 * @param args command-line argument
	 * @throws DriverFailedException on fatal error
	 */
	@Override
	public void startDriver(final String... args) throws DriverFailedException {
		if (args.length < 1) {
			SYS_OUT.println("Usage: ConverterDriver filename [filename ...]");
			throw new DriverFailedException("Need files to convert",
					                               new IllegalArgumentException("Not enough arguments"));
		}
		for (final String filename : args) {
			if (filename == null) {
				continue;
			}
			SYS_OUT.print("Reading ");
			SYS_OUT.print(filename);
			SYS_OUT.print(" ... ");
			final File file = new File(filename);
			try {
				final IMapNG old = READER.readMap(file, Warning.INSTANCE);
				SYS_OUT.println(" ... Converting ... ");
				final String newFilename = filename + ".new";
				final File newFile = new File(newFilename);
				final IMapNG map = ResolutionDecreaseConverter.convert(old);
				SYS_OUT.print("About to write ");
				SYS_OUT.println(newFilename);
				new MapReaderAdapter().write(newFile, map); //NOPMD
			} catch (final MapVersionException e) {
				LOGGER.log(Level.SEVERE, "Map version in " + filename
						                         + " not acceptable to reader", e);
			} catch (final FileNotFoundException e) {
				LOGGER.log(Level.SEVERE, filename + " not found", e);
			} catch (final IOException e) {
				LOGGER.log(Level.SEVERE, "I/O error processing " + filename, e);
			} catch (final XMLStreamException e) {
				LOGGER.log(Level.SEVERE,
						"XML stream error reading " + filename, e);
			} catch (final SPFormatException e) {
				LOGGER.log(Level.SEVERE, "SP map format error reading "
						                         + filename, e);
			}
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
		return "ConverterDriver";
	}
}

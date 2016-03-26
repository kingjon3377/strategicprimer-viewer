package controller.map.drivers;

import controller.map.drivers.DriverUsage.ParamCount;
import controller.map.formatexceptions.MapVersionException;
import controller.map.formatexceptions.SPFormatException;
import controller.map.misc.MapReaderAdapter;
import java.io.File;
import java.io.IOException;
import javax.xml.stream.XMLStreamException;
import model.map.IMapNG;
import util.Warning;

/**
 * A driver that reads in maps and then writes them out again---this is primarily to make
 * sure that the map format is properly read, but is also useful for correcting deprecated
 * syntax. (Because of that usage, warnings are disabled.)
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2012-2015 Jonathan Lovelace
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
public final class EchoDriver implements UtilityDriver {
	/**
	 * An object indicating how to use and invoke this driver.
	 */
	private static final DriverUsage USAGE =
			new DriverUsage(false, "-e", "--echo", ParamCount.Two,
					               "Read, then write a map.",
					               "Read and write a map, correcting deprecated syntax.",
					               EchoDriver.class);

	/**
	 * Run the driver.
	 *
	 * @param args command-line arguments
	 * @throws DriverFailedException on error
	 */
	@SuppressWarnings("OverloadedVarargsMethod")
	@Override
	public void startDriver(final String... args) throws DriverFailedException {
		if (args.length != 2) {
			throw new IncorrectUsageException(usage());
		}
		final IMapNG map; // NOPMD
		final File infile = new File(args[0]);
		try {
			map =
					new MapReaderAdapter().readMap(infile, Warning.Ignore);
		} catch (final MapVersionException except) {
			throw new DriverFailedException("Unsupported map version", except);
		} catch (final IOException except) {
			throw new DriverFailedException(
					                               "I/O error reading file " +
							                               infile.getPath(), except);
		} catch (final XMLStreamException except) {
			throw new DriverFailedException("Malformed XML", except);
		} catch (final SPFormatException except) {
			throw new DriverFailedException("SP map format error", except);
		}
		final File outfile = new File(args[1]);
		try {
			new MapReaderAdapter().write(outfile, map);
		} catch (final IOException except) {
			throw new DriverFailedException("I/O error writing " + outfile,
					                               except);
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
		return "EchoDriver";
	}
}

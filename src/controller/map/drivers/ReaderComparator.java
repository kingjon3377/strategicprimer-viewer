package controller.map.drivers;

import controller.map.cxml.CompactXMLReader;
import controller.map.drivers.DriverUsage.ParamCount;
import controller.map.formatexceptions.MapVersionException;
import controller.map.formatexceptions.SPFormatException;
import controller.map.iointerfaces.IMapReader;
import controller.map.readerng.MapReaderNG;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.CharBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.stream.XMLStreamException;
import model.map.IMapNG;
import model.misc.IDriverModel;
import util.NullCleaner;
import util.TypesafeLogger;
import util.Warning;
import util.Warning.Action;

import static view.util.SystemOut.SYS_OUT;

/**
 * A driver for comparing map readers.
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
@SuppressWarnings("deprecation")
public final class ReaderComparator implements ISPDriver {
	/**
	 * An object indicating how to use and invoke this driver.
	 */
	private static final DriverUsage USAGE =
			new DriverUsage(false, "-t", "--test", ParamCount.One, "Test map readers",
								   "Test the two map-reading implementations by " +
										   "comparing their results on the same file.",
								   ReaderComparator.class);

	/**
	 * Logger.
	 */
	private static final Logger LOGGER = TypesafeLogger
												 .getLogger(ReaderComparator.class);
	/**
	 * The first reader.
	 */
	private final IMapReader oldReader = new MapReaderNG();
	/**
	 * The second reader.
	 */
	private final IMapReader newReader = new CompactXMLReader();

	/**
	 * Compare the two readers.
	 *
	 * @param args The list of specified files to compare them on
	 */
	public void compareReaders(final String... args) {
		for (final String arg : args) {
			if (arg == null) {
				continue;
			}
			try {
				compareReaders(new File(arg));
			} catch (final XMLStreamException e) {
				LOGGER.log(Level.SEVERE,
						"XMLStreamException (probably badly formed input) in "
								+ arg, e);
			} catch (final MapVersionException e) {
				LOGGER.log(Level.SEVERE,
						"Map version too old for old-style reader in file "
								+ arg, e);
			} catch (final SPFormatException e) {
				LOGGER.log(Level.SEVERE,
						"New reader claims invalid SP map data in " + arg, e);
			}
		}
	}

	/**
	 * Compare the two readers on a file.
	 *
	 * @param arg the file to have each read.
	 * @throws XMLStreamException if either reader claims badly formed input
	 * @throws SPFormatException  if either reader claims invalid data
	 */
	public void compareReaders(final File arg) throws XMLStreamException,
															  SPFormatException {
		SYS_OUT.print(arg);
		SYS_OUT.println(':');
		final String contents;
		try {
			contents = readIntoBuffer(arg);
		} catch (final FileNotFoundException except) {
			LOGGER.log(Level.SEVERE, "File " + arg + " not found", except);
			return; // NOPMD
		} catch (final IOException except) {
			LOGGER.log(Level.SEVERE, "I/O error reading file " + arg, except);
			return;
		}
		final Warning warner = new Warning(Action.Ignore);
		final long startOne = System.nanoTime();
		final IMapNG map1;
		try (StringReader reader = new StringReader(contents)) {
			map1 = oldReader.readMap(arg, reader, warner);
		}
		final long endOne = System.nanoTime();
		printElapsed("Old", endOne - startOne);
		final long startTwo = System.nanoTime();
		final IMapNG map2;
		try (StringReader reader = new StringReader(contents)) {
			map2 = newReader.readMap(arg, reader, warner);
		}
		final long endTwo = System.nanoTime();
		printElapsed("New", endTwo - startTwo);
		if (map1.equals(map2)) {
			SYS_OUT.println("Readers produce identical results.");
		} else {
			SYS_OUT.print("Readers differ on ");
			SYS_OUT.println(arg);
		}
	}

	/**
	 * Print a description of a method's elapsed time.
	 *
	 * @param desc a description of the method ("old" or "new")
	 * @param time how many time-units it took
	 */
	private static void printElapsed(final String desc, final long time) {
		SYS_OUT.print(desc);
		SYS_OUT.print(" method took ");
		SYS_OUT.print(time);
		SYS_OUT.println(" time-units.");
	}

	/**
	 * @param file a file
	 * @return a string containing its contents, so reading from it won't be
	 * confounded by
	 * disk I/O.
	 * @throws IOException if file not found, or on other I/O error reading from file
	 */
	private static String readIntoBuffer(final File file)
			throws IOException {
		try (final FileReader reader = new FileReader(file)) {
			final CharBuffer buffer = CharBuffer.allocate((int) file.length());
			reader.read(buffer);
			buffer.position(0);
			return NullCleaner.assertNotNull(buffer.toString());
		}
	}

	/**
	 * @return a String representation of this object
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "ReaderComparator";
	}

	/**
	 * @param model ignored
	 * @throws DriverFailedException always; this is meaningless on a driver-model.
	 */
	@Override
	public void startDriver(final IDriverModel model) throws DriverFailedException {
		throw new DriverFailedException(new IllegalStateException("ReaderComparator is " +
																		  "meaningless " +
																		  "for a driver " +
																		  "model"));
	}

	/**
	 * Run the driver, comparing the readers' performance.
	 *
	 * @param args The files to test on
	 */
	@Override
	public void startDriver(final String... args) {
		compareReaders(args);
	}

	/**
	 * @return an object indicating how to use and invoke this driver.
	 */
	@Override
	public DriverUsage usage() {
		return USAGE;
	}
}

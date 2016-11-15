package controller.map.drivers;

import controller.map.cxml.CompactXMLReader;
import controller.map.fluidxml.SPFluidReader;
import controller.map.formatexceptions.MapVersionException;
import controller.map.formatexceptions.SPFormatException;
import controller.map.iointerfaces.IMapReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import javax.xml.stream.XMLStreamException;
import model.map.IMapNG;
import util.TypesafeLogger;
import util.Warning;

import static view.util.SystemOut.SYS_OUT;

/**
 * A driver for comparing map readers.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2011-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
@SuppressWarnings("deprecation")
public final class ReaderComparator implements UtilityDriver {
	/**
	 * An object indicating how to use and invoke this driver.
	 */
	private static final DriverUsage USAGE =
			new DriverUsage(false, "-t", "--test", ParamCount.AtLeastOne,
								"Test map readers",
								"Test the two map-reading implementations by " +
										"comparing their results on the same file."
			);

	/**
	 * Logger.
	 */
	private static final Logger LOGGER = TypesafeLogger.getLogger(ReaderComparator
																		.class);
	/**
	 * The first reader.
	 */
	private final IMapReader oldReader = new CompactXMLReader();
	/**
	 * The second reader.
	 */
	private final IMapReader newReader = new SPFluidReader();

	/**
	 * Compare the two readers.
	 *
	 * @param args The list of specified files to compare them on
	 */
	@SuppressWarnings("OverloadedVarargsMethod")
	public void compareReaders(final String... args) {
		Stream.of(args).map(Paths::get).forEach(this::compareReaders);
	}
	/**
	 * Handle (log appropriately) an exception.
	 * @param except the exception to handle
	 * @param file the name of the file being read
	 */
	private static void handleException(final Exception except, final Path file) {
		if (except instanceof XMLStreamException) {
			LOGGER.log(Level.SEVERE,
					"XMLStreamException (probably badly formed input) in " + file,
					except);
		} else if (except instanceof MapVersionException) {
			LOGGER.log(Level.SEVERE,
					"Map version too old for old-style reader in file " + file, except);
		} else if (except instanceof SPFormatException) {
			LOGGER.log(Level.SEVERE, "New reader claims invalid SP map data in " + file,
					except);
		} else {
			LOGGER.log(Level.SEVERE, "Unhandled exception type", except);
		}
	}
	/**
	 * Compare the two readers on a file.
	 *
	 * @param arg the file to have each read.
	 */
	public void compareReaders(final Path arg) {
		SYS_OUT.print(arg);
		SYS_OUT.println(':');
		final String contents;
		try {
			contents = readIntoBuffer(arg);
		} catch (final FileNotFoundException|NoSuchFileException except) {
			LOGGER.log(Level.SEVERE, "File " + arg + " not found", except);
			return;
		} catch (final IOException except) {
			//noinspection HardcodedFileSeparator
			LOGGER.log(Level.SEVERE, "I/O error reading file " + arg, except);
			return;
		}
		final long startOne = System.nanoTime();
		final IMapNG map1;
		try (StringReader reader = new StringReader(contents)) {
			map1 = oldReader.readMap(arg, reader, Warning.Ignore);
		} catch (XMLStreamException | SPFormatException except) {
			handleException(except, arg);
			return;
		}
		final long endOne = System.nanoTime();
		printElapsed("Old", endOne - startOne);
		final long startTwo = System.nanoTime();
		final IMapNG map2;
		try (StringReader reader = new StringReader(contents)) {
			map2 = newReader.readMap(arg, reader, Warning.Ignore);
		} catch (XMLStreamException | SPFormatException except) {
			handleException(except, arg);
			return;
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
	private static String readIntoBuffer(final Path file)
			throws IOException {
		return new String(Files.readAllBytes(file));
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
	 * Run the driver, comparing the readers' performance.
	 *
	 * @param options
	 * @param args The files to test on
	 */
	@SuppressWarnings("OverloadedVarargsMethod")
	@Override
	public void startDriver(final SPOptions options, final String... args) {
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

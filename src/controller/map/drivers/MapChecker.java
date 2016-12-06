package controller.map.drivers;

import controller.map.formatexceptions.MapVersionException;
import controller.map.formatexceptions.SPFormatException;
import controller.map.misc.ICLIHelper;
import controller.map.misc.MapReaderAdapter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
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
 * Copyright (C) 2011-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public final class MapChecker implements UtilityDriver {
	/**
	 * An object indicating how to use and invoke this driver.
	 */
	private static final DriverUsage USAGE =
			new DriverUsage(false, "-k", "--check", ParamCount.AtLeastOne,
								   "Check map for errors",
								   "Check a map file for errors, deprecated syntax, etc."
			);

	/**
	 * Logger.
	 */
	private static final Logger LOGGER = TypesafeLogger.getLogger(MapChecker.class);
	/**
	 * The map reader we'll use.
	 */
	private final MapReaderAdapter reader = new MapReaderAdapter();

	/**
	 * Run the driver.
	 *
	 * @param cli the interface for user I/O
	 * @param options command-line options passed in
	 * @param args    command-line arguments
	 * @throws DriverFailedException if not enough arguments
	 */
	@SuppressWarnings("OverloadedVarargsMethod")
	@Override
	public void startDriver(final ICLIHelper cli, final SPOptions options,
							final String... args)
			throws DriverFailedException {
		if (args.length < 1) {
			throw new IncorrectUsageException(usage());
		}
		Stream.of(args).map(Paths::get).forEach(this::check);
	}

	/**
	 * Check a map.
	 *
	 * @param file the file to check
	 */
	@SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
	private void check(final Path file) {
		SYS_OUT.print("Starting ");
		SYS_OUT.println(file);
		boolean retval = true;
		try {
			reader.readMap(file, Warning.DEFAULT);
		} catch (final MapVersionException e) {
			LOGGER.log(Level.SEVERE,
					"Map version in " + file + " not acceptable to reader", e);
			retval = false;
		} catch (final FileNotFoundException | NoSuchFileException e) {
			LOGGER.log(Level.SEVERE, file + " not found", e);
			retval = false;
		} catch (final IOException e) {
			//noinspection HardcodedFileSeparator
			LOGGER.log(Level.SEVERE, "I/O error reading " + file, e);
			retval = false;
		} catch (final XMLStreamException e) {
			LOGGER.log(Level.SEVERE,
					"XML stream error reading " + file, e);
			retval = false;
		} catch (final SPFormatException e) {
			LOGGER.log(Level.SEVERE,
					"SP map format error reading " + file, e);
			retval = false;
		}
		if (retval) {
			SYS_OUT.print("No errors in ");
			SYS_OUT.println(file);
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

package controller.map.drivers;

import controller.map.converter.ResolutionDecreaseConverter;
import controller.map.formatexceptions.MapVersionException;
import controller.map.formatexceptions.SPFormatException;
import controller.map.misc.ICLIHelper;
import controller.map.misc.MapReaderAdapter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.stream.XMLStreamException;
import model.map.IMapNG;
import model.map.IMutableMapNG;
import util.TypesafeLogger;
import util.Warning;

import static view.util.SystemOut.SYS_OUT;

/**
 * A driver to convert maps to the new format.
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
public final class ConverterDriver implements UtilityDriver {
	/**
	 * Default constructor, for when this is run at a command line. Sets output to
	 * stdout.
	 */
	public ConverterDriver() {
		this(SYS_OUT, false);
	}
	/**
	 * @param outputStream the stream to write progress information to
	 */
	public ConverterDriver(final PrintStream outputStream) {
		this(outputStream, true);
	}
	/**
	 * @param outputStream the stream to write progress information to
	 * @param gui whether it is (presumed to be) connected to a GUI window rather than
	 *               stdout
	 */
	private ConverterDriver(final PrintStream outputStream, final boolean gui) {
		ostream = outputStream;
		usageObject = new DriverUsage(gui, "-v", "--convert", ParamCount.One,
											 "Convert a map's format",
											 "Convert a map. At present, this means reducing its resolution."

		);
		usageObject.addSupportedOption("--current-turn=NN");
	}
	/**
	 * The stream to write progress information to.
	 */
	private final PrintStream ostream;
	/**
	 * The usage object. This is only an instance rather than static object so we can
	 * have it be CLI by default and GUI when a stream was passed in.
	 */
	private final DriverUsage usageObject;

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
	 *
	 *
	 * @param cli
	 * @param options
	 * @param args command-line argument
	 * @throws DriverFailedException on fatal error
	 */
	@SuppressWarnings({"OverloadedVarargsMethod", "resource"})
	@Override
	public void startDriver(final ICLIHelper cli, final SPOptions options,
							final String... args)
			throws DriverFailedException {
		if (args.length < 1) {
			throw new IncorrectUsageException(usage());
		}
		for (final String filename : args) {
			if (filename == null) {
				continue;
			}
			ostream.printf("Reading %s ... ", filename);
			try {
				//noinspection ObjectAllocationInLoop
				final IMutableMapNG old = READER.readMap(Paths.get(filename), Warning.DEFAULT);
				if (options.hasOption("--current-turn")) {
					final int currentTurn =
							Integer.parseInt(options.getArgument("--current-turn"));
					old.setCurrentTurn(currentTurn);
				}
				ostream.println(" ... Converting ... ");
				final IMapNG map = ResolutionDecreaseConverter.convert(old);
				ostream.print("About to write ");
				final String newFilename = filename + ".new";
				ostream.println(newFilename);
				//noinspection ObjectAllocationInLoop
				READER.write(Paths.get(newFilename), map);
			} catch (final MapVersionException e) {
				LOGGER.log(Level.SEVERE, "Map version in " + filename
												+ " not acceptable to reader", e);
			} catch (final FileNotFoundException|NoSuchFileException e) {
				LOGGER.log(Level.SEVERE, filename + " not found", e);
			} catch (final IOException e) {
				//noinspection HardcodedFileSeparator
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
		return usageObject;
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

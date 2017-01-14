package controller.map.drivers;

import controller.map.misc.ICLIHelper;
import controller.map.report.tabular.TableReportGenerator;
import java.io.IOError;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Function;
import java.util.logging.Logger;
import model.map.IMutableMapNG;
import model.misc.IDriverModel;
import model.misc.IMultiMapModel;
import util.Pair;
import util.TypesafeLogger;

/**
 * A driver to produce tabular (CSV) reports of the contents of a player's map.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public class TabularReportDriver implements SimpleDriver {
	/**
	 * An object indicating how to use and invoke this driver.
	 */
	private static final IDriverUsage USAGE =
			new DriverUsage(false, "-b", "--tabular", ParamCount.AtLeastOne,
								   "Tabular Report Generator",
								   "Produce CSV reports of contents of a map.");
	/**
	 * Logger.
	 */
	private static final Logger LOGGER =
			TypesafeLogger.getLogger(TabularReportDriver.class);

	/**
	 * Produce a function for producing a stream for writing to a sibling file to the
	 * given file.
	 * @param base the base file
	 * @return a function for producing a stream for writing to a sibling file
	 */
	private static Function<String, OutputStream> getFilenameFunction(final Path base) {
		return s -> {
			try {
				return Files.newOutputStream(
						base.resolveSibling(base.getFileName() + "." + s + ".csv"));
			} catch (final IOException except) {
				throw new IOError(except);
			}
		};
	}
	@SuppressWarnings("ErrorNotRethrown")
	@Override
	public void startDriver(final ICLIHelper cli, final SPOptions options,
							final IDriverModel model)
			throws DriverFailedException {
		if (model instanceof IMultiMapModel) {
			for (final Pair<IMutableMapNG, Optional<Path>> pair :
					((IMultiMapModel) model).getAllMaps()) {
				final Optional<Path> mapFileOpt = pair.second();
				if (mapFileOpt.isPresent()) {
					final Path mapFile = mapFileOpt.get();
					try {
						TableReportGenerator.createReports(pair.first(),
								getFilenameFunction(mapFile));
					} catch (IOException | IOError e) {
						throw new DriverFailedException(e);
					}
				} else {
					LOGGER.severe("Asked to create reports from map with no filename");
				}
			}
		} else {
			final Optional<Path> mapFileOpt = model.getMapFile();
			if (mapFileOpt.isPresent()) {
				final Path mapFile = mapFileOpt.get();
				try {
					TableReportGenerator
							.createReports(model.getMap(), getFilenameFunction(mapFile));
				} catch (IOException | IOError e) {
					throw new DriverFailedException(e);
				}
			} else {
				LOGGER.severe("Asked to create reports from map with no filename");
			}
		}
	}

	/**
	 * The usage object.
	 * @return an object indicating how to use and invoke this driver
	 */
	@Override
	public IDriverUsage usage() {
		return USAGE;
	}

	/**
	 * A trivial toString().
	 * @return a String representation of the object
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "TabularReportDriver";
	}
}

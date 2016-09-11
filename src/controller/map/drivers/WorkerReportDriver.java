package controller.map.drivers;

import controller.map.report.ReportGenerator;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.logging.Logger;
import model.map.IMutableMapNG;
import model.misc.IDriverModel;
import model.misc.IMultiMapModel;
import util.Pair;
import util.TypesafeLogger;

/**
 * A driver to produce a report of the units in a map.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2013-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public final class WorkerReportDriver implements SimpleDriver {
	/**
	 * An object indicating how to use and invoke this driver.
	 */
	private static final DriverUsage USAGE =
			new DriverUsage(false, "-m", "--map", ParamCount.One,
								"Report Generator",
								"Produce HTML report of the contents of a map",
								WorkerReportDriver.class);
	/**
	 * Logger.
	 */
	private static final Logger LOGGER =
			TypesafeLogger.getLogger(WorkerReportDriver.class);

	/**
	 * Run the driver.
	 *
	 * TODO: Allow user to specify output filename
	 *
	 * @param model ignored
	 * @throws DriverFailedException always: this driver has to write to the filesystem
	 */
	@Override
	public void startDriver(final IDriverModel model) throws DriverFailedException {
		if (model instanceof IMultiMapModel) {
			for (final Pair<IMutableMapNG, Optional<Path>> pair :
					((IMultiMapModel) model).getAllMaps()) {
				final Optional<Path> mapFile = pair.second();
				if (mapFile.isPresent()) {
					final String report = ReportGenerator.createReport(pair.first());
					final Path mapPath = mapFile.get();
					try (final BufferedWriter writer = Files.newBufferedWriter(
							mapPath.resolveSibling(
									mapPath.getFileName() + ".report.html"))) {
						writer.write(report);
					} catch (final IOException except) {
						//noinspection HardcodedFileSeparator
						throw new DriverFailedException("I/O error writing report",
															   except);
					}
				} else {
					LOGGER.severe("Asked to make report from map with no filename");
				}
			}
		} else {
			final Optional<Path> mapFile = model.getMapFile();
			if (mapFile.isPresent()) {
				final String report = ReportGenerator.createReport(model.getMap());
				final Path mapPath = mapFile.get();
				try (final BufferedWriter writer = Files.newBufferedWriter(
						mapPath.resolveSibling(mapPath.getFileName() + ".report.html"))) {
					writer.write(report);
				} catch (final IOException except) {
					//noinspection HardcodedFileSeparator
					throw new DriverFailedException("I/O error writing report", except);
				}
			} else {
				LOGGER.severe("Asked to create report from map with no filename");
			}
		}
	}
	/**
	 * @return an object indicating how to use and invoke this driver
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
		return "WorkerReportDriver";
	}
}

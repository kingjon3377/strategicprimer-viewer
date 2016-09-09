package controller.map.drivers;

import controller.map.report.tabular.TableReportGenerator;
import java.io.IOError;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import model.map.IMutableMapNG;
import model.misc.IDriverModel;
import model.misc.IMultiMapModel;
import util.Pair;

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
	private static final DriverUsage USAGE =
			new DriverUsage(false, "-b", "--tabular", ParamCount.AtLeastOne,
								   "Tabular Report Generator",
								   "Produce CSV reports of contents of a map.",
								   TabularReportDriver.class);

	@SuppressWarnings("ErrorNotRethrown")
	@Override
	public void startDriver(final IDriverModel model) throws DriverFailedException {
		if (model instanceof IMultiMapModel) {
			for (final Pair<IMutableMapNG, Optional<Path>> pair :
					((IMultiMapModel) model).getAllMaps()) {
				try {
					TableReportGenerator.createReports(pair.first(), s -> {
						try {
							final Path mapFile = pair.second().get();
							return Files.newOutputStream(mapFile.resolveSibling(
									mapFile.getFileName() + "." + s + ".csv"));
						} catch (final IOException e) {
							throw new IOError(e);
						}
					});
				} catch (IOException|IOError e) {
					throw new DriverFailedException(e);
				}
			}
		} else {
			try {
				TableReportGenerator.createReports(model.getMap(), s -> {
					try {
						Path mapFile = model.getMapFile().get();
						return Files.newOutputStream(mapFile.resolveSibling(
								mapFile.getFileName() + "." + s + ".csv"));
					} catch (final IOException e) {
						throw new IOError(e);
					}
				});
			} catch (IOException|IOError e) {
				throw new DriverFailedException(e);
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
		return "TabularReportDriver";
	}
}

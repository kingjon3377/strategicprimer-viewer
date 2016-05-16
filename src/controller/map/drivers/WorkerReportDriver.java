package controller.map.drivers;

import controller.map.drivers.DriverUsage.ParamCount;
import controller.map.report.ReportGenerator;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import model.map.IMutableMapNG;
import model.misc.IDriverModel;
import model.misc.IMultiMapModel;
import util.Pair;

/**
 * A driver to produce a report of the units in a map.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2013-2015 Jonathan Lovelace
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
public final class WorkerReportDriver implements SimpleDriver {
	/**
	 * An object indicating how to use and invoke this driver.
	 */
	private static final DriverUsage USAGE =
			new DriverUsage(false, "-w", "--worker", ParamCount.One,
								"Worker Report Generator",
								"Produce HTML report of units, workers, etc., in a map.",
								WorkerReportDriver.class);

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
			for (final Pair<IMutableMapNG, File> pair : ((IMultiMapModel) model).getAllMaps()) {
				final String report = ReportGenerator.createReport(pair.first());
				try (final FileWriter writer = new FileWriter(pair.second().getPath() +
																	  ".report.html")) {
					writer.write(report);
				} catch (final IOException except) {
					throw new DriverFailedException("I/O error writing report", except);
				}
			}
		} else {
			final String report = ReportGenerator.createReport(model.getMap());
			try (final FileWriter writer = new FileWriter(model.getMapFile().getPath() +
																  ".report.html")) {
				writer.write(report);
			} catch (final IOException except) {
				throw new DriverFailedException("I/O error writing report", except);
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

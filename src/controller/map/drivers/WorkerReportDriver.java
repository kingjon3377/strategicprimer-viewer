package controller.map.drivers;

import controller.map.drivers.DriverUsage.ParamCount;
import controller.map.formatexceptions.MapVersionException;
import controller.map.formatexceptions.SPFormatException;
import controller.map.misc.MapReaderAdapter;
import controller.map.report.ReportGenerator;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import javax.xml.stream.XMLStreamException;
import model.misc.IDriverModel;
import util.Warning;
import util.Warning.Action;

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
public final class WorkerReportDriver implements ISPDriver {
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
	 * @param model ignored
	 * @throws DriverFailedException always: this driver has to write to the filesystem
	 */
	@Override
	public void startDriver(final IDriverModel model) throws DriverFailedException {
		throw new DriverFailedException(new IllegalStateException("The report driver has" +
				                                                          " to interact " +
				                                                          "with files"));
	}

	/**
	 * Start the driver.
	 *
	 * @param args command-line arguments
	 * @throws DriverFailedException on fatal error
	 */
	@Override
	public void startDriver(final String... args) throws DriverFailedException {
		final MapReaderAdapter reader = new MapReaderAdapter();
		final Warning warner = new Warning(Action.Ignore);
		for (final String filename : args) {
			if (filename == null) {
				continue;
			}
			final String report; // NOPMD
			try {
				// When we developed createReportIR, it was unacceptably slower, so we
				// left this using the original.
				report =
						ReportGenerator.createReport(reader.readMap(new File(
								                                                    filename),
								warner));
			} catch (final MapVersionException except) {
				throw new DriverFailedException(filename
						                                +
						                                " contained a map format version" +
						                                " we can't handle",
						                               except);
			} catch (final IOException except) {
				throw new DriverFailedException(
						                               "I/O error reading " + filename,
						                               except);
			} catch (final XMLStreamException except) {
				throw new DriverFailedException("Error parsing XML in "
						                                + filename, except);
			} catch (final SPFormatException except) {
				throw new DriverFailedException(filename
						                                +
						                                " didn't contain a valid SP map",
						                               except);
			}
			try (final FileWriter writer = new FileWriter(filename // NOPMD
					                                              + ".report.html")) {
				writer.write(report);
			} catch (final IOException except) {
				throw new DriverFailedException("I/O error writing report",
						                               except);
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
	 * @return what to call the driver in a CLI list.
	 */
	@Override
	public String getName() {
		return USAGE.getShortDescription();
	}

	/**
	 * @param nomen ignored
	 */
	@Override
	public void setName(final String nomen) {
		throw new IllegalStateException("Can't rename a driver");
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

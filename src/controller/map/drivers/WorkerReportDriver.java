package controller.map.drivers;

import java.io.FileWriter;
import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import util.Warning;
import controller.map.formatexceptions.MapVersionException;
import controller.map.formatexceptions.SPFormatException;
import controller.map.misc.DriverUsage;
import controller.map.misc.DriverUsage.ParamCount;
import controller.map.misc.MapReaderAdapter;
import controller.map.report.ReportGenerator;
/**
 * A driver to produce a report of the units in a map.
 * @author Jonathan Lovelace
 *
 */
public class WorkerReportDriver implements ISPDriver {
	/**
	 * Run the driver.
	 * @param args command-line arguments.
	 */
	public static void main(final String[] args) {
		try {
			new WorkerReportDriver().startDriver(args);
		} catch (DriverFailedException except) {
			// TODO Auto-generated catch block
			except.printStackTrace();
		}
	}
	/**
	 * Start the driver.
	 * @param args command-line arguments
	 * @throws DriverFailedException  on fatal error
	 */
	@Override
	public void startDriver(final String... args) throws DriverFailedException {
		final ReportGenerator generator = new ReportGenerator();
		final MapReaderAdapter reader = new MapReaderAdapter();
		final Warning warner = new Warning(Warning.Action.Ignore);
		for (final String filename : args) {
			// ESCA-JAVA0177:
			final String report; // NOPMD
			try {
				report = generator.createReport(reader.readMap(filename, warner));
			} catch (MapVersionException except) {
				throw new DriverFailedException(filename + " contained a map format version we can't handle", except);
			} catch (IOException except) {
				throw new DriverFailedException("I/O error reading " + filename, except);
			} catch (XMLStreamException except) {
				throw new DriverFailedException("Error parsing XML in " + filename, except);
			} catch (SPFormatException except) {
				throw new DriverFailedException(filename + " didn't contain a valid SP map", except);
			}
			final FileWriter writer; // NOPMD
			try {
				writer = new FileWriter(filename + ".report.html"); // NOPMD
			} catch (IOException except) {
				throw new DriverFailedException("I/O error writing report", except);
			}
			try {
				writer.write(report);
			} catch (IOException except) {
				throw new DriverFailedException("I/O error writing report", except);
			} finally { // NOPMD
				try {
					writer.close();
				} catch (IOException except) {
					throw new DriverFailedException("I/O error writing report", except);
				}
			}
		}
	}
	/**
	 * An object indicating how to use and invoke this driver.
	 */
	private static final DriverUsage USAGE_OBJ = new DriverUsage(false, "-w",
			"--worker", ParamCount.One, "Worker Report Generator",
			"Produce HTML report of units, workers, etc., in a map.",
			WorkerReportDriver.class);
	/**
	 * @return an object indicating how to use and invoke this driver
	 */
	@Override
	public DriverUsage usage() {
		return USAGE_OBJ;
	}

}

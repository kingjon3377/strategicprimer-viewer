package controller.map.drivers;

import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.stream.XMLStreamException;

import util.Warning;
import view.map.main.ViewerFrame;
import controller.map.drivers.ISPDriver.DriverUsage.ParamCount;
import controller.map.formatexceptions.MapVersionException;
import controller.map.formatexceptions.SPFormatException;
import controller.map.misc.MapReaderAdapter;
import controller.map.report.ReportGenerator;
/**
 * A driver to produce a report of the units in a map.
 * @author Jonathan Lovelace
 *
 */
public class WorkerReportDriver implements ISPDriver {
	/**
	 * An object indicating how to use and invoke this driver.
	 */
	private static final DriverUsage USAGE_OBJ = new DriverUsage(false, "-w",
			"--worker", ParamCount.One, "Worker Report Generator",
			"Produce HTML report of units, workers, etc., in a map.",
			WorkerReportDriver.class);
	/**
	 * Logger.
	 */
	private static final Logger LOGGER = Logger.getLogger(ViewerFrame.class
			.getName());
	/**
	 * Run the driver.
	 * @param args command-line arguments.
	 */
	public static void main(final String[] args) {
		try {
			new WorkerReportDriver().startDriver(args);
		} catch (DriverFailedException except) {
			LOGGER.log(Level.SEVERE, except.getMessage(), except.getCause());
		}
	}
	/**
	 * Start the driver.
	 * @param args command-line arguments
	 * @throws DriverFailedException  on fatal error
	 */
	@Override
	public void startDriver(final String... args) throws DriverFailedException {
		final MapReaderAdapter reader = new MapReaderAdapter();
		final Warning warner = new Warning(Warning.Action.Ignore);
		for (final String filename : args) {
			// ESCA-JAVA0177:
			final String report; // NOPMD
			try {
				report = ReportGenerator.createReport(reader.readMap(filename, warner));
//				report = ReportGenerator.createReportIR(reader.readMap(filename, warner)).produce();
			} catch (MapVersionException except) {
				throw new DriverFailedException(filename + " contained a map format version we can't handle", except);
			} catch (IOException except) {
				throw new DriverFailedException("I/O error reading " + filename, except);
			} catch (XMLStreamException except) {
				throw new DriverFailedException("Error parsing XML in " + filename, except);
			} catch (SPFormatException except) {
				throw new DriverFailedException(filename + " didn't contain a valid SP map", except);
			}
			try (final FileWriter writer = new FileWriter(filename + ".report.html")) {
				writer.write(report);
			} catch (IOException except) {
				throw new DriverFailedException("I/O error writing report", except);
			}
		}
	}
	/**
	 * @return an object indicating how to use and invoke this driver
	 */
	@Override
	public DriverUsage usage() {
		return USAGE_OBJ;
	}
	/**
	 * @return what to call the driver in a CLI list.
	 */
	@Override
	public String getName() {
		return USAGE_OBJ.getShortDescription();
	}
	/**
	 * @param nomen ignored
	 */
	@Override
	public void setName(final String nomen) {
		throw new IllegalStateException("Can't rename a driver");
	}
}

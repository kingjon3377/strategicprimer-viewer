package controller.map.drivers;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.stream.XMLStreamException;

import model.map.IMap;
import model.map.MapView;
import util.Warning;
import view.util.SystemOut;
import controller.map.converter.ResolutionDecreaseConverter;
import controller.map.formatexceptions.MapVersionException;
import controller.map.formatexceptions.SPFormatException;
import controller.map.misc.DriverUsage;
import controller.map.misc.MapReaderAdapter;
import controller.map.misc.DriverUsage.ParamCount;

/**
 * A driver to convert maps to the new format.
 *
 * @author Jonathan Lovelace
 *
 */
public final class ConverterDriver implements ISPDriver {
	/**
	 * Logger.
	 */
	private static final Logger LOGGER = Logger.getLogger(ConverterDriver.class
			.getName());
	/**
	 * The converter to use.
	 */
	private static final ResolutionDecreaseConverter CONV = new ResolutionDecreaseConverter();
	/**
	 * The map reader we'll use.
	 */
	private static final MapReaderAdapter READER = new MapReaderAdapter();

	/**
	 * Main method.
	 *
	 * @param args the names of files to convert. Each, after conversion, will
	 *        be written back to its original name plus ".new"
	 */
	public static void main(final String[] args) {
		try {
			new ConverterDriver().startDriver(args);
		} catch (DriverFailedException except) {
			LOGGER.log(Level.SEVERE, except.getMessage(), except.getCause());
		}
	}
	/**
	 * Run the driver.
	 * @param args command-line argument
	 * @throws DriverFailedException on fatal error
	 */
	@Override
	public void startDriver(final String... args) throws DriverFailedException {
		if (args.length < 1) {
			SystemOut.SYS_OUT
					.println("Usage: ConverterDriver filename [filename ...]");
			throw new DriverFailedException("Need files to convert",
					new IllegalArgumentException("Not enough arguments"));
		}
		for (final String filename : args) {
			SystemOut.SYS_OUT.print("Reading ");
			SystemOut.SYS_OUT.print(filename);
			SystemOut.SYS_OUT.print(" ... ");
			try {
				final IMap old = READER.readMap(filename, Warning.INSTANCE);
				SystemOut.SYS_OUT.println(" ... Converting ... ");
				final String newFilename = filename + ".new";
				final MapView map = CONV.convert(old);
				SystemOut.SYS_OUT.print("About to write ");
				SystemOut.SYS_OUT.println(newFilename);
				new MapReaderAdapter().write(newFilename, map); // NOPMD
			} catch (final MapVersionException e) {
				LOGGER.log(Level.SEVERE, "Map version in " + filename
						+ " not acceptable to reader", e);
				continue;
			} catch (final FileNotFoundException e) {
				LOGGER.log(Level.SEVERE, filename + " not found", e);
			} catch (final IOException e) {
				LOGGER.log(Level.SEVERE, "I/O error processing " + filename, e);
				continue;
			} catch (final XMLStreamException e) {
				LOGGER.log(Level.SEVERE,
						"XML stream error reading " + filename, e);
				continue;
			} catch (final SPFormatException e) {
				LOGGER.log(Level.SEVERE, "SP map format error reading "
						+ filename, e);
				continue;
			}
		}
	}
	/**
	 * An object indicating how to use and invoke this driver.
	 */
	private static final DriverUsage USAGE_OBJ = new DriverUsage(false, "-v",
			"--convert", ParamCount.One, "Convert a map's format",
			"Convert a map. At present, this means reducing its resolution.",
			ConverterDriver.class);

	/**
	 * @return an object indicating how to use and invoke this driver.
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
}

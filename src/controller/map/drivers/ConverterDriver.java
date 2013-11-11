package controller.map.drivers;

import static view.util.SystemOut.SYS_OUT;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.stream.XMLStreamException;

import model.map.IMap;
import model.map.MapView;
import util.TypesafeLogger;
import util.Warning;
import controller.map.converter.ResolutionDecreaseConverter;
import controller.map.drivers.ISPDriver.DriverUsage.ParamCount;
import controller.map.formatexceptions.MapVersionException;
import controller.map.formatexceptions.SPFormatException;
import controller.map.misc.MapReaderAdapter;

/**
 * A driver to convert maps to the new format.
 *
 * @author Jonathan Lovelace
 *
 */
public final class ConverterDriver implements ISPDriver {
	/**
	 * An object indicating how to use and invoke this driver.
	 */
	private static final DriverUsage USAGE_OBJ = new DriverUsage(false, "-v",
			"--convert", ParamCount.One, "Convert a map's format",
			"Convert a map. At present, this means reducing its resolution.",
			ConverterDriver.class);

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
	 * Main method.
	 *
	 * @param args the names of files to convert. Each, after conversion, will
	 *        be written back to its original name plus ".new"
	 */
	public static void main(final String[] args) {
		try {
			new ConverterDriver().startDriver(args);
		} catch (final DriverFailedException except) {
			LOGGER.log(Level.SEVERE, except.getMessage(), except.getCause());
		}
	}

	/**
	 * Run the driver.
	 *
	 * @param args command-line argument
	 * @throws DriverFailedException on fatal error
	 */
	@Override
	public void startDriver(final String... args) throws DriverFailedException {
		if (args.length < 1) {
			SYS_OUT.println("Usage: ConverterDriver filename [filename ...]");
			throw new DriverFailedException("Need files to convert",
					new IllegalArgumentException("Not enough arguments"));
		}
		for (final String filename : args) {
			if (filename == null) {
				continue;
			}
			SYS_OUT.print("Reading ");
			SYS_OUT.print(filename);
			SYS_OUT.print(" ... ");
			try {
				final IMap old = READER.readMap(filename, Warning.INSTANCE);
				SYS_OUT.println(" ... Converting ... ");
				final String newFilename = filename + ".new";
				final MapView map = ResolutionDecreaseConverter.convert(old);
				SYS_OUT.print("About to write ");
				SYS_OUT.println(newFilename);
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
	@Override
	public String toString() {
		return "ConverterDriver";
	}
}

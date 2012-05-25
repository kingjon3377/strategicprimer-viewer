package controller.map.drivers;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.stream.XMLStreamException;

import model.map.IMap;
import util.Warning;
import view.util.SystemOut;
import controller.map.MapVersionException;
import controller.map.SPFormatException;
import controller.map.converter.ResolutionDecreaseConverter;
import controller.map.misc.MapReaderAdapter;
import controller.map.readerng.MapWriterNG;

/**
 * A driver to convert maps to the new format.
 * @author Jonathan Lovelace
 *
 */
public final class ConverterDriver {
	/**
	 * Logger.
	 */
	private static final Logger LOGGER = Logger.getLogger(ConverterDriver.class.getName());
	/**
	 * Do not instantiate.
	 */
	private ConverterDriver() {
		// Do nothing.
	}
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
	 * @param args
	 *            the names of files to convert. Each, after conversion, will be
	 *            written back to its original name plus ".new"
	 */
	public static void main(final String[] args) {
		if (args.length < 1) {
			SystemOut.SYS_OUT.println("Usage: ConverterDriver filename [filename ...]");
		}
		for (final String filename : args) {
			SystemOut.SYS_OUT.print("Starting ");
			SystemOut.SYS_OUT.println(filename);
			try {
				final IMap old = READER.readMap(filename, Warning.INSTANCE);
				final IMap map = CONV.convert(old);
				SystemOut.SYS_OUT.print("About to write ");
				SystemOut.SYS_OUT.print(filename);
				SystemOut.SYS_OUT.println(".new");
				new MapWriterNG().write(filename + ".new", map, false); // NOPMD
			} catch (MapVersionException e) {
				LOGGER.log(Level.SEVERE, "Map version in " + filename + " not acceptable to reader", e);
				continue;
			} catch (FileNotFoundException e) {
				LOGGER.log(Level.SEVERE, filename + " not found", e);
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE, "I/O error processing " + filename, e);
				continue;
			} catch (XMLStreamException e) {
				LOGGER.log(Level.SEVERE, "XML stream error reading " + filename, e);
				continue;
			} catch (SPFormatException e) {
				LOGGER.log(Level.SEVERE, "SP map format error reading " + filename, e);
				continue;
			}
		}
	}
}

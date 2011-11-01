package controller.map.drivers;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.stream.XMLStreamException;

import view.util.SystemOut;

import controller.map.MapVersionException;
import controller.map.SPFormatException;
import controller.map.misc.MapReaderAdapter;

/**
 * A driver to check every map file in a list for errors.
 * @author Jonathan Lovelace
 *
 */
public final class MapChecker {
	/**
	 * Logger.
	 */
	private static final Logger LOGGER = Logger.getLogger(MapChecker.class.getName());
	/**
	 * The map reader we'll use.
	 */
	private static final MapReaderAdapter READER = new MapReaderAdapter();
	/**
	 * Do not instantiate.
	 */
	private MapChecker() {
		// Nothing.
	}
	/**
	 * @param args the list of filenames to check
	 */
	public static void main(final String[] args) {
		if (args.length < 1) {
			SystemOut.SYS_OUT.println("Usage: MapChecker filename [filename ...]");
		}
		for (String filename : args) {
			try {
				READER.readMap(filename);
			} catch (MapVersionException e) {
				LOGGER.log(Level.SEVERE, "Map version in " + filename + " not acceptable to reader", e);
				continue;
			} catch (FileNotFoundException e) {
				LOGGER.log(Level.SEVERE, filename + " not found", e);
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE, "I/O error reading " + filename, e);
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

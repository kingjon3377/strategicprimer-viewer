package controller.map.drivers;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.stream.XMLStreamException;

import view.util.SystemOut;
import controller.map.MapVersionException;
import controller.map.SPFormatException;
import controller.map.simplexml.SimpleXMLReader;

/**
 * A driver to test reflection-based map loading speed against the old way.
 * @author Jonathan Lovelace
 *
 */
public final class ReflectionTestDriver {
	/**
	 * Logger.
	 */
	private static final Logger LOGGER = Logger.getLogger(ReflectionTestDriver.class.getName());
	/**
	 * The map reader we'll use.
	 */
	private static final SimpleXMLReader READER = new SimpleXMLReader();
	/**
	 * How many reps to use on each file.
	 */
	private static final int REPS = 50;
	/**
	 * Do not instantiate.
	 */
	private ReflectionTestDriver() {
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
			SystemOut.SYS_OUT.print("Starting ");
			SystemOut.SYS_OUT.println(filename);
			long start = System.nanoTime();
			try {
			for (int i = 0; i < REPS; i++) {
					READER.readMap(filename);
			}
			long end = System.nanoTime();
			SystemOut.SYS_OUT.print(filename);
			SystemOut.SYS_OUT.print("\t\t\ttook ");
			SystemOut.SYS_OUT.print((end - start));
			SystemOut.SYS_OUT.print(" ns, average of ");
			SystemOut.SYS_OUT.println(((end - start) / REPS));
			start = System.nanoTime();
			for (int i = 0; i < REPS; i++) {
					READER.readMap(filename, true);
			}
			end = System.nanoTime();
			SystemOut.SYS_OUT.print("With reflection, ");
			SystemOut.SYS_OUT.print(filename);
			SystemOut.SYS_OUT.print("\ttook ");
			SystemOut.SYS_OUT.print((end - start));
			SystemOut.SYS_OUT.print(" ns, average of ");
			SystemOut.SYS_OUT.println(((end - start) / REPS));
			} catch (MapVersionException e) {
				LOGGER.log(Level.SEVERE, "Map version in " + filename + " not acceptable to reader", e);
				break;
			} catch (FileNotFoundException e) {
				LOGGER.log(Level.SEVERE, filename + " not found", e);
				break;
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE, "I/O error reading " + filename, e);
				break;
			} catch (XMLStreamException e) {
				LOGGER.log(Level.SEVERE, "XML stream error reading " + filename, e);
				break;
			} catch (SPFormatException e) {
				LOGGER.log(Level.SEVERE, "SP map format error reading " + filename, e);
				break;
			}
		}
	}

}

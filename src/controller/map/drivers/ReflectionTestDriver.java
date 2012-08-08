package controller.map.drivers;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.stream.XMLStreamException;

import util.Warning;
import view.util.SystemOut;
import controller.map.MapVersionException;
import controller.map.SPFormatException;
import controller.map.simplexml.SimpleXMLReader;

/**
 * A driver to test reflection-based map loading speed against the old way.
 * 
 * @author Jonathan Lovelace
 * 
 */
@Deprecated
public final class ReflectionTestDriver {
	/**
	 * Logger.
	 */
	private static final Logger LOGGER = Logger
			.getLogger(ReflectionTestDriver.class.getName());
	/**
	 * The map reader we'll use.
	 */
	private static final SimpleXMLReader READER = new SimpleXMLReader();
	/**
	 * How many reps to use on each file.
	 */
	private static final int REPS = 3;

	/**
	 * Do not instantiate.
	 */
	private ReflectionTestDriver() {
		// Nothing.
	}

	/**
	 * Run the test.
	 * 
	 * @param filename the test file
	 * @param runs how many times to run the test
	 * @param reflection whether to use reflection
	 * @return how long the test took
	 * @throws SPFormatException on map format error
	 * @throws XMLStreamException on XML reading error
	 * @throws IOException on other I/O error
	 */
	private static long runTest(final String filename, final int runs,
			final boolean reflection) throws IOException, XMLStreamException,
			SPFormatException {
		final long start = System.nanoTime();
		for (int i = 0; i < runs; i++) {
			READER.readMap(filename, reflection, Warning.INSTANCE);
		}
		final long end = System.nanoTime();
		return end - start;
	}

	/**
	 * Test correctness by comparing the results of the two reading methods.
	 * 
	 * @param filename the file containing map data to test with
	 * @return whether the two methods returned identical results
	 * @throws SPFormatException on map format error detected by one of the
	 *         readers
	 * @throws XMLStreamException on XML error detected by one of the readers
	 * @throws IOException on I/O error detected by one o the readers
	 */
	private static boolean methodsAgree(final String filename)
			throws IOException, XMLStreamException, SPFormatException {
		return READER.readMap(filename, false, Warning.INSTANCE).equals(
				READER.readMap(filename, true, Warning.INSTANCE));
	}

	/**
	 * @param args the list of filenames to check
	 */
	public static void main(final String[] args) {
		if (args.length < 1) {
			SystemOut.SYS_OUT
					.println("Usage: MapChecker filename [filename ...]");
		}
		for (final String filename : args) {
			try {
				SystemOut.SYS_OUT.print("Starting ");
				SystemOut.SYS_OUT.println(filename);
				SystemOut.SYS_OUT.print("Testing correctness ...\t\t\t");
				if (methodsAgree(filename)) {
					SystemOut.SYS_OUT.println("OK");
				} else {
					SystemOut.SYS_OUT.println("FAIL");
				}
				long duration = runTest(filename, REPS, false);
				SystemOut.SYS_OUT.print(filename);
				SystemOut.SYS_OUT.print("\t\t\ttook ");
				SystemOut.SYS_OUT.print(duration);
				SystemOut.SYS_OUT.print(" ns, average of ");
				SystemOut.SYS_OUT.println((duration / REPS));
				duration = runTest(filename, REPS, true);
				SystemOut.SYS_OUT.print("With reflection, ");
				SystemOut.SYS_OUT.print(filename);
				SystemOut.SYS_OUT.print("\ttook ");
				SystemOut.SYS_OUT.print(duration);
				SystemOut.SYS_OUT.print(" ns, average of ");
				SystemOut.SYS_OUT.println((duration / REPS));
			} catch (final MapVersionException e) {
				LOGGER.log(Level.SEVERE, "Map version in " + filename
						+ " not acceptable to reader", e);
				continue;
			} catch (final FileNotFoundException e) {
				LOGGER.log(Level.SEVERE, filename + " not found", e);
				continue;
			} catch (final IOException e) {
				LOGGER.log(Level.SEVERE, "I/O error reading " + filename, e);
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

}

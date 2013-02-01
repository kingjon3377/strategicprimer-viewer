package controller.map.drivers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.CharBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.stream.XMLStreamException;

import model.map.IMap;
import util.Warning;
import view.util.SystemOut;
import controller.map.cxml.CompactXMLReader;
import controller.map.formatexceptions.MapVersionException;
import controller.map.formatexceptions.SPFormatException;
import controller.map.iointerfaces.IMapReader;
import controller.map.readerng.MapReaderNG;

/**
 * A driver for comparing map readers.
 *
 * @author Jonathan Lovelace
 *
 */
@SuppressWarnings("deprecation")
public class ReaderComparator implements ISPDriver {
	/**
	 * Logger.
	 */
	private static final Logger LOGGER = Logger.getLogger(ReaderComparator.class.getName());
	/**
	 * The first reader.
	 */
	private final IMapReader one = new MapReaderNG();
	/**
	 * The second reader.
	 */
	private final IMapReader two =  new CompactXMLReader();
	/**
	 * The stream to print results to.
	 */
	private final PrintStream out = SystemOut.SYS_OUT;
	/**
	 * Driver method.
	 *
	 * @param args The maps to test the two readers on.
	 */
	public static void main(final String[] args) {
		try {
			new ReaderComparator().startDriver(args);
		} catch (DriverFailedException except) {
			LOGGER.log(Level.SEVERE, except.getMessage(), except.getCause());
		}
	}

	/**
	 * Compare the two readers.
	 *
	 * @param args The list of specified files to compare them on
	 */
	public void compareReaders(final String[] args) {
		for (final String arg : args) {
			try {
				compareReaders(arg);
			} catch (final XMLStreamException e) {
				LOGGER.log(Level.SEVERE,
						"XMLStreamException (probably badly formed input) in "
								+ arg, e);
				continue;
			} catch (final FileNotFoundException e) {
				LOGGER.log(Level.SEVERE, arg + " not found", e);
				continue;
			} catch (final IOException e) {
				LOGGER.log(Level.SEVERE, "I/O error while parsing" + arg, e);
				continue;
			} catch (final MapVersionException e) {
				LOGGER.log(Level.SEVERE,
						"Map version too old for old-style reader in file "
								+ arg, e);
				continue;
			} catch (final SPFormatException e) {
				LOGGER.log(Level.SEVERE,
						"New reader claims invalid SP map data in " + arg, e);
				continue;
			}
		}
	}

	/**
	 * Compare the two readers on a file.
	 *
	 * @param arg the name of the file to have each read.
	 * @throws XMLStreamException if either reader claims badly formed input
	 * @throws FileNotFoundException if either reader can't find the file
	 * @throws IOException on other I/O error in either reader
	 * @throws SPFormatException if the new reader claims invalid data
	 * @throws MapVersionException if the old reader can't handle that map
	 *         version
	 */
	// ESCA-JAVA0160:
	public void compareReaders(final String arg) throws XMLStreamException,
			FileNotFoundException, IOException, SPFormatException,
			MapVersionException {
		final Warning warner = new Warning(Warning.Action.Ignore);
		out.print(arg);
		out.println(':');
		final File file = new File(arg);
		final FileReader reader = new FileReader(file);
		final CharBuffer buffer = CharBuffer.allocate((int) file.length());
		try {
			reader.read(buffer);
		} finally {
			reader.close();
		}
		buffer.position(0);
		final String contents = buffer.toString();
		final long startOne = System.nanoTime();
		final IMap map1 = one.readMap(arg, new StringReader(contents), warner);
		final long endOne = System.nanoTime();
		out.print("Old method took ");
		out.print(endOne - startOne);
		out.println(" time-units.");
		final long startTwo = System.nanoTime();
		final IMap map2 = two.readMap(arg, new StringReader(contents), warner);
		final long endTwo = System.nanoTime();
		out.print("New method took ");
		out.print(endTwo - startTwo);
		out.println(" time-units.");
		if (map1.equals(map2)) {
			out.println("Readers produce identical results.");
		} else {
			out.print("Readers differ on ");
			out.println(arg);
		}
	}

	/**
	 *
	 * @return a String representation of this object
	 */
	@Override
	public String toString() {
		return "ReaderComparator";
	}
	/**
	 * Run the driver, comparing the readers' performance.
	 * @param args The files to test on
	 * @throws DriverFailedException Probably never?
	 */
	@Override
	public void startDriver(final String... args) throws DriverFailedException {
		compareReaders(args);
	}
}

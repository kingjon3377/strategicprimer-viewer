package controller.map.drivers;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.stream.XMLStreamException;

import model.map.SPMap;
import view.util.SystemOut;
import controller.map.IMapReader;
import controller.map.MapVersionException;
import controller.map.SPFormatException;
import controller.map.readerng.MapReaderNG;
import controller.map.simplexml.SimpleXMLReader;

/**
 * A driver for comparing map readers.
 * 
 * @author Jonathan Lovelace
 * 
 */
@SuppressWarnings("deprecation")
public class ReaderComparator {
	/**
	 * Driver method.
	 * 
	 * @param args
	 *            The maps to test the two readers on.
	 */
	public static void main(final String[] args) {
		new ReaderComparator(SystemOut.SYS_OUT, new SimpleXMLReader(),
				new MapReaderNG()).compareReaders(args,
				Logger.getLogger(ReaderComparator.class.getName()));
	}

	/**
	 * Compare the two readers.
	 * 
	 * @param args
	 *            The list of specified files to compare them on
	 * @param logger
	 *            The logger to log errors to.
	 */
	public void compareReaders(final String[] args, final Logger logger) {
		for (final String arg : args) {
			try {
				compareReaders(arg);
			} catch (final XMLStreamException e) {
				logger.log(Level.SEVERE,
						"XMLStreamException (probably badly formed input) in "
								+ arg, e);
				continue;
			} catch (final FileNotFoundException e) {
				logger.log(Level.SEVERE, arg + " not found", e);
				continue;
			} catch (final IOException e) {
				logger.log(Level.SEVERE, "I/O error while parsing" + arg, e);
				continue;
			} catch (final MapVersionException e) {
				logger.log(Level.SEVERE,
						"Map version too old for old-style reader in file "
								+ arg, e);
				continue;
			} catch (final SPFormatException e) {
				logger.log(Level.SEVERE,
						"New reader claims invalid SP map data in " + arg, e);
				continue;
			}
		}
	}

	/**
	 * Constructor.
	 * 
	 * @param ostream
	 *            the stream to print results to
	 * @param readerOne
	 *            the first reader
	 * @param readerTwo
	 *            the second reader
	 */
	public ReaderComparator(final PrintStream ostream,
			final IMapReader readerOne, final IMapReader readerTwo) {
		out = ostream;
		one = readerOne;
		two = readerTwo;
	}

	/**
	 * The stream to print results to.
	 */
	private final PrintStream out;
	/**
	 * The first reader.
	 */
	private final IMapReader one;
	/**
	 * The second reader.
	 */
	private final IMapReader two;

	/**
	 * Compare the two readers on a file.
	 * 
	 * @param arg
	 *            the name of the file to have each read.
	 * @throws XMLStreamException
	 *             if either reader claims badly formed input
	 * @throws FileNotFoundException
	 *             if either reader can't find the file
	 * @throws IOException
	 *             on other I/O error in either reader
	 * @throws SPFormatException
	 *             if the new reader claims invalid data
	 * @throws MapVersionException
	 *             if the old reader can't handle that map version
	 */
	// ESCA-JAVA0160:
	public void compareReaders(final String arg) throws XMLStreamException,
			FileNotFoundException, IOException, SPFormatException,
			MapVersionException {
		final long startOne = System.nanoTime();
		final SPMap map1 = one.readMap(arg);
		final long endOne = System.nanoTime();
		out.print("Old method took ");
		out.print((endOne - startOne));
		out.println(" time-units.");
		final long startTwo = System.nanoTime();
		final SPMap map2 = two.readMap(arg);
		final long endTwo = System.nanoTime();
		out.print("New method took ");
		out.print((endTwo - startTwo));
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
}

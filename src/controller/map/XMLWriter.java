package controller.map;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

import model.map.SPMap;

/**
 * A class to write a map to file.
 * 
 * @author JOnathan Lovelace
 * 
 */
public class XMLWriter { // NOPMD
	/**
	 * The writer we'll write to.
	 */
	private final PrintWriter writer;

	/**
	 * Constructor.
	 * 
	 * @param filename
	 *            the file to write to
	 * 
	 * @throws IOException
	 *             on I/O error opening the file
	 */
	public XMLWriter(final String filename) throws IOException {
		this(new FileWriter(filename)); // $codepro.audit.disable
										// closeWhereCreated
	}

	/**
	 * Constructor. FIXME: The writer (or filename) should be a parameter to
	 * write(), not an instance variable.
	 * 
	 * @param out
	 *            the writer to write to
	 */
	public XMLWriter(final Writer out) {
		writer = new PrintWriter(new BufferedWriter(out)); // $codepro.audit.disable
															// closeWhereCreated
	}

	/**
	 * Write a map.
	 * 
	 * @param map
	 *            the map to write
	 */
	public void write(final SPMap map) {
		try {
			writer.print("<?xml version=");
			printQuoted("1.0");
			writer.println("?>");
			writer.println(map.toXML());
		} finally {
			writer.close();
		}
	}

	/**
	 * Prints a string in quotation marks.
	 * 
	 * @param text
	 *            the string to print
	 */
	private void printQuoted(final String text) {
		writer.print('"');
		writer.print(text);
		writer.print('"');
	}

	/**
	 * 
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "XMLWriter";
	}
}

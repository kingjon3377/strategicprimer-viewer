package controller.map.simplexml;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

import controller.map.SPWriter;

import model.map.SPMap;

/**
 * A class to write a map to file.
 * 
 * @author JOnathan Lovelace
 * 
 */
@Deprecated
public class SimpleXMLWriter implements SPWriter { // NOPMD
	/**
	 * Write a map.
	 * @param filename the file to write to
	 * @param map the map to write. 
	 * @throws IOException on error opening the file
	 */
	@Override
	public void write(final String filename, final SPMap map) throws IOException {
		final FileWriter writer = new FileWriter(filename);
		try {
			write(writer, map);
		} finally {
			writer.close();
		}
	}

	/**
	 * Write a map.
	 * 
	 * @param out the writer to write to
	 * @param map
	 *            the map to write
	 */
	@Override
	public void write(final Writer out, final SPMap map) {
		final PrintWriter writer = new PrintWriter(new BufferedWriter(out));
		try {
			writer.print("<?xml version=");
			printQuoted(writer, "1.0");
			writer.println("?>");
			writer.println(map.toXML());
		} finally {
			writer.close();
		}
	}

	/**
	 * Prints a string in quotation marks.
	 * @param writer the Writer to write to
	 * @param text
	 *            the string to print
	 */
	private static void printQuoted(final PrintWriter writer, final String text) {
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

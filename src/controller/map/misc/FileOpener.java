package controller.map.misc;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.io.StringReader;

/**
 * A (stateless, with singleton) class to open files for the XML readers. It
 * exists because we also want a special extension for testing the "include"
 * tag: filenames beginning "string:" (with the first tag following immediately)
 * are, after that prefix is stripped, turned into a StringReader instead of a
 * FileReader as usual.
 *
 * @author Jonathan Lovelace
 *
 */
public class FileOpener {
	/**
	 * Singleton instance.
	 */
	public static final FileOpener OPENER = new FileOpener();

	/**
	 * If filename begins "string:", with the colon followed immediately by the
	 * angle-bracket to begin the first XML tag, it is not treated as a
	 * filename; instead, a StringReader is created from the string (with the
	 * "string:" prefix removed) and returned.
	 *
	 * @param filename a filename
	 * @return a Reader reading the file it contains (but see method summary)
	 * @throws FileNotFoundException if file not found.
	 */
	public static Reader createReader(final String filename)
			throws FileNotFoundException {
		// ESCA-JAVA0177:
		final Reader istream; // NOPMD
		if (filename.contains("string:<")) {
			istream = new StringReader(filename.substring(7));
		} else {
			istream = new FileReader(filename);
		}
		return istream;
	}
	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "FileOpener";
	}
}

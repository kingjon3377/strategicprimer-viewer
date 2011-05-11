package util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Load a file, from a file or (if no such file) from a resource on the
 * classpath.
 * 
 * @author hoom Taken from
 *         http://hoomb.blogspot.com/2010/12/read-text-file-from-
 *         resource-jar-or.html
 * @author Adapted by Jonathan Lovelace to pass our static analysis and fit our
 *         needs.
 * 
 */
public class LoadFile {
	/**
	 * Load a file from disk or the classpath.
	 * 
	 * @param fileName
	 *            Name of the file to load
	 * @return the file as an input stream
	 * @throws FileNotFoundException
	 *             when the file isn't in either place
	 */
	public BufferedReader doLoadFile(final String fileName)
			throws FileNotFoundException {
		// ESCA-JAVA0177:
		InputStream inputStream; // $codepro.audit.disable localDeclaration
		// try to load file from disk
		try {
			inputStream = new FileInputStream(fileName); // $codepro.audit.disable closeWhereCreated
		} catch (final FileNotFoundException fe) {
			// failed, so try to load it from resources in class path
			inputStream = LoadFile.class.getClassLoader().getResourceAsStream(
					fileName); // NOPMD
			if (inputStream == null) {
				throw new FileNotFoundException("could not find file: "
						+ fileName); // NOPMD
			}
		}
		return new BufferedReader(new InputStreamReader(inputStream)); // $codepro.audit.disable closeWhereCreated
	}
}

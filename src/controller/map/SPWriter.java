package controller.map;

import java.io.IOException;
import java.io.Writer;

import model.map.SPMap;
/**
 * An interface for map (and other SP XML) writers.
 * @author Jonathan Lovelace
 *
 */
public interface SPWriter {

	/**
	 * Write a map.
	 * @param filename the file to write to
	 * @param map the map to write. 
	 * @throws IOException on error opening the file
	 */
	void write(final String filename, final SPMap map) throws IOException;

	/**
	 * Write a map.
	 * 
	 * @param out the writer to write to
	 * @param map
	 *            the map to write
	 */
	void write(final Writer out, final SPMap map);

}
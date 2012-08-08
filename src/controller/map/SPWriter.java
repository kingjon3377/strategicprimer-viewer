package controller.map;

import java.io.IOException;
import java.io.Writer;

import model.map.IMap;

/**
 * An interface for map (and other SP XML) writers.
 * 
 * @author Jonathan Lovelace
 * 
 */
public interface SPWriter {

	/**
	 * Write a map.
	 * 
	 * @param filename the file to write to
	 * @param map the map to write.
	 * @param inclusion whether to write to other files if sub-objects came from
	 *        'include' tags.
	 * @throws IOException on error opening the file
	 */
	void write(final String filename, final IMap map, final boolean inclusion)
			throws IOException;

	/**
	 * Write a map.
	 * 
	 * @param out the writer to write to
	 * @param map the map to write
	 * @param inclusion whether to write to other files if sub-objects came from
	 *        'include' tags.
	 * @throws IOException on error in writing
	 */
	void write(final Writer out, final IMap map, final boolean inclusion)
			throws IOException;

}

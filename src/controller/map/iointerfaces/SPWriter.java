package controller.map.iointerfaces;

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
	 * @throws IOException on error opening the file
	 */
	void write(final String filename, final IMap map) throws IOException;

	/**
	 * Write a map.
	 *
	 * @param ostream the writer to write to
	 * @param map the map to write
	 * @throws IOException on error in writing
	 */
	void write(final Writer ostream, final IMap map) throws IOException;

}

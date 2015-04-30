package controller.map.iointerfaces;

import java.io.File;
import java.io.IOException;

import model.map.IMapNG;

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
	 * @param file the file to write to
	 * @param map the map to write.
	 * @throws IOException on error opening the file
	 */
	void write(File file, IMapNG map) throws IOException;

	/**
	 * Write a map.
	 *
	 * @param ostream the writer to write to
	 * @param map the map to write
	 * @throws IOException on error in writing
	 */
	void write(Appendable ostream, IMapNG map) throws IOException;

}

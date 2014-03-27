package view.util;

import java.io.IOException;

/**
 * An interface for UIs that can save and open their contents to and from file.
 *
 * @author Jonathan Lovelace
 *
 */
public interface SaveableOpenable {
	/**
	 * Load from file.
	 * 
	 * @param file
	 *            the filename to load from
	 * 
	 * @throws IOException
	 *             if the file doesn't exist or on other I/O error while loading
	 */
	void open(final String file) throws IOException;

	/**
	 * Save to file.
	 *
	 * @param file the filename to save to
	 *
	 * @throws IOException on I/O error while saving
	 */
	void save(final String file) throws IOException;
}

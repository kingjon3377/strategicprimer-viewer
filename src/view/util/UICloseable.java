package view.util;

/**
 * An interface for closeable UI elements. There is a Closeable interface
 * already, but it's for I/O streams and so declares the method to throw an
 * IOException.
 * 
 * @author Jonathan Lovelace
 * 
 */
public interface UICloseable {
	/**
	 * Close the element.
	 */
	void close();
}

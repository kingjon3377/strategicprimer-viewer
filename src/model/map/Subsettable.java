package model.map;

import java.io.IOException;


/**
 * An interface to let us check converted player maps against the main map.
 *
 * @author Jonathan Lovelace
 *
 * @param <T> The type itself.
 */
public interface Subsettable<T> {
	/**
	 * @param obj an object
	 * @return whether it is a strict subset of this object---with no members
	 *         that aren't also in this.
	 * @param ostream the stream to write details to
	 * @throws IOException on I/O error writing output to the stream
	 */
	boolean isSubset(T obj, Appendable ostream) throws IOException;
}

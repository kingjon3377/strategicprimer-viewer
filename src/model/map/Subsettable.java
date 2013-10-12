package model.map;

import java.io.PrintWriter;

import org.eclipse.jdt.annotation.NonNull;

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
	 * @param out the stream to write details to
	 */
	boolean isSubset(@NonNull T obj, @NonNull PrintWriter out);
}

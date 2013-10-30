package util;

import java.util.Map;

/**
 * A Map that actually executes the removal of elements only when the coalesce()
 * method is called.
 *
 * @author Jonathan Lovelace
 *
 * @param <K> the first type parameter
 * @param <V> the second type parameter
 */
public interface DelayedRemovalMap<K, V> extends Map<K, V> {

	/**
	 * Apply all scheduled removals.
	 */
	void coalesce();
}

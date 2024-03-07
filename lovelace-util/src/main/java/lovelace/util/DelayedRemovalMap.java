package lovelace.util;

import java.util.Map;

/**
 * A {@link Map} that actually executes the removal of elements only when the {@link #coalesce} method is called, to
 * avoid concurrent-modification errors.
 */
public interface DelayedRemovalMap<Key, Item> extends Map<Key, Item> {
	/**
	 * Apply all scheduled and pending removals.
	 */
	void coalesce();
}

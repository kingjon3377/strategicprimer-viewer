package drivers.advancement;

/**
 * An interface for objects listening for added or removed items in lists.
 */
@FunctionalInterface
public interface AddRemoveListener {
	/**
	 * Handle something being added.
	 *
	 * @param category What kind of thing is being added
	 * @param addendum A String description of the thing to be added
	 */
	void add(String category, String addendum);

	/**
	 * Handle something being removed. Default implementation is a no-op.
	 *
	 * @param category What kind of thing is being removed
	 */
	default void remove(final String category) {
	}
}

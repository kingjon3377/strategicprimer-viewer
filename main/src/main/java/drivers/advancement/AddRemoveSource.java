package drivers.advancement;

/**
 * An interface for UIs (etc.) for adding and removing items in lists.
 *
 * TODO: Move to lovelace.util?
 */
interface AddRemoveSource {
	/**
	 * Add a listener.
	 */
	void addAddRemoveListener(AddRemoveListener listener);

	/**
	 * Remove a listener.
	 */
	void removeAddRemoveListener(AddRemoveListener listener);
}

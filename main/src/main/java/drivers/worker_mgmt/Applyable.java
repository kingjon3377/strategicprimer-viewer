package drivers.worker_mgmt;

/**
 * An interface to help simplify form management
 *
 * TODO: Move to lovelace.util
 *
 * TODO: Do we really need given there's Runnable and method references?
 *
 * TODO: Combine with Revertible again?
 */
public interface Applyable {
	/**
	 * Method to call when an "Apply" button is pressed.
	 */
	void apply();
}

package drivers.worker_mgmt;

/**
 * An interface to, together with {@link Applyable}, simplify form management.
 */
public interface Revertible {
	/**
	 * Method to call when a "Revert" button is pressed.
	 */
	void revert();
}

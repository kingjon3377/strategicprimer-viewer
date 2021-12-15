package drivers.common;

/**
 * An interface for objects that want to know when the current map version changes.
 */
public interface VersionChangeListener {
	/**
	 * Handle a change in map version.
	 */
	void changeVersion(int previous, int newVersion);
}

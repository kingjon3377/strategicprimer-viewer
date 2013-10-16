package model.listeners;

/**
 * An interface for objects that want to know when the current map version
 * changes.
 *
 * @author Jonathan Lovelace
 *
 */
public interface VersionChangeListener {
	/**
	 * @param old the previous version
	 * @param newVersion the new version
	 */
	void changeVersion(int old, int newVersion);
}

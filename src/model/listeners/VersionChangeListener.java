package model.listeners;

import java.util.EventListener;

/**
 * An interface for objects that want to know when the current map version
 * changes.
 *
 * @author Jonathan Lovelace
 *
 */
public interface VersionChangeListener extends EventListener {
	/**
	 * @param old the previous version
	 * @param newVersion the new version
	 */
	void changeVersion(int old, int newVersion);
}

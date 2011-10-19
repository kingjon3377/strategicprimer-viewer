package util;

import java.beans.PropertyChangeListener;

/**
 * An interface for sources of PropertyChangeEvents that don't inherit from Component.
 * @author Jonathan Lovelace
 *
 */
public interface PropertyChangeSource {
	/**
	 * Add a PropertyChangeListener.
	 * @param list the listener to add
	 */
	void addPropertyChangeListener(final PropertyChangeListener list);
	/**
	 * Remove a PropertyChangeListener.
	 * @param list the listener to remove
	 */
	void removePropertyChangeListener(final PropertyChangeListener list);
}

package util;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import org.eclipse.jdt.annotation.Nullable;

/**
 * A proxy for property-change listening. It will pass on any property change
 * events it gets to its own listeners.
 *
 * @author Jonathan Lovelace
 *
 */
public final class PropertyProxy implements PropertyChangeSource,
		PropertyChangeListener {
	/**
	 * The helper we'll use to manage our listeners.
	 */
	private final PropertyChangeSupport pcs;

	/**
	 * Constructor.
	 *
	 * @param source a source of events that we're to proxy.
	 */
	public PropertyProxy(final Component source) {
		source.addPropertyChangeListener(this);
		pcs = new PropertyChangeSupport(source);
	}

	/**
	 * Constructor.
	 *
	 * @param source a source of events that we're to proxy.
	 */
	public PropertyProxy(final PropertyChangeSource source) {
		source.addPropertyChangeListener(this);
		pcs = new PropertyChangeSupport(source);
	}

	/**
	 * Handle (pass on) a property change event.
	 *
	 * @param evt the event to handle.
	 */
	@Override
	public void propertyChange(@Nullable final PropertyChangeEvent evt) {
		if (evt != null) {
			pcs.firePropertyChange(evt);
		}
	}

	/**
	 * Add a property-change listener.
	 *
	 * @param list the listener to add
	 */
	@Override
	public void addPropertyChangeListener(final PropertyChangeListener list) {
		pcs.addPropertyChangeListener(list);
	}

	/**
	 * Remove a property-change listener.
	 *
	 * @param list The listener to remove.
	 */
	@Override
	public void removePropertyChangeListener(final PropertyChangeListener list) {
		pcs.removePropertyChangeListener(list);
	}
	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "PropertyProxy";
	}
}

package util;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import org.eclipse.jdt.annotation.Nullable;

/**
 * A class to change one property into another. It was initially written to
 * allow one class to distinguish between events from two AddRemovePanels.
 *
 * @author Jonathan Lovelace
 *
 */
public class PropertyChangeAdapter implements PropertyChangeListener,
		PropertyChangeSource {
	/**
	 * Constructor.
	 * @param source Who we should say sent the event.
	 * @param oldName the name we should change events from
	 * @param newName the name we should change events to
	 */
	public PropertyChangeAdapter(final PropertyChangeSource source, final String oldName, final String newName) {
		pcs = new PropertyChangeSupport(source);
		from = oldName;
		toName = newName;
		source.addPropertyChangeListener(this);
	}
	/**
	 * The helper that handles dispatching of the adjusted events.
	 */
	private final PropertyChangeSupport pcs;
	/**
	 * The property name we want to change from.
	 */
	private final String from;
	/**
	 * The property name we want to change to.
	 */
	private final String toName;
	/**
	 * @param listener the listener to add
	 */
	@Override
	public void addPropertyChangeListener(final PropertyChangeListener listener) {
		pcs.addPropertyChangeListener(listener);
	}
	/**
	 * @param listener the listener to remove
	 */
	@Override
	public void removePropertyChangeListener(final PropertyChangeListener listener) {
		pcs.removePropertyChangeListener(listener);
	}
	/**
	 * Handle a property change, either by passing the event on, or by adapting it as instructed.
	 * @param evt the event to handle
	 */
	@Override
	public void propertyChange(@Nullable final PropertyChangeEvent evt) {
		if (evt != null) {
			if (from.equals(evt.getPropertyName())) {
				pcs.firePropertyChange(toName, evt.getOldValue(),
						evt.getNewValue());
			} else {
				pcs.firePropertyChange(evt);
			}
		}
	}

}

package util;

import java.beans.PropertyChangeSupport;


/**
 * An extension of PropertyChangeSupport to mark it as implementing PropertyChangeSource.
 * @author Jonathan Lovelace
 */
public class PropertyChangeSupportSource extends PropertyChangeSupport
		implements PropertyChangeSource {
	/**
	 * Version UID for serialization.
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * Constructor.
	 * @param sourceBean passed to superclass
	 */
	public PropertyChangeSupportSource(final Object sourceBean) {
		super(sourceBean);
	}
}
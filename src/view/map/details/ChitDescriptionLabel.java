package view.map.details;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JLabel;

/**
 * A label to describe a Chit.
 * 
 * @author Jonathan Lovelace
 * 
 */
public class ChitDescriptionLabel extends JLabel implements
		PropertyChangeListener {
	/**
	 * Handle a changed property.
	 * 
	 * @param evt the event to handle.
	 */
	@Override
	public void propertyChange(final PropertyChangeEvent evt) {
		if ("chit".equals(evt.getPropertyName())) {
			if (evt.getNewValue() instanceof Chit) {
				setText("<html>" + ((Chit) (evt.getNewValue())).describe()
						+ "</html>");
			} else {
				setText("");
			}
		}
	}

}

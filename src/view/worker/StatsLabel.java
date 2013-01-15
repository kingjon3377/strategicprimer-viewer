package view.worker;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JLabel;

import util.PropertyChangeSource;

/**
 * A label to show a worker's stats. At present it just shows placeholder text.
 * TODO: implement properly when model support for stats is added.
 *
 * @author Jonathan Lovelace
 *
 */
public class StatsLabel extends JLabel implements PropertyChangeListener {
	/**
	 * Constructor.
	 * @param sources things to listen to
	 */
	public StatsLabel(final PropertyChangeSource... sources) {
		for (PropertyChangeSource source : sources) {
			source.addPropertyChangeListener(this);
		}
		addPropertyChangeListener(this);
		firePropertyChange("member", null, null);
		removePropertyChangeListener(this);
	}
	/**
	 * A placeholder string.
	 */
	private static final String PLACEHOLDER = "stat";
	/**
	 * A HTML newline.
	 */
	private static final String NEWLINE = "<br />";
	/**
	 * Handle a property change.
	 * @param evt the event to handle.
	 */
	@Override
	public void propertyChange(final PropertyChangeEvent evt) {
		if ("member".equals(evt.getPropertyName())) {
			if (evt.getNewValue() == null) {
				final StringBuilder builder = new StringBuilder("<html>HP: ");
				builder.append(PLACEHOLDER);
				builder.append(" / ");
				builder.append(PLACEHOLDER);
				builder.append(NEWLINE);
				builder.append("Str: ");
				builder.append(PLACEHOLDER);
				builder.append(NEWLINE);
				builder.append("Dex: ");
				builder.append(PLACEHOLDER);
				builder.append(NEWLINE);
				builder.append("Con: ");
				builder.append(PLACEHOLDER);
				builder.append(NEWLINE);
				builder.append("Int: ");
				builder.append(PLACEHOLDER);
				builder.append(NEWLINE);
				builder.append("Wis: ");
				builder.append(PLACEHOLDER);
				builder.append(NEWLINE);
				builder.append("Cha: ");
				builder.append(PLACEHOLDER);
				builder.append(NEWLINE);
				setText(builder.toString());
			}
		}
	}
}

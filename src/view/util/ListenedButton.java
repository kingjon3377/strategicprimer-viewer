package view.util;

import java.awt.event.ActionListener;

import javax.swing.JButton;

/**
 * A button that takes its listeners as constructor parameters.
 *
 * @author Jonathan Lovelace
 *
 */
public class ListenedButton extends JButton {
	/**
	 * Constructor.
	 *
	 * @param text the text to put on the button
	 * @param listeners listeners to add to the button
	 */
	public ListenedButton(final String text, final ActionListener... listeners) {
		super(text);
		for (final ActionListener listener : listeners) {
			addActionListener(listener);
		}
	}
}

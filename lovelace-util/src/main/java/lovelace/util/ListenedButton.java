package lovelace.util;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.JButton;

/**
 * A button that takes its listeners as constructor parameters.
 */
public class ListenedButton extends JButton {
	private static final long serialVersionUID = 1;
	/**
	 * @param text The text to put on the button
	 */
	public ListenedButton(String text, ActionListener... listeners) {
		super(text);
		for (ActionListener listener : listeners) {
			addActionListener(listener);
		}
	}
}
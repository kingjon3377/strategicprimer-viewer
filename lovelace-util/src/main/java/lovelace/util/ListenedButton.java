package lovelace.util;

import java.awt.event.ActionListener;

import javax.swing.JButton;

/**
 * A button that takes its listeners as constructor parameters.
 */
public final class ListenedButton extends JButton {
	private static final long serialVersionUID = 1;
	/**
	 * @param text The text to put on the button
	 */
	public ListenedButton(final String text, final ActionListener... listeners) {
		super(text);
		for (final ActionListener listener : listeners) {
			addActionListener(listener);
		}
	}

	/**
	 * Alternate constructor for the common case where there's only one listener
	 * and it doesn't care about the parameter.
	 */
	public ListenedButton(final String text, final Runnable listener) {
		this(text, ignored -> listener.run());
	}
}

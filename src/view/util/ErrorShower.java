package view.util;

import java.awt.Component;

import javax.swing.JOptionPane;

/**
 * A utility class to show error messages.
 * 
 * @author Jonathan Lovelace
 * 
 */
public final class ErrorShower {
	/**
	 * Do not instantiate.
	 */
	private ErrorShower() {
		// Do not use.
	}

	/**
	 * Show an error dialog.
	 * 
	 * @param parent
	 *            the parent component for the dialog
	 * @param message
	 *            the error message.
	 */
	public static void showErrorDialog(final Component parent,
			final String message) {
		JOptionPane.showMessageDialog(parent, message,
				"Strategic Primer Map Viewer error", JOptionPane.ERROR_MESSAGE);
	}
}

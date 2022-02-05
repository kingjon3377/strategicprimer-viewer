package lovelace.util;

import javax.swing.JOptionPane;

import java.awt.Component;

import org.jetbrains.annotations.Nullable;

public class ShowErrorDialog {
	/**
	 * Show an error dialog to the user.
	 * @deprecated Inline into callers
	 * @param parent The parent component for the dialog. {@link JOptionPane} doesn't seem to care
	 * if it is null.
	 * @param title What to title the dialog.
	 * @param message The error message to show the user.
	 */
	@Deprecated
	public static void showErrorDialog(@Nullable final Component parent, final String title, final String message) {
		JOptionPane.showMessageDialog(parent, message, title, JOptionPane.ERROR_MESSAGE);
	}
}

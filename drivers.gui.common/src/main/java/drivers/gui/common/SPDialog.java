package drivers.gui.common;

import org.jetbrains.annotations.Nullable;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.JDialog;
import javax.swing.WindowConstants;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

import lovelace.util.MenuUtils;
import lovelace.util.Platform;

/**
 * A superclass to perform setup common to dialogs: unlike app windows (which
 * do nothing on close to let us ask the user to confirm closing a modified
 * file) dialogs are set to dispose on close, and we set up hotkeys so both
 * Command/Control+W and Escape hide and dispose the window. We also add a
 * {@link showWindow} method so calling code doesn't have to create a lambda to show a dialog.
 */
public class SPDialog extends JDialog {
	public SPDialog(@Nullable Frame parentFrame, String title) {
		super(parentFrame, title);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		MenuUtils.createHotKey(getRootPane(), "close", (ignored) -> dispose(),
			JComponent.WHEN_IN_FOCUSED_WINDOW,
			KeyStroke.getKeyStroke(KeyEvent.VK_W, Platform.SHORTCUT_MASK),
			KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));
	}

	/**
	 * Show the dialog.
	 */
	public void showWindow() {
		setVisible(true);
	}
}
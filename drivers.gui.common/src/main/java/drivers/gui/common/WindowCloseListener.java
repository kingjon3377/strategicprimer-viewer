package drivers.gui.common;

import java.awt.event.WindowAdapter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.util.logging.Logger;

/**
 * A simple listener to send a "Close" menu event when the user clicks the
 * "Close" window-decoration button, since we now set windows to "do nothing on
 * close".
 */
public class WindowCloseListener extends WindowAdapter {
	private static final Logger LOGGER = Logger.getLogger(WindowCloseListener.class.getName());
	public WindowCloseListener(final ActionListener menuListener) {
		this.menuListener = menuListener;
	}

	private final ActionListener menuListener;

	@Override
	public void windowClosing(final WindowEvent evt) {
		LOGGER.fine("About to send synthetic Close menu event");
		menuListener.actionPerformed(new ActionEvent(evt.getSource(),
			ActionEvent.ACTION_FIRST, "Close"));
		LOGGER.fine("Returned from Close menu handler");
	}
}

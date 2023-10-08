package drivers.gui.common;

import java.awt.event.WindowAdapter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;

import lovelace.util.LovelaceLogger;

/**
 * A simple listener to send a "Close" menu event when the user clicks the
 * "Close" window-decoration button, since we now set windows to "do nothing on
 * close".
 */
public class WindowCloseListener extends WindowAdapter {
    public WindowCloseListener(final ActionListener menuListener) {
        this.menuListener = menuListener;
    }

    private final ActionListener menuListener;

    @Override
    public void windowClosing(final WindowEvent evt) {
        LovelaceLogger.debug("About to send synthetic Close menu event");
        menuListener.actionPerformed(new ActionEvent(evt.getSource(),
                ActionEvent.ACTION_FIRST, "Close"));
        LovelaceLogger.debug("Returned from Close menu handler");
    }
}

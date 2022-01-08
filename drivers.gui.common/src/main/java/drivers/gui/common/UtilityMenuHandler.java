package drivers.gui.common;

import drivers.common.UtilityGUI;
import java.awt.event.ActionEvent;
import lovelace.util.Platform;
import com.apple.eawt.Application;
import com.apple.eawt.AppEvent;
import drivers.gui.common.about.AboutDialog;
import java.util.logging.Logger;
import java.nio.file.Path;
import java.util.logging.Level;
import java.io.IOException;

/**
 * A class to handle menu items for utility apps that only have "Open",
 * "Close", "About", and "Quit" menu items enabled.
 */
public class UtilityMenuHandler {
	private static final Logger LOGGER = Logger.getLogger(UtilityMenuHandler.class.getName());
	public UtilityMenuHandler(UtilityGUI driver, SPFrame window) {
		this.driver = driver;
		this.window = window;
		if (Platform.SYSTEM_IS_MAC) {
			Application.getApplication().setAboutHandler(this::macAboutHandler);
		}
	}

	private final UtilityGUI driver;
	private final SPFrame window;

	/**
	 * Show the About dialog (as a response to a menu-item event).
	 *
	 * Note that this can't be made static because it refers to {@link window}.
	 *
	 * TODO: We'd like to cache the dialog, maybe?
	 */
	private void aboutHandler() {
		try {
			new AboutDialog(window, window.getWindowName()).setVisible(true);
		} catch (IOException except) {
			LOGGER.log(Level.SEVERE, "I/O error reading About dialog contents", except);
			// FIXME: Show an error dialog
		}
	}

	/**
	 * Show the About dialog (as a response to the About item in the Mac app-menu being chosen).
	 */
	private void macAboutHandler(AppEvent.AboutEvent event) {
		aboutHandler();
	}

	/**
	 * Handle the user's chosen menu item.
	 *
	 * TODO: Most of this logic should be called on the EDT
	 */
	public void handleEvent(ActionEvent event) {
		String command = event.getActionCommand().toLowerCase();
		switch (command) {
		case "load":
			SPFileChooser.open((Path) null).call(driver::open); // TODO: Pass event's component reference instead of null
			break;
		case "close":
			window.dispose();
			break;
		case "quit":
			System.exit(0);
			break;
		case "about":
			aboutHandler();
			break;
		default:
			LOGGER.info("Unhandled command " + event.getActionCommand());
			break;
		}
	}
}

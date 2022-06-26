package drivers.gui.common;

import drivers.common.UtilityGUI;
import java.awt.event.ActionEvent;
import lovelace.util.LovelaceLogger;
import lovelace.util.Platform;
import com.apple.eawt.Application;
import com.apple.eawt.AppEvent;
import drivers.gui.common.about.AboutDialog;
import java.nio.file.Path;
import java.io.IOException;

/**
 * A class to handle menu items for utility apps that only have "Open",
 * "Close", "About", and "Quit" menu items enabled.
 */
public class UtilityMenuHandler {
	public UtilityMenuHandler(final UtilityGUI driver, final SPFrame window) {
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
	 * Note that this can't be made static because it refers to {@link #window}.
	 *
	 * TODO: We'd like to cache the dialog, maybe?
	 */
	private void aboutHandler() {
		try {
			new AboutDialog(window, window.getWindowName()).setVisible(true);
		} catch (final IOException except) {
			LovelaceLogger.error(except, "I/O error reading About dialog contents");
			// FIXME: Show an error dialog
		}
	}

	/**
	 * Show the About dialog (as a response to the About item in the Mac app-menu being chosen).
	 */
	private void macAboutHandler(final AppEvent.AboutEvent event) {
		aboutHandler();
	}

	/**
	 * Handle the user's chosen menu item.
	 *
	 * TODO: Most of this logic should be called on the EDT
	 */
	public void handleEvent(final ActionEvent event) {
		final String command = event.getActionCommand().toLowerCase();
		switch (command) {
		case "load" -> SPFileChooser.open((Path) null).call(driver::open);
		case "close" -> window.dispose();
		case "quit" -> System.exit(0);
		case "about" -> aboutHandler();
		default -> LovelaceLogger.info("Unhandled command %s", event.getActionCommand());
		}
	}
}

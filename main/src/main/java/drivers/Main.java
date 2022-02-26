package drivers;

import java.util.Arrays;

import java.util.Optional;
import java.util.logging.Logger;
import java.util.logging.Level;

import javax.swing.UIManager;

import javax.swing.UnsupportedLookAndFeelException;
import lovelace.util.Platform;

import drivers.common.IDriverUsage;
import drivers.common.DriverFailedException;
import drivers.common.IncorrectUsageException;
import drivers.common.SPOptionsImpl;

import drivers.common.cli.CLIHelper;

import com.apple.eawt.Application;

public final class Main {
	private static final Logger LOGGER = Logger.getLogger(Main.class.getName());
	private Main() {}

	public static void main(final String... args) {
		// TODO: Any logger setup we're going to do should go here.
		System.setProperty("com.apple.mrj.application.apple.menu.about.name", "SP Helpers");
		System.setProperty("apple.awt.application.name", "SP Helpers");
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (final ClassNotFoundException|InstantiationException|IllegalAccessException|
				                     UnsupportedLookAndFeelException e) {
			System.err.println("ERROR: Unexpected low-level exception while setting the Java look-and-feel.");
			System.err.println("ERROR: Most likely something is very seriously broken in your Java installation.");
		}
		System.setProperty("apple.laf.useScreenMenuBar", "true");
		// TODO: While we're at it, also set up something to save *all* (our) log messages to a file as well.
		final Logger rootLog = Logger.getLogger("");
		if (Arrays.asList(args).contains("--trace")) {
			rootLog.setLevel(Level.FINER);
			rootLog.getHandlers()[0].setLevel(Level.FINER);
		} else if (Arrays.asList(args).contains("--debug")) {
			rootLog.setLevel(Level.FINE);
			rootLog.getHandlers()[0].setLevel(Level.FINE);
		}
		LOGGER.fine("If you can see this, debug-level log messages are enabled.");
		LOGGER.finer("If you can see this, trace-level log messages are enabled.");
		final SPOptionsImpl options = new SPOptionsImpl();
		if (Platform.SYSTEM_IS_MAC) {
			Application.getApplication().setOpenFileHandler(AppChooserState::handleDroppedFiles);
		}
		final AppStarter appStarter = new AppStarter();
		try {
			appStarter.startDriverOnArguments(new CLIHelper(), options, args);
		} catch (final IncorrectUsageException except) {
			final IDriverUsage usage = except.getCorrectUsage();
			System.err.println(AppChooserState.usageMessage(usage, options.hasOption("--verbose")));
			System.exit(1);
		} catch (final DriverFailedException except) {
			LOGGER.log(Level.SEVERE, except.getMessage(),
				Optional.ofNullable(except.getCause()).orElse(except));
				System.exit(2);
		}
	}
}

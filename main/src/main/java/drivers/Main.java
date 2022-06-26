package drivers;

import java.util.Arrays;

import java.util.Objects;

import javax.swing.UIManager;

import javax.swing.UnsupportedLookAndFeelException;
import lovelace.util.LovelaceLogger;
import lovelace.util.Platform;

import drivers.common.IDriverUsage;
import drivers.common.DriverFailedException;
import drivers.common.IncorrectUsageException;
import drivers.common.SPOptionsImpl;

import drivers.common.cli.CLIHelper;

import com.apple.eawt.Application;

public final class Main {
	private Main() {}

	public static void main(final String... args) {
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
		if (Arrays.asList(args).contains("--trace")) {
			LovelaceLogger.setLevel(LovelaceLogger.Level.TRACE);
		} else if (Arrays.asList(args).contains("--debug")) {
			LovelaceLogger.setLevel(LovelaceLogger.Level.DEBUG);
		}
		LovelaceLogger.debug("If you can see this, debug-level log messages are enabled.");
		LovelaceLogger.trace("If you can see this, trace-level log messages are enabled.");
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
			LovelaceLogger.error(Objects.requireNonNullElse(except.getCause(), except), except.getMessage());
				System.exit(2);
		}
	}
}

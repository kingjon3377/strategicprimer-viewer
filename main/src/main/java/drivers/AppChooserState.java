package drivers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import java.util.Map;
import java.util.ServiceLoader;
import java.util.logging.Logger;
import java.util.logging.Level;

import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.Dimension;
import java.awt.Graphics2D;

import java.io.IOException;

import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.UIManager;
import javax.swing.SwingUtilities;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JLabel;
import javax.swing.JEditorPane;

import static lovelace.util.ShowErrorDialog.showErrorDialog;
import lovelace.util.BorderedPanel;
import lovelace.util.MalformedXMLException;
import lovelace.util.Platform;
import lovelace.util.ListenedButton;

import drivers.common.IDriverModel;
import drivers.common.IDriverUsage;
import drivers.common.SPOptions;
import drivers.common.ParamCount;
import drivers.common.DriverFailedException;
import drivers.common.IncorrectUsageException;
import drivers.common.SPOptionsImpl;
import drivers.common.CLIDriver;
import drivers.common.DriverFactory;
import drivers.common.GUIDriverFactory;
import drivers.common.UtilityDriverFactory;
import drivers.common.ModelDriverFactory;
import drivers.common.UtilityGUI;

import drivers.common.cli.ICLIHelper;
import drivers.common.cli.CLIHelper;

import com.apple.eawt.AppEvent;
import com.apple.eawt.Application;

import com.pump.window.WindowList;

import drivers.gui.common.SPFrame;
import drivers.gui.common.WindowCloseListener;
import drivers.gui.common.SPMenu;

import common.xmlio.SPFormatException;
import common.xmlio.Warning;

import lovelace.util.MissingFileException;

import java.awt.image.BufferedImage;

import impl.xmlio.MapIOHelper;
import lovelace.util.ResourceInputStream;
import lovelace.util.ShowErrorDialog;
import org.javatuples.Pair;

/* package */ class AppChooserState {
	private static final Logger LOGGER = Logger.getLogger(AppChooserState.class.getName());
	// TODO: Define and register a custom log formatter? We had to in
	// Ceylon to get log messags to show at all, but in Java I dropped this
	// as j.u.l includes one. Or TODO: define my own central logging method?

	/**
	 * Create the cache of driver objects.
	 */
	public Map<String, Iterable<DriverFactory>> createCache() {
		// TODO: Use a multimap?
		Map<String, List<DriverFactory>> cache = new HashMap<>();
		Map<String, List<DriverFactory>> conflicts = new HashMap<>();
		for (DriverFactory factory : ServiceLoader.load(DriverFactory.class)) {
			String command = factory.getUsage().getInvocation();
			if (command.startsWith("-")) {
				LOGGER.severe(String.format(
					"An app wants to register an option, %s, not a subcommand", command));
			} else if (conflicts.containsKey(command)) {
				LOGGER.warning(String.format("Additional conflict for %s: %s", command,
					factory.getUsage().getShortDescription()));
				conflicts.get(command).add(factory);
			} else if (cache.containsKey(command) &&
					cache.get(command).stream().anyMatch(f -> f.getUsage().isGraphical() ==
						factory.getUsage().isGraphical())) {
				DriverFactory existing = cache.get(command).stream()
					.filter(f -> f.getUsage().isGraphical() ==
						factory.getUsage().isGraphical()).findAny().orElse(null);
				LOGGER.warning(String.format("Invocation command conflict for %s between %s and %s",
					command, factory.getUsage().getShortDescription(),
					existing.getUsage().getShortDescription()));
				conflicts.put(command, new ArrayList<>(Arrays.asList(factory, existing)));
				List<DriverFactory> existingList = cache.get(command);
				existingList.remove(existing);
				if (existingList.isEmpty()) {
					cache.remove(command);
				}
			} else {
				List<DriverFactory> list = new ArrayList<>();
				list.add(factory);
				cache.put(command, list);
			}
		}
		return new HashMap<>(cache);
	}

	/**
	 * Create the usage message for a particular driver.
	 */
	public String usageMessage(final IDriverUsage usage, final boolean verbose) {
		StringBuilder builder = new StringBuilder();
		builder.append("Usage: ");
		String mainInvocation;
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(
				new ResourceInputStream("invocation", AppChooserState.class)))) {
			mainInvocation = reader.lines().collect(Collectors.joining(System.lineSeparator())).trim();
		} catch (final FileNotFoundException | NoSuchFileException except) {
			LOGGER.warning("Invocation file not found");
			LOGGER.log(Level.FINER, "Stack trace for invocation-not-found", except);
			mainInvocation = "java -jar viewer-VERSION.jar";
		} catch (final IOException except) {
			LOGGER.log(Level.SEVERE, "I/O error reading invocation file", except);
			mainInvocation = "java -jar viewer-VERSION.jar";
		}
		builder.append(mainInvocation);
		if (usage.isGraphical()) {
			builder.append(" [-g|--gui] ");
		} else {
			builder.append(" -c|--cli "); // FIXME For apps with no GUI counterpart this is actually optional
		}
		builder.append(usage.getInvocation());
		for (String option : usage.getSupportedOptions()) {
			builder.append(" [").append(option).append("]");
		}
		switch (usage.getParamsWanted()) {
			case None:
				// Nothing to show
				break;
			case One:
				builder.append(" ").append(usage.getFirstParamDescription());
				break;
			case AtLeastOne:
				builder.append(" ").append(usage.getFirstParamDescription()).append(" [")
					.append(usage.getSubsequentParamDescription()).append(" ...]");
				break;
			case Two:
				builder.append(" ").append(usage.getFirstParamDescription()).append(" ")
					.append(usage.getSubsequentParamDescription());
				break;
			case AtLeastTwo:
				builder.append(" ").append(usage.getFirstParamDescription()).append(" ")
					.append(usage.getSubsequentParamDescription()).append(" [")
					.append(usage.getSubsequentParamDescription()).append(" ...]");
				break;
			case AnyNumber:
				builder.append(" [").append(usage.getSubsequentParamDescription()).append(" ...]");
				break;
			default:
				throw new IllegalStateException("Exhaustive switch wasn't");
			}
			builder.append(System.lineSeparator());
			if (verbose) {
				builder.append(usage.getLongDescription());
			} else {
				builder.append(usage.getShortDescription());
			}
			return builder.toString();
	}

	public static void handleDroppedFiles(final AppEvent.OpenFilesEvent openFilesEvent) {
		SPFrame topWindow = Stream.of(WindowList.getWindows(true, false)).filter(SPFrame.class::isInstance)
			.map(SPFrame.class::cast).reduce((first, second) -> second).orElse(null);
		if (topWindow != null) {
			for (File file : openFilesEvent.getFiles()) {
				try {
					topWindow.acceptDroppedFile(file.toPath());
				} catch (final SPFormatException except) {
					showErrorDialog(topWindow, "Strategic Primer Map Format Error", except.getMessage());
					LOGGER.severe(except.getMessage());
				} catch (final FileNotFoundException|NoSuchFileException|MissingFileException except) {
					showErrorDialog(topWindow, "File Not Found", except.getMessage());
					LOGGER.log(Level.SEVERE, "Dropped file not found", except);
				} catch (final IOException except) {
					showErrorDialog(topWindow, "I/O Error", except.getMessage());
					LOGGER.log(Level.SEVERE, "I/O error reading dropped file", except.getMessage());
				} catch (final MalformedXMLException except) {
					showErrorDialog(topWindow, "Strategic Primer Map Format Error", except.getMessage());
					LOGGER.log(Level.SEVERE, "Malformed XML in " + file, except);
				}
			}
		}
	}
}


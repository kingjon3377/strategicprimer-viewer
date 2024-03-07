package drivers;

import java.awt.desktop.OpenFilesEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.ServiceLoader;

import java.io.IOException;

import java.util.stream.Stream;

import javax.swing.JOptionPane;
import javax.xml.stream.XMLStreamException;

import drivers.common.IDriverUsage;
import drivers.common.DriverFactory;

import com.pump.window.WindowList;

import drivers.gui.common.SPFrame;

import common.xmlio.SPFormatException;

import lovelace.util.LovelaceLogger;
import lovelace.util.Platform;
import org.jetbrains.annotations.Nullable;

/* package */ final class AppChooserState {
	private AppChooserState() {
	}
	// FIXME: Move methods back into AppStarter, unless that would break something.
	// TODO: Define and register a custom log formatter? We had to in
	// Ceylon to get log messags to show at all, but in Java I dropped this
	// as j.u.l includes one. Or TODO: define my own central logging method?

	/**
	 * Create the cache of driver objects.
	 */
	public static Map<String, Iterable<DriverFactory>> createCache() {
		// TODO: Use a multimap?
		final Map<String, List<DriverFactory>> cache = new HashMap<>();
		final Map<String, List<DriverFactory>> conflicts = new HashMap<>();
		for (final DriverFactory factory : ServiceLoader.load(DriverFactory.class)) {
			final String command = factory.getUsage().getInvocation();
			if (command.startsWith("-")) {
				LovelaceLogger.error(
						"An app wants to register an option, %s, not a subcommand", command);
			} else if (conflicts.containsKey(command)) {
				LovelaceLogger.warning("Additional conflict for %s: %s", command,
						factory.getUsage().getShortDescription());
				conflicts.get(command).add(factory);
			} else if (cache.containsKey(command) &&
					cache.get(command).stream().anyMatch(f -> f.getUsage().isGraphical() ==
							factory.getUsage().isGraphical())) {
				final DriverFactory existing = cache.get(command).stream()
						.filter(f -> f.getUsage().isGraphical() ==
								factory.getUsage().isGraphical()).findAny().orElse(null);
				LovelaceLogger.warning("Invocation command conflict for %s between %s and %s",
						command, factory.getUsage().getShortDescription(),
						Optional.ofNullable(existing).map(DriverFactory::getUsage)
								.map(IDriverUsage::getShortDescription).orElse("a null factory"));
				conflicts.put(command, new ArrayList<>(Arrays.asList(factory, existing)));
				final List<DriverFactory> existingList = cache.get(command);
				existingList.remove(existing);
				if (existingList.isEmpty()) {
					cache.remove(command);
				}
			} else if (cache.containsKey(command)) {
				final List<DriverFactory> list = cache.get(command);
				list.add(factory);
				cache.put(command, list);
			} else {
				final List<DriverFactory> list = new ArrayList<>();
				list.add(factory);
				cache.put(command, list);
			}
		}
		return new HashMap<>(cache);
	}

	private static @Nullable Path getContainingApp(final @Nullable Path path) {
		if (Objects.isNull(path)) {
			return null;
		}
		if (path.toString().endsWith(".app") || path.toString().endsWith(".app/")) {
			return path;
		}
		final Path root = path.getRoot();
		if (Objects.isNull(root) || root.equals(path)) {
			return null;
		} else {
			return getContainingApp(path.getParent());
		}
	}

	/**
	 * Create the usage message for a particular driver.
	 *
	 * FIXME: Cache "invocation" contents or equivalent.
	 */
	public static String usageMessage(final IDriverUsage usage, final boolean verbose) {
		final StringBuilder builder = new StringBuilder();
		String mainInvocation;
		builder.append("Usage: ");
		try {
			final File clsSource = new File(AppChooserState.class.getProtectionDomain().getCodeSource().getLocation().toURI());
			final String clsPath = clsSource.toString();
			if (clsPath.endsWith(".exe")) {
				mainInvocation = Paths.get(".").toAbsolutePath().relativize(clsSource.toPath()).toString();
			} else if (clsPath.endsWith(".jar")) {
				if (Platform.SYSTEM_IS_MAC && clsPath.contains(".app/")) {
					final Path containingApp = getContainingApp(clsSource.toPath());
					if (Objects.isNull(containingApp)) {
						mainInvocation = "java -jar " + Paths.get(".").toAbsolutePath().relativize(clsSource.toPath());
					} else {
						mainInvocation = "open " + Paths.get(".").toAbsolutePath().relativize(containingApp) + " --args";
					}
				} else {
					mainInvocation = "java -jar " + Paths.get(".").toAbsolutePath().relativize(clsSource.toPath());
				}
			} else {
				mainInvocation = "java -cp CLASSPATH " + Main.class.getName();
			}
		} catch (final URISyntaxException except) {
			LovelaceLogger.warning("Error computing usage message");
			LovelaceLogger.debug(except, "Stack trace: URI syntax exception");
			mainInvocation = "java -jar viewer-VERSION.jar";
		}
		builder.append(mainInvocation);
		if (usage.isGraphical()) {
			builder.append(" [-g|--gui] ");
		} else {
			builder.append(" -c|--cli "); // FIXME For apps with no GUI counterpart this is actually optional
		}
		builder.append(usage.getInvocation());
		for (final String option : usage.getSupportedOptions()) {
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
		builder.append(System.lineSeparator());
		return builder.toString();
	}

	public static void handleDroppedFiles(final OpenFilesEvent openFilesEvent) {
		final SPFrame topWindow = Stream.of(WindowList.getWindows(true, false)).filter(SPFrame.class::isInstance)
				.map(SPFrame.class::cast).reduce((first, second) -> second).orElse(null);
		if (!Objects.isNull(topWindow)) {
			for (final File file : openFilesEvent.getFiles()) {
				try {
					topWindow.acceptDroppedFile(file.toPath());
				} catch (final SPFormatException except) {
					JOptionPane.showMessageDialog(topWindow, except.getMessage(), "Strategic Primer Map Format Error",
							JOptionPane.ERROR_MESSAGE);
					LovelaceLogger.error(except.getMessage());
				} catch (final FileNotFoundException | NoSuchFileException except) {
					JOptionPane.showMessageDialog(topWindow, except.getMessage(), "File Not Found",
							JOptionPane.ERROR_MESSAGE);
					LovelaceLogger.error(except, "Dropped file not found");
				} catch (final IOException except) {
					JOptionPane.showMessageDialog(topWindow, except.getMessage(), "I/O Error", JOptionPane.ERROR_MESSAGE);
					LovelaceLogger.error("I/O error reading dropped file: %s", except.getMessage());
				} catch (final XMLStreamException except) {
					JOptionPane.showMessageDialog(topWindow, except.getMessage(), "Strategic Primer Map Format Error",
							JOptionPane.ERROR_MESSAGE);
					LovelaceLogger.error(except, "Malformed XML in %s", file);
				}
			}
		}
	}
}


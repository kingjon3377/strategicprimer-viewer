package drivers.common.cli;

import java.util.Map;
import java.util.HashMap;
import either.Either;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.List;
import java.util.Arrays;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

/**
 * A class to allow CLI drivers to present a menu of applets to the user.
 */
public class AppletChooser<AppletClass extends Applet> {
	@SafeVarargs
	public AppletChooser(ICLIHelper cli, AppletClass... applets) {
		this.cli = cli;
		Map<String, AppletClass> temp = new HashMap<String, AppletClass>();
		for (AppletClass applet : applets) {
			for (String command : applet.getCommands()) {
				if ("help".equals(command) || "quit".equals(command) ||
						"?".equals(command) || "exit".equals(command)) {
					throw new IllegalArgumentException("Applet with reserved command");
				} else if (temp.containsKey(command)) {
					throw new IllegalArgumentException(
						"Two applets with the same command");
				}
				temp.put(command, applet);
			}
		}
		commands = Collections.unmodifiableMap(temp);
		this.applets = Collections.unmodifiableList(Arrays.<AppletClass>asList(applets));
	}

	private final ICLIHelper cli;

	private final Map<String, AppletClass> commands;

	private final List<AppletClass> applets;

	private void usageMessage() {
		cli.println("The following commands are supported:");
		for (AppletClass applet : applets) {
			cli.print(String.join(", ", applet.getCommands().stream().toArray(String[]::new)));
			cli.println(applet.getDescription());
		}
		cli.println("help, ?: Print this list of commands.");
		cli.println("quit, exit: Exit the program.");
		cli.print("Any string that is the beginning of only one command is also ");
		cli.println("accepted for that command.");
	}

	/**
	 * Ask the user to choose an applet. If the user chooses an applet,
	 * return it (wrapped in {@link Either}). If the user chooses "quit" or
	 * "exit", or an EOF condition occurs, return false (wrapped in {@link
	 * Either}). If the user asks for the usage messge, print it and return
	 * true (wrapped in {@link Either}). If the user's input is ambiguous
	 * or does not match any applet, print the usage message and return null.
	 */
	@Nullable
	public Either<AppletClass, Boolean> chooseApplet() {
		String command = Optional.ofNullable(cli.inputString("Command:"))
			.map(String::toLowerCase).orElse(null);
		if (command == null) {
			return Either.<AppletClass, Boolean>right(false);
		} else {
			List<Map.Entry<String, AppletClass>> matches =
				commands.entrySet().stream().filter(e -> e.getKey().startsWith(command))
					.collect(Collectors.toList());
			if ("quit".startsWith(command) || "exit".startsWith(command)) {
				if (matches.isEmpty()) {
					return Either.<AppletClass, Boolean>right(false);
				} else {
					cli.println("That command was ambiguous between the following:");
					cli.println(String.join(", ",
						Stream.concat(Stream.of("quit", "exit")
									.filter(s -> s.startsWith(command)),
								matches.stream().map(Map.Entry::getKey))
							.toArray(String[]::new)));
					usageMessage();
					return null;
				}
			} else if ("?".equals(command)) {
				usageMessage();
				return Either.<AppletClass, Boolean>right(true);
			} else if ("help".startsWith(command)) {
				if (matches.isEmpty()) {
					usageMessage();
					return Either.<AppletClass, Boolean>right(true);
				} else {
					cli.println("That command was ambiguous between the following:");
					cli.print("help, ");
					cli.println(String.join(", ", matches.stream()
						.map(Map.Entry::getKey).toArray(String[]::new)));
					usageMessage();
					return null;
				}
			} else if (matches.isEmpty()) {
				cli.println("Unknown command.");
				usageMessage();
				return null;
			} else if (matches.size() > 1) {
				cli.println("That command was ambiguous between the following: ");
				cli.println(String.join(", ", matches.stream().map(Map.Entry::getKey).
					toArray(String[]::new)));
				usageMessage();
				return null;
			} else {
				return Either.<AppletClass, Boolean>left(matches.get(0).getValue());
			}
		}
	}
}

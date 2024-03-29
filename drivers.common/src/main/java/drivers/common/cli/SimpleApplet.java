package drivers.common.cli;

import java.util.List;

public class SimpleApplet implements Applet {
	public SimpleApplet(final Runnable invoke, final String description, final String... commands) {
		impl = invoke;
		this.description = description;
		this.commands = List.of(commands);
	}

	private final Runnable impl;

	private final String description;

	private final List<String> commands;

	@Override
	public final List<String> getCommands() {
		return commands;
	}

	@Override
	public final String getDescription() {
		return description;
	}

	@Override
	public final void invoke() {
		impl.run();
	}
}

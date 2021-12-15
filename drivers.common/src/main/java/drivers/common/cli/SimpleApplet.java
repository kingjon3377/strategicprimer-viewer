package drivers.common.cli;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SimpleApplet implements Applet {
	public SimpleApplet(Runnable invoke, String description, String... commands) {
		this.impl = invoke;
		this.description = description;
		this.commands = Collections.unmodifiableList(Arrays.asList(commands));
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

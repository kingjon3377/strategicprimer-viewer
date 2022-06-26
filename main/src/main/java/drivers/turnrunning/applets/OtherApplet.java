package drivers.turnrunning.applets;

import java.util.Collections;
import java.util.List;
import org.jetbrains.annotations.Nullable;

/* package */ class OtherApplet implements TurnApplet {
	private static final List<String> COMMANDS = Collections.singletonList("other");

	@Override
	public List<String> getCommands() {
		return COMMANDS;
	}

	@Override
	public String getDescription() {
		return "something no applet supports";
	}

	@Override
	public @Nullable String run() {
		return null;
	}
}

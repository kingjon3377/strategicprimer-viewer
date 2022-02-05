package drivers.turnrunning.applets;

import java.util.List;
import drivers.common.cli.Applet;

import org.jetbrains.annotations.Nullable;

public interface TurnApplet extends Applet {
	@Override
	List<String> getCommands();

	@Override
	String getDescription();

	@Nullable String run();

	@Override
	default void invoke() {
		run();
	}

	default String inHours(final int minutes) {
		if (minutes < 60) {
			return String.format("%d minutes", minutes);
		} else {
			return String.format("%d hours, %d minutes", minutes / 60, minutes % 60);
		}
	}
}

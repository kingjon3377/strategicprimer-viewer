package drivers.turnrunning.applets;

import drivers.common.cli.Applet;

import org.jspecify.annotations.Nullable;

public interface TurnApplet extends Applet {
	int MINS_PER_HOUR = 60;

	@Nullable
	String run();

	@Override
	default void invoke() {
		run();
	}

	default String inHours(final long minutes) {
		if (minutes < 0) {
			return "negative " + inHours(-minutes);
		} else if (minutes == 0) {
			return "no time";
		} else if (minutes == 1) {
			return "1 minute";
		} else if (minutes < MINS_PER_HOUR) {
			return "%d minutes".formatted(minutes);
		} else if (minutes < (2 * MINS_PER_HOUR)) {
			return "1 hour, " + inHours(minutes % MINS_PER_HOUR);
		} else if (minutes % MINS_PER_HOUR == 0) {
			return "%d hours".formatted(minutes / MINS_PER_HOUR);
		} else {
			return "%d hours, %s".formatted(minutes / MINS_PER_HOUR, inHours(minutes % MINS_PER_HOUR));
		}
	}
}

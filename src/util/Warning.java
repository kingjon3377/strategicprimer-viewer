package util;

import java.util.logging.Level;
import java.util.logging.Logger;

import controller.map.SPFormatException;

/**
 * A class to log warnings.
 * 
 * @author Jonathan Lovelace
 * 
 */
public final class Warning {
	/**
	 * An instance.
	 */
	public static final Warning INSTANCE = new Warning();

	/**
	 * An enumeration of possible states.
	 */
	public enum Action {
		/**
		 * Don't do anything with a warning.
		 */
		Ignore,
		/**
		 * Default: Log each warning, but let them pass.
		 */
		Warn,
		/**
		 * Treat warnings as errors: Throw them as runtime exceptions.
		 */
		Die;
	}

	/**
	 * How we should deal with warnings.
	 */
	private final Action state;

	/**
	 * Constructor.
	 * 
	 * @param action what action to take with each warning
	 */
	public Warning(final Action action) {
		state = action;
	}

	/**
	 * Constructor. Only warn on warnings.
	 */
	public Warning() {
		this(Action.Warn);
	}

	/**
	 * Log a warning, e.g. if a particular map-format construct is deprecated.
	 * 
	 * @param warning the warning
	 */
	public void warn(final Exception warning) {
		switch (state) {
		case Die:
			throw new FatalWarning(warning); // NOPMD
		case Warn:
			if (warning instanceof SPFormatException) {
				Logger.getLogger(
						warning.getStackTrace()[0].getClass().getName())
						.warning("Warning: " + warning.getMessage());
			} else {
				Logger.getLogger(
						warning.getStackTrace()[0].getClass().getName()).log(
						Level.WARNING, "Warning: ", warning);
			}
			break;
		case Ignore:
			break;
		default:
			throw new IllegalStateException(
					"Default case of an enum-switch that isn't missing any cases");
		}
	}

}

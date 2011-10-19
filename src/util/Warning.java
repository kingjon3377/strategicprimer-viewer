package util;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A class to log warnings.
 * @author Jonathan Lovelace
 *
 */
public final class Warning {
	/**
	 * Do not instantiate.
	 */
	private Warning() {
		// Do  not call.
	}
	/**
	 * Log a warning, e.g. if a particular map-format construct is deprecated.
	 * @param warning the warning 
	 */
	public static void warn(final Exception warning) {
		Logger.getLogger(warning.getStackTrace()[0].getClass().getName()).log(
				Level.WARNING, "Warning: ", warning);
	}

}

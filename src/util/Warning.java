package util;

import java.util.logging.Level;
import java.util.logging.Logger;

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
	 * Whether warnings should be fatal (i.e. the exception thrown) or merely logged.
	 */
	private boolean fatal = false;
	/**
	 * @return whether warnings are set to be fatal
	 */
	public boolean isFatal() {
		return fatal;
	}
	/**
	 * Set the "fatal" flag---but only if this isn't the global instance.
	 * @param die whether warnings should be fatal
	 */
	public void setFatal(final boolean die) {
		if (this == INSTANCE) {
			throw new IllegalArgumentException(
					"Don't call setFatal() on the singleton; construct your own instance");
		}
		fatal = die;
	}
	/**
	 * Log a warning, e.g. if a particular map-format construct is deprecated.
	 * 
	 * @param warning
	 *            the warning
	 */
	public void warn(final Exception warning) {
		if (fatal) {
			throw new FatalWarning(warning); // NOPMD
		} else {
			Logger.getLogger(warning.getStackTrace()[0].getClass().getName()).log(
					Level.WARNING, "Warning: ", warning);
		}
	}

}

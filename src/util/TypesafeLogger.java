package util;

import java.util.logging.Logger;

/**
 * A class to get Loggers and guarantee that they are not null, since the JDK
 * isn't annotated yet.
 *
 * @author Jonathan Lovelace
 *
 */
public final class TypesafeLogger {
	/**
	 * Do not instantiate.
	 */
	private TypesafeLogger() {
		// Do not instantiate.
	}

	/**
	 * @param type the class that will be calling the logger
	 * @return the logger produced by {@link Logger#getLogger(String)}.
	 */
	public static Logger getLogger(final Class<?> type) {
		final Logger retval = Logger.getLogger(type.getName());
		if (retval == null) {
			throw new IllegalStateException("Logger was null");
		} else {
			return retval;
		}
	}
}

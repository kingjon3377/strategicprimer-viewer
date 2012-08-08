package view.util;

/**
 * A class for a wrapper around System.exit().
 * 
 * @author Jonathan Lovelace
 */
public final class DriverQuit {
	/**
	 * Do not instantiate.
	 */
	private DriverQuit() {
		// Do nothing.
	}

	/**
	 * Quit. Note that this should not be called from a non-static context
	 * except by some CLI drivers.
	 * 
	 * @param code The exit code.
	 */
	public static void quit(final int code) {
		System.exit(code);
	}
}

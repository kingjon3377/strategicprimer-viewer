package view.util;

import java.io.PrintStream;

import util.NullCleaner;

/**
 * A class to get around Eclipse's insistence that System.out might be null.
 *
 * @author Jonathan Lovelace
 *
 */
public final class SystemOut {
	/**
	 * The singleton object.
	 */
	public static final PrintStream SYS_OUT = NullCleaner
			.assertNotNull(System.out);

	/**
	 * Constructor.
	 */
	private SystemOut() {
		// Do not instantiate.
	}

	/**
	 *
	 * @return a String representation of the object.
	 */
	@Override
	public String toString() {
		return "SystemOut";
	}
}

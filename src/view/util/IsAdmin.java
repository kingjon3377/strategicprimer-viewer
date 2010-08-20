package view.util;

/**
 * A class to store a constant saying whether we should expose admin interfaces
 * to the user or not. This is given its own class so I can commit it at "false"
 * and then change it to true and add it to .bzrignore.
 * 
 * @author Jonathan Lovelace
 * 
 */
public final class IsAdmin {
	/**
	 * Do not instantiate.
	 */
	private IsAdmin() {
		// Do nothing.
	}

	/**
	 * Should we expose admin interfaces to the user?
	 */
	public static final boolean IS_ADMIN = false;
}

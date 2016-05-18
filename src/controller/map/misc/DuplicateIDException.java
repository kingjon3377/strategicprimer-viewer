package controller.map.misc;

/**
 * An exception to warn about duplicate IDs. TODO: Tests should ensure that this is fired.
 */
public final class DuplicateIDException extends Exception {
	/**
	 * @param idNum the duplicate ID.
	 */
	public DuplicateIDException(final int idNum) {
		super("Duplicate ID #" + idNum);
	}
}

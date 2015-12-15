package controller.map.misc;

/**
 * An exception to warn about duplicate IDs.
 */
public final class DuplicateIDException extends Exception {
	/**
	 * @param idNum the duplicate ID.
	 */
	public DuplicateIDException(final int idNum) {
		super("Duplicate ID #" + idNum);
	}
}

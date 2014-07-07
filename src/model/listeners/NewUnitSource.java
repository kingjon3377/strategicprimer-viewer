package model.listeners;

/**
 * Something for NewUnitListeners to listen to.
 *
 * @author Jonathan Lovelace
 *
 */
public interface NewUnitSource {
	/**
	 * @param list a new listener to add
	 */
	void addNewUnitListener(NewUnitListener list);

	/**
	 * @param list a listener to remove
	 */
	void removeNewUnitListener(NewUnitListener list);
}

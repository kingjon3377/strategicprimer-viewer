package model.listeners;
/**
 * Something for NewUnitListeners to listen to.
 * @author Jonathan Lovelace
 *
 */
public interface NewUnitSource {
	/**
	 * @param list a new listener to add
	 */
	void addNewUnitListener(final NewUnitListener list);
	/**
	 * @param list a listener to remove
	 */
	void removeNewUnitListener(final NewUnitListener list);
}

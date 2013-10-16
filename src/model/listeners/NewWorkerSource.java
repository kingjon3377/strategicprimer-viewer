package model.listeners;

/**
 * Something for NewWorkerListeners to listen to.
 *
 * @author Jonathan Lovelace
 *
 */
public interface NewWorkerSource {
	/**
	 * @param list a new listener to add
	 */
	void addNewWorkerListener(final NewWorkerListener list);

	/**
	 * @param list a listener to remove
	 */
	void removeNewWorkerListener(final NewWorkerListener list);
}

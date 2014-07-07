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
	void addNewWorkerListener(NewWorkerListener list);

	/**
	 * @param list a listener to remove
	 */
	void removeNewWorkerListener(NewWorkerListener list);
}

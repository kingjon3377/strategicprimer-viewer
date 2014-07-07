package model.listeners;

/**
 * An interface for objects that can tell others when they've finished
 * something.
 *
 * @author Jonathan Lovelace
 */
public interface CompletionSource {
	/**
	 * @param list a listener to add
	 */
	void addCompletionListener(CompletionListener list);

	/**
	 * @param list a listener to remove
	 */
	void removeCompletionListener(CompletionListener list);
}

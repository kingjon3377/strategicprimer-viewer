package model.listeners;

/**
 * An interface for objects that want to start something when another object has
 * finished whatever it's doing.
 *
 * @author Jonathan Lovelace
 */
public interface CompletionListener {
	/**
	 * Stop waiting for the thing being listened to, because it's finished.
	 * @param result what was being waited on, or a signal value
	 */
	void stopWaitingOn(final Object result);
}

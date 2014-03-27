package model.listeners;

import java.util.EventListener;

/**
 * An interface for objects that want to start something when another object has
 * finished whatever it's doing.
 *
 * FIXME: Too many uses of this should be replaced by their own set of
 * listeners; in fact, the 'result' below should be a boolean for "start afresh"
 * vs "start incremental.
 *
 * @author Jonathan Lovelace
 */
public interface CompletionListener extends EventListener {
	/**
	 * Stop waiting for the thing being listened to, because it's finished.
	 * 
	 * @param end
	 *            whether a list should be scrolled to the end or (if false)
	 *            reset to the beginning
	 */
	void stopWaitingOn(final boolean end);
}

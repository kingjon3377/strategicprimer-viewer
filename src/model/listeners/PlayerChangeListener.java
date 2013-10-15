package model.listeners;

import model.map.Player;

import org.eclipse.jdt.annotation.Nullable;

/**
 * An interface for things that want to be called when the current player changes.
 * @author Jonathan Lovelace
 *
 */
public interface PlayerChangeListener {
	/**
	 * Called when the current player changes.
	 * @param old the previous current player
	 * @param newPlayer the new current player
	 */
	void playerChanged(@Nullable final Player old, final Player newPlayer);
}

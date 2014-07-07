package model.listeners;

import java.util.EventListener;

import model.map.Player;

import org.eclipse.jdt.annotation.Nullable;

/**
 * An interface for things that want to be called when the current player
 * changes.
 *
 * @author Jonathan Lovelace
 *
 */
public interface PlayerChangeListener extends EventListener {
	/**
	 * Called when the current player changes.
	 *
	 * @param old the previous current player
	 * @param newPlayer the new current player
	 */
	void playerChanged(@Nullable  Player old,  Player newPlayer);
}

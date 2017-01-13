package controller.map.misc;

import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import model.listeners.PlayerChangeListener;
import model.listeners.PlayerChangeSource;
import model.map.Player;
import model.map.PlayerCollection;
import model.misc.IDriverModel;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A class to respond to "change current player" menu items.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public class PlayerChangeMenuListener implements ActionListener, PlayerChangeSource {
	/**
	 * The list of player-change listeners.
	 */
	private final Collection<PlayerChangeListener> playerChangeListeners =
			new ArrayList<>();
	/**
	 * The driver model to operate on.
	 */
	private final IDriverModel model;
	/**
	 * Constructor.
	 * @param dmodel the driver model to operate on
	 */
	public PlayerChangeMenuListener(final IDriverModel dmodel) {
		model = dmodel;
	}
	/**
	 * Add a listener.
	 * @param list a listener to add
	 */
	@Override
	public void addPlayerChangeListener(final PlayerChangeListener list) {
		playerChangeListeners.add(list);
	}

	/**
	 * Remove a listener.
	 * @param list a listener to remove
	 */
	@Override
	public void removePlayerChangeListener(final PlayerChangeListener list) {
		playerChangeListeners.remove(list);
	}

	/**
	 * This class ignores the event's actual command, and does what it does
	 * unconditionally.
	 *
	 * @param evt ignored
	 */
	@Override
	public void actionPerformed(final ActionEvent evt) {
		final Player currentPlayer = model.getMap().getCurrentPlayer();
		final Player retval = (Player) JOptionPane.showInputDialog(
				getContainingFrame(eventSource(evt.getSource())),
				"Player to view:", "Choose New Player",
				JOptionPane.PLAIN_MESSAGE, null,
				playersAsArray(model.getMap().players()),
				currentPlayer);
		if (retval != null) {
			for (final PlayerChangeListener list : playerChangeListeners) {
				list.playerChanged(currentPlayer, retval);
			}
		}
	}
	/**
	 * Return the given object if it is a component, and null otherwise.
	 * @param obj an object
	 * @return it if it's a component, or null
	 */
	@SuppressWarnings("ReturnOfNull")
	@Nullable
	private static Component eventSource(@Nullable final Object obj) {
		if (obj instanceof Component) {
			return (Component) obj;
		} else {
			return null;
		}
	}
	/**
	 * Convert a collection of players to an array.
	 * @param players a collection of players
	 * @return the players as an array
	 */
	private static Player[] playersAsArray(final Iterable<Player> players) {
		if (players instanceof PlayerCollection) {
			return ((PlayerCollection) players).asArray();
		} else {
			final List<Player> list = StreamSupport.stream(players.spliterator(), false)
											  .collect(Collectors.toList());
			return list.toArray(new Player[list.size()]);
		}
	}
	/**
	 * The containing frame, if any, of a component.
	 * @param component a component
	 * @return the frame containing it, if any
	 */
	@Nullable
	private static Frame getContainingFrame(@Nullable final Component component) {
		Component temp = component;
		while (temp != null) {
			if (temp instanceof Frame) {
				return (Frame) temp;
			} else if (temp instanceof JPopupMenu) {
				temp = ((JPopupMenu) temp).getInvoker();
			} else {
				temp = temp.getParent();
			}
		}
		return null;
	}
}

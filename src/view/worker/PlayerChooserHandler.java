package view.worker;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import org.eclipse.jdt.annotation.Nullable;

import model.listeners.PlayerChangeListener;
import model.listeners.PlayerChangeSource;
import model.map.IMutablePlayerCollection;
import model.map.Player;
import model.map.PlayerCollection;
import model.misc.IDriverModel;
import util.NullCleaner;

/**
 * Listens for the 'Change current player' menu item and lets the player choose
 * a new player to look at, updating listeners with the new player.
 *
 * This is part of the Strategic Primer assistive programs suite developed by
 * Jonathan Lovelace.
 *
 * Copyright (C) 2013-2015 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of version 3 of the GNU General Public License as published by the
 * Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 *
 */
public final class PlayerChooserHandler implements ActionListener,
		PlayerChangeSource {
	/**
	 * The menu item we listen for.
	 */
	public static final String MENU_ITEM = "Change current player";
	/**
	 * The list of listeners.
	 */
	private final List<PlayerChangeListener> listeners = new ArrayList<>();

	/**
	 * The frame to attach the dialog to.
	 */
	private final Component parent;
	/**
	 * The driver model to get the players and current player from.
	 */
	private final IDriverModel model;
	/**
	 * The current player.
	 */
	private Player player;

	/**
	 * Constructor.
	 *
	 * @param outer the component to attach the dialog to
	 * @param dmodel the driver model
	 */
	public PlayerChooserHandler(final Component outer, final IDriverModel dmodel) {
		parent = outer;
		model = dmodel;
		player = dmodel.getMap().getCurrentPlayer();
	}

	/**
	 * @param list a listener to add
	 */
	@Override
	public void addPlayerChangeListener(final PlayerChangeListener list) {
		listeners.add(list);
	}

	/**
	 * @param list a listener to remove
	 */
	@Override
	public void removePlayerChangeListener(final PlayerChangeListener list) {
		listeners.remove(list);
	}

	/**
	 * Handle menu item.
	 *
	 * @param evt the event to handle.
	 */
	@Override
	public void actionPerformed(@Nullable final ActionEvent evt) {
		if (evt != null && MENU_ITEM.equals(evt.getActionCommand())) {
			final Player retval = (Player) JOptionPane.showInputDialog(parent,
					"Player to view:", "Choose New Player",
					JOptionPane.PLAIN_MESSAGE, null, playersAsArray(model
							.getMap().players()), player);
			if (retval != null) {
				for (final PlayerChangeListener list : listeners) {
					list.playerChanged(player, retval);
				}
				player = retval;
			}
		}
	}
	/**
	 * Make listeners reload.
	 */
	public void reload() {
		for (final PlayerChangeListener listener : listeners) {
			listener.playerChanged(player, player);
		}
	}
	/**
	 * @param players a collection of players
	 * @return the players as an array
	 */
	private static Player[] playersAsArray(final Iterable<Player> players) {
		if (players instanceof PlayerCollection) {
			return ((PlayerCollection) players).asArray(); // NOPMD
		} else {
			final List<Player> list = new ArrayList<>();
			for (final Player player : players) {
				list.add(player);
			}
			return NullCleaner
					.assertNotNull(list.toArray(new Player[list.size()]));
		}
	}
	/**
	 * Should only be called once per object lifetime. Notify all listeners, as
	 * if the current player had changed from null to its current value.
	 */
	public void notifyListeners() {
		for (final PlayerChangeListener list : listeners) {
			list.playerChanged(null, player);
		}
	}
	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "PlayerChooserHandler";
	}
}

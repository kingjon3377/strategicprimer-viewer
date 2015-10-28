package model.exploration;

import javax.swing.DefaultListModel;

import model.listeners.MapChangeListener;
import model.map.Player;

/**
 * A list model for players in the exploration GUI.
 *
 * This is part of the Strategic Primer assistive programs suite developed by
 * Jonathan Lovelace.
 *
 * Copyright (C) 2013-2013 Jonathan Lovelace
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
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * @author Jonathan Lovelace
 *
 */
public class PlayerListModel extends DefaultListModel<Player> implements
		MapChangeListener {
	/**
	 * The exploration model.
	 */
	private final IExplorationModel model;

	/**
	 * @param emodel the exploration model
	 */
	public PlayerListModel(final ExplorationModel emodel) {
		model = emodel;
		for (final Player player : model.getPlayerChoices()) {
			addElement(player);
		}
	}

	/**
	 * Handle notification that a new map was loaded.
	 */
	@Override
	public void mapChanged() {
		clear();
		for (final Player player : model.getPlayerChoices()) {
			addElement(player);
		}
	}

}

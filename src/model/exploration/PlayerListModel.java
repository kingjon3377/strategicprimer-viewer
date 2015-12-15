package model.exploration;

import javax.swing.DefaultListModel;

import org.eclipse.jdt.annotation.NonNull;

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
 * this program. If not, see <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 *
 */
public final class PlayerListModel extends DefaultListModel<@NonNull Player> implements
		MapChangeListener {
	/**
	 * The exploration model.
	 */
	private final IExplorationModel model;

	/**
	 * @param emodel the exploration model
	 */
	public PlayerListModel(final IExplorationModel emodel) {
		model = emodel;
		model.getPlayerChoices().forEach(this::addElement);
	}

	/**
	 * Handle notification that a new map was loaded.
	 */
	@Override
	public void mapChanged() {
		clear();
		model.getPlayerChoices().forEach(this::addElement);
	}

}

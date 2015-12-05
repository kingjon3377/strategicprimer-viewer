package model.exploration;

import java.util.List;

import javax.swing.DefaultListModel;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import model.listeners.PlayerChangeListener;
import model.map.Player;
import model.map.fixtures.mobile.IUnit;

/**
 * A unit list model for the exploration GUI.
 *
 * This is part of the Strategic Primer assistive programs suite developed by
 * Jonathan Lovelace.
 *
 * Copyright (C) 2013-2014 Jonathan Lovelace
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
public class ExplorationUnitListModel extends DefaultListModel<@NonNull IUnit> implements
		PlayerChangeListener {
	/**
	 * The exploration model to work from.
	 */
	private final IExplorationModel model;
	/**
	 * Constructor.
	 *
	 * @param emodel the exploration model, so we can select the unit the user
	 *        selects
	 */
	public ExplorationUnitListModel(final IExplorationModel emodel) {
		model = emodel;
	}

	/**
	 * Called when the current player has changed.
	 *
	 * @param old the previous current player
	 * @param newPlayer the new current player
	 */
	@Override
	public void playerChanged(@Nullable final Player old, final Player newPlayer) {
		clear();
		final List<IUnit> units = model.getUnits(newPlayer);
		for (final IUnit unit : units) {
			addElement(unit);
		}
	}
}

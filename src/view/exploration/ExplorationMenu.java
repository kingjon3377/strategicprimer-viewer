package view.exploration;

import javax.swing.JFrame;

import com.bric.window.WindowMenu;

import controller.map.misc.IOHandler;
import model.exploration.IExplorationModel;
import view.util.SPMenu;
import view.worker.PlayerChooserHandler;

/**
 * Menus for the exploration GUI.
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
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * @author Jonathan Lovelace
 *
 */
public class ExplorationMenu extends SPMenu {
	/**
	 * Constructor.
	 *
	 * @param ioh the I/O handler to handle I/O related items
	 * @param model the exploration model
	 * @param parent the window this is to be attached to, which should close on
	 *        "Close".
	 */
	public ExplorationMenu(final IOHandler ioh,
			final IExplorationModel model, final JFrame parent) {
		add(createFileMenu(ioh, parent, model));
		addDisabled(createMapMenu(parent, model));
		addDisabled(createEditMenu(new PlayerChooserHandler(parent, model)));
		add(new WindowMenu(parent));
	}
}

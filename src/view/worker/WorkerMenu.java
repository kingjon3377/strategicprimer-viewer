package view.worker;

import javax.swing.JFrame;

import com.bric.window.WindowMenu;

import controller.map.misc.IOHandler;
import model.misc.IDriverModel;
import view.util.SPMenu;

/**
 * A set of menus for the worker GUI.
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
public class WorkerMenu extends SPMenu {
	/**
	 * Constructor.
	 *
	 * @param handler the I/O handler to handle I/O related items
	 * @param parent the window this is to be attached to, which should close on
	 *        "Close".
	 * @param pch a handler to listen to the 'change player' menu item.
	 * @param model the current driver model
	 */
	public WorkerMenu(final IOHandler handler, final JFrame parent,
			final PlayerChooserHandler pch, final IDriverModel model) {
		add(createFileMenu(handler, parent, model));
		addDisabled(createMapMenu(parent, model));
		add(createEditMenu(pch));
		add(new WindowMenu(parent));
	}
}

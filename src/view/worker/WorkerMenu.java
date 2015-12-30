package view.worker;

import com.bric.window.WindowMenu;
import controller.map.misc.IOHandler;
import javax.swing.JFrame;
import model.misc.IDriverModel;
import view.util.SPMenu;

/**
 * A set of menus for the worker GUI.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2013-2015 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program. If not, see
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public final class WorkerMenu extends SPMenu {
	/**
	 * Constructor.
	 *
	 * @param handler the I/O handler to handle I/O related items
	 * @param parent  the window this is to be attached to, which should close on
	 *                "Close".
	 * @param pch     a handler to listen to the 'change player' menu item.
	 * @param model   the current driver model
	 * @param teh     a handler to listen for "expand all" etc.
	 */
	public WorkerMenu(final IOHandler handler, final JFrame parent,
	                  final IDriverModel model) {
		add(createFileMenu(handler, parent, model));
		addDisabled(createMapMenu(handler, model));
		add(createViewMenu(handler));
		add(new WindowMenu(parent));
	}
}

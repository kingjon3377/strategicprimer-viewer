package view.exploration;

import com.bric.window.WindowMenu;
import controller.map.misc.IOHandler;
import javax.swing.JFrame;
import model.exploration.IExplorationModel;
import view.util.SPMenu;
import view.worker.PlayerChooserHandler;

/**
 * Menus for the exploration GUI.
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
public final class ExplorationMenu extends SPMenu {
	/**
	 * Constructor.
	 *
	 * @param ioh    the I/O handler to handle I/O related items
	 * @param model  the exploration model
	 * @param parent the window this is to be attached to, which should close on "Close".
	 */
	public ExplorationMenu(final IOHandler ioh,
	                       final IExplorationModel model, final JFrame parent) {
		add(createFileMenu(ioh, parent, model));
		addDisabled(createMapMenu(ioh, parent, model));
		addDisabled(createViewMenu(ioh, new PlayerChooserHandler(parent, model), ioh));
		add(new WindowMenu(parent));
	}
}

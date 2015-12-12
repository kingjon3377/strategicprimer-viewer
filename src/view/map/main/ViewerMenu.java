package view.map.main;

import javax.swing.JFrame;

import com.bric.window.WindowMenu;

import controller.map.misc.IOHandler;
import model.viewer.IViewerModel;
import view.util.SPMenu;
import view.worker.PlayerChooserHandler;

/**
 * A class encapsulating the menus.
 *
 * This is part of the Strategic Primer assistive programs suite developed by
 * Jonathan Lovelace.
 *
 * Copyright (C) 2015-2015 Jonathan Lovelace
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
public class ViewerMenu extends SPMenu {
	/**
	 * Constructor.
	 *
	 * @param handler the I/O handler to handle I/O related items
	 * @param parent the frame we'll be attached to
	 * @param model the map model
	 */
	public ViewerMenu(final IOHandler handler, final JFrame parent,
			final IViewerModel model) {
		add(createFileMenu(handler, parent, model));
		add(createMapMenu(parent, model));
		addDisabled(createViewMenu(new PlayerChooserHandler(parent, model), handler));
		add(new WindowMenu(parent));
	}

	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "SPMenu";
	}
}

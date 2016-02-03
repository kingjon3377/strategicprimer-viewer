package view.map.main;

import com.bric.window.WindowMenu;
import java.awt.event.ActionListener;
import javax.swing.JFrame;
import model.misc.IDriverModel;
import view.util.SPMenu;

/**
 * A class encapsulating the menus.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2015-2015 Jonathan Lovelace
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
public final class ViewerMenu extends SPMenu {
	/**
	 * Constructor.
	 *
	 * @param handler the I/O handler to handle I/O related items
	 * @param parent  the frame we'll be attached to
	 * @param model   the map model
	 */
	public ViewerMenu(final ActionListener handler, final JFrame parent,
	                  final IDriverModel model) {
		add(createFileMenu(handler, model));
		add(createMapMenu(handler, model));
		addDisabled(createViewMenu(handler));
		add(new WindowMenu(parent));
	}

	/**
	 * @return a String representation of the object
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "SPMenu";
	}
}

package view.map.main;

import model.misc.IDriverModel;

/**
 * An interface for a UI representing a map.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2011-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public interface MapGUI {
	/**
	 * The driver model the GUI represents. TODO: should this be IViewerModel?
	 * @return the model encapsulating the map, secondary map, etc.
	 */
	IDriverModel getMapModel();
}

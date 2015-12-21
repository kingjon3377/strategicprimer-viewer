package view.map.main;

import model.viewer.IViewerModel;
import view.util.BorderedPanel;

import javax.swing.*;

/**
 * A panel to contain the map GUI and add scrollbars to it.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2013-2013 Jonathan Lovelace
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
public final class MapScrollPanel extends BorderedPanel {
	/**
	 * Constructor.
	 *
	 * @param map       the viewer model
	 * @param component the map component
	 */
	public MapScrollPanel(final IViewerModel map, final JComponent component) {
		super(component, null, null, null, null);
		final ScrollListener scrollListener = new ScrollListener(map, this);
		map.addGraphicalParamsListener(scrollListener);
		map.addMapChangeListener(scrollListener);
	}
}

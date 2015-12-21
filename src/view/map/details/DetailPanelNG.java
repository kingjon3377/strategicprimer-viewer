package view.map.details;

import javax.swing.*;
import model.listeners.SelectionChangeListener;
import model.listeners.VersionChangeListener;
import model.map.Point;
import model.misc.IDriverModel;
import org.eclipse.jdt.annotation.Nullable;
import view.map.key.KeyPanel;
import view.util.BorderedPanel;

/**
 * A panel to show the details of a tile, using a tree rather than subpanels with chits
 * for its fixtures.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2012-2015 Jonathan Lovelace
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
public final class DetailPanelNG extends JSplitPane implements VersionChangeListener,
		                                                               SelectionChangeListener {
	/**
	 * The "weight" to give the divider. We want the 'key' to get very little of any
	 * extra
	 * space, but to get some.
	 */
	private static final double DIVIDER_LOCATION = 0.9;
	/**
	 * The 'key' panel, showing what each tile color represents.
	 */
	private final KeyPanel keyPanel;
	/**
	 * The list of fixtures on the current tile.
	 */
	private final FixtureList fixList;
	/**
	 * The 'header' label above the list.
	 */
	private final JLabel header = new JLabel(
			                                        "<html><body><p>Contents of the tile" +
					                                        " at (-1, -1)" +
					                                        ":</p></body></html>");

	/**
	 * Constructor.
	 *
	 * @param version the (initial) map version
	 * @param model   the driver model; needed for the fixture list
	 */
	public DetailPanelNG(final int version, final IDriverModel model) {
		super(HORIZONTAL_SPLIT, true);

		fixList = new FixtureList(this, model, model.getMap().players());
		final BorderedPanel listPanel = new BorderedPanel(new JScrollPane(
				                                                                 fixList),
				                                                 header, null, null,
				                                                 null);

		keyPanel = new KeyPanel(version);
		setLeftComponent(listPanel);
		setRightComponent(keyPanel);
		setResizeWeight(DIVIDER_LOCATION);
		setDividerLocation(DIVIDER_LOCATION);
	}

	/**
	 * @param old        passed to key panel
	 * @param newVersion passed to key panel
	 */
	@Override
	public void changeVersion(final int old, final int newVersion) {
		keyPanel.changeVersion(old, newVersion);
	}

	/**
	 * @param old      passed to fixture list
	 * @param newPoint passed to fixture list and shown on the header
	 */
	@Override
	public void selectedPointChanged(@Nullable final Point old,
	                                 final Point newPoint) {
		fixList.selectedPointChanged(old, newPoint);
		header.setText("<html><body><p>Contents of the tile at "
				               + newPoint.toString() + ":</p></body></html>");
	}
}

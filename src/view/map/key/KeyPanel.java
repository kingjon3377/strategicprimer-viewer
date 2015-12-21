package view.map.key;

import java.awt.Dimension;
import java.awt.GridLayout;
import javax.swing.JPanel;
import model.listeners.VersionChangeListener;
import model.map.TileType;

/**
 * Provides a visual "key" to the various terrain types.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2011-2013 Jonathan Lovelace
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
public final class KeyPanel extends JPanel implements VersionChangeListener {
	/**
	 * Constructor.
	 *
	 * @param version the map version
	 */
	public KeyPanel(final int version) {
		super(new GridLayout(0, 4));
		updateForVersion(version);
		setMinimumSize(new Dimension(new KeyElement(version,
				                                           TileType.NotVisible)
				                             .getMinimumSize().width * 4,
				                            getMinimumSize().height));
		setPreferredSize(getMinimumSize());
	}

	/**
	 * Update the key for a new map version.
	 *
	 * @param version the map version
	 */
	private void updateForVersion(final int version) {
		removeAll();
		for (final TileType type : TileType.valuesForVersion(version)) {
			add(new KeyElement(version, type)); // NOPMD
		}
	}

	/**
	 * @param old        the previous map version
	 * @param newVersion the new map version
	 */
	@Override
	public void changeVersion(final int old, final int newVersion) {
		updateForVersion(newVersion);
	}
}

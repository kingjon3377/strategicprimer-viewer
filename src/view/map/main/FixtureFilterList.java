package view.map.main;

import java.awt.Component;
import java.util.HashMap;
import java.util.Map;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;

import org.eclipse.jdt.annotation.Nullable;

import model.map.TileFixture;
import model.viewer.FixtureFilterListModel;
import model.viewer.ZOrderFilter;
import util.NullCleaner;

/**
 * A list to let the user select which fixtures ought to be searched.
 *
 * This is part of the Strategic Primer assistive programs suite developed by
 * Jonathan Lovelace.
 *
 * Copyright (C) 2013-2014 Jonathan Lovelace
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
public class FixtureFilterList extends JList<Class<? extends TileFixture>>
		implements ZOrderFilter, ListCellRenderer<Class<? extends TileFixture>> {
	/**
	 * The renderer that does most of the work.
	 */
	private final ListCellRenderer<Object> lcr = new DefaultListCellRenderer();

	/**
	 * A mapping from classes of fixtures to their plurals.
	 */
	private final Map<Class<? extends TileFixture>, String> plurals;
	/**
	 * The selection model.
	 */
	private final ListSelectionModel lsm;
	/**
	 * The data model.
	 */
	private final FixtureFilterListModel model;

	/**
	 * Constructor.
	 */
	public FixtureFilterList() {
		plurals = new HashMap<>();
		model = new FixtureFilterListModel();
		setModel(model);
		lsm = NullCleaner.assertNotNull(getSelectionModel());
		lsm.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		setCellRenderer(this);
	}

	/**
	 * @param fix a fixture
	 * @return whether it should be searched
	 */
	@Override
	public boolean shouldDisplay(final TileFixture fix) {
		final Class<? extends TileFixture> cls = fix.getClass();
		if (cls == null) {
			return false; // NOPMD
		} else if (plurals.containsKey(cls)) {
			return lsm.isSelectedIndex(model.indexOf(cls)); // NOPMD
		} else {
			model.add(cls);
			plurals.put(cls, fix.plural());
			final int size = model.getSize();
			lsm.addSelectionInterval(size - 1, size - 1);
			return true;
		}
	}

	/**
	 *
	 * @param list this
	 * @param value the value being rendered
	 * @param index its index
	 * @param isSelected whether or not it's selected
	 * @param cellHasFocus whether or not it has the focus
	 * @return the rendered widget
	 */
	@Override
	public Component getListCellRendererComponent(
			@Nullable final JList<? extends Class<? extends TileFixture>> list,
			@Nullable final Class<? extends TileFixture> value, final int index,
			final boolean isSelected, final boolean cellHasFocus) {
		if (list == null) {
			throw new IllegalArgumentException("Asked to render null list");
		}
		final Component retval = lcr.getListCellRendererComponent(list, value,
				index, isSelected, cellHasFocus);
		if (retval instanceof JLabel) {
			((JLabel) retval).setText(plurals.get(value));
		} else if (retval == null) {
			throw new IllegalStateException("Default produced null");
		}
		return retval;
	}

}

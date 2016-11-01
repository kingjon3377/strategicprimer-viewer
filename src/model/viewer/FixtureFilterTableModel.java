package model.viewer;

import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import model.map.TileFixture;
import model.map.fixtures.Ground;
import model.map.fixtures.resources.Grove;
import model.map.fixtures.resources.Meadow;
import util.Reorderable;

/**
 * A class to allow the Z-order of fixtures to be represented as a table.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public class FixtureFilterTableModel extends AbstractTableModel
		implements Reorderable, ZOrderFilter {
	/**
	 * Constructor. Initialize with data that the default algorithm won't handle properly.
	 */
	public FixtureFilterTableModel() {
		list.add(new FixtureMatcher(fix -> fix instanceof Ground &&
												   ((Ground) fix).isExposed(),
										   "Ground (exposed)"));
		list.add(new FixtureMatcher(fix -> fix instanceof Ground &&
												   !((Ground) fix).isExposed(),
										   "Ground"));
		list.add(new FixtureMatcher(fix -> fix instanceof Grove &&
												   ((Grove) fix).isOrchard(),
										   "Orchards"));
		list.add(new FixtureMatcher(fix -> fix instanceof Grove &&
												   !((Grove) fix).isOrchard(),
										   "Groves"));
		list.add(new FixtureMatcher(fix -> fix instanceof Meadow &&
												   ((Meadow) fix).isField(), "Fields"));
		list.add(new FixtureMatcher(fix -> fix instanceof Meadow &&
												   !((Meadow) fix).isField(), "Meadows"));
	}
	/**
	 * The backing collection.
	 */
	private final List<FixtureMatcher> list = new ArrayList<>();
	/**
	 * @return the number of rows in the model
	 */
	@Override
	public int getRowCount() {
		return list.size();
	}

	/**
	 * @return the number of columns in the model
	 */
	@Override
	public int getColumnCount() {
		return 2;
	}

	/**
	 * @param rowIndex    the row whose value is to be queried
	 * @param columnIndex the column whose value is to be queried
	 * @return the value Object at the specified cell
	 */
	@Override
	public Object getValueAt(final int rowIndex, final int columnIndex) {
		final FixtureMatcher matcher = list.get(rowIndex);
		if (columnIndex == 0) {
			return Boolean.valueOf(matcher.isDisplayed());
		} else if (columnIndex == 1) {
			return matcher.getDescription();
		} else {
			throw new IllegalArgumentException("Only two columns");
		}
	}
	/**
	 * @param column the column being queried
	 * @return the name of that column
	 */
	@Override
	public String getColumnName(final int column) {
		if (column == 0) {
			return "Visible";
		} else if (column == 1) {
			return "Category";
		} else {
			return super.getColumnName(column);
		}
	}
	/**
	 * @param columnIndex the column being queried
	 * @return the class of the data in that column
	 */
	@Override
	public Class<?> getColumnClass(final int columnIndex) {
		if (columnIndex == 0) {
			return Boolean.class;
		} else if (columnIndex == 1) {
			return String.class;
		} else {
			return Object.class;
		}
	}
	/**
	 * @param rowIndex the row being queried
	 * @param columnIndex the column being queried
	 * @return whether that cell is editable
	 */
	@Override
	public boolean isCellEditable(final int rowIndex, final int columnIndex) {
		return columnIndex == 0;
	}
	/**
	 * @param aValue value assign to a cell
	 * @param rowIndex the row of the cell
	 * @param columnIndex the column of the cell
	 */
	@Override
	public void setValueAt(final Object aValue, final int rowIndex, final int columnIndex) {
		if (columnIndex == 0 && aValue instanceof Boolean) {
			list.get(rowIndex).setDisplayed(((Boolean) aValue).booleanValue());
			fireTableCellUpdated(rowIndex, columnIndex);
		}
	}
	/**
	 * Move a row of a list or table from one position to another.
	 * @param fromIndex the index to remove from
	 * @param toIndex the index to move to (its index *before* removing the old!)
	 */
	@Override
	public void reorder(final int fromIndex, final int toIndex) {
		if (fromIndex != toIndex) {
			if (fromIndex > toIndex) {
				list.add(toIndex, list.remove(fromIndex));
			} else {
				list.add(toIndex - 1, list.remove(fromIndex));
			}
			fireTableRowsDeleted(fromIndex, fromIndex);
			fireTableRowsInserted(toIndex, toIndex);
		}
	}
	/**
	 * @param fix a fixture
	 * @return whether it should be displayed
	 */
	@Override
	public boolean shouldDisplay(final TileFixture fix) {
		for (final FixtureMatcher matcher : list) {
			if (matcher.matches(fix)) {
				return matcher.isDisplayed();
			}
		}
		final Class<? extends TileFixture> cls = fix.getClass();
		if (cls == null) {
			return false;
		} else {
			list.add(new FixtureMatcher(cls::isInstance, fix.plural()));
			final int size = list.size();
			fireTableRowsInserted(size - 1, size - 1);
			return true;
		}
	}
}

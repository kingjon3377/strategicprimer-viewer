package model.viewer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import model.map.TileFixture;
import model.map.fixtures.Ground;
import model.map.fixtures.RiverFixture;
import model.map.fixtures.TextFixture;
import model.map.fixtures.explorable.AdventureFixture;
import model.map.fixtures.explorable.Battlefield;
import model.map.fixtures.explorable.Cave;
import model.map.fixtures.explorable.Portal;
import model.map.fixtures.mobile.Animal;
import model.map.fixtures.mobile.Centaur;
import model.map.fixtures.mobile.Djinn;
import model.map.fixtures.mobile.Dragon;
import model.map.fixtures.mobile.Fairy;
import model.map.fixtures.mobile.Giant;
import model.map.fixtures.mobile.Griffin;
import model.map.fixtures.mobile.Minotaur;
import model.map.fixtures.mobile.Ogre;
import model.map.fixtures.mobile.Phoenix;
import model.map.fixtures.mobile.Simurgh;
import model.map.fixtures.mobile.Sphinx;
import model.map.fixtures.mobile.Troll;
import model.map.fixtures.mobile.Unit;
import model.map.fixtures.resources.CacheFixture;
import model.map.fixtures.resources.Grove;
import model.map.fixtures.resources.Meadow;
import model.map.fixtures.resources.Mine;
import model.map.fixtures.resources.MineralVein;
import model.map.fixtures.resources.Shrub;
import model.map.fixtures.resources.StoneDeposit;
import model.map.fixtures.terrain.Forest;
import model.map.fixtures.terrain.Hill;
import model.map.fixtures.terrain.Mountain;
import model.map.fixtures.terrain.Oasis;
import model.map.fixtures.terrain.Sandbar;
import model.map.fixtures.towns.AbstractTown;
import model.map.fixtures.towns.Fortress;
import model.map.fixtures.towns.Village;
import util.Reorderable;

import static model.viewer.FixtureMatcher.simpleMatcher;

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
		implements Reorderable, ZOrderFilter, Iterable<FixtureMatcher> {
	/**
	 * Constructor. Initialize with data that the default algorithm won't handle properly.
	 */
	public FixtureFilterTableModel() {
		list.add(new FixtureMatcher(Unit.class::isInstance, "Units"));
		list.add(new FixtureMatcher(Fortress.class::isInstance, "Fortresses"));
		// TODO: Towns should be broken up by kind or size, and maybe by status or owner
		list.add(new FixtureMatcher(AbstractTown.class::isInstance,
										   "Cities, Towns, and Fortifications"));
		// TODO: Village through Centaur were all 45, so their ordering happened by chance
		list.add(new FixtureMatcher(Village.class::isInstance, "Villages"));
		list.add(new FixtureMatcher(Troll.class::isInstance, "Trolls"));
		list.add(new FixtureMatcher(Sphinx.class::isInstance, "Sphinxes"));
		list.add(new FixtureMatcher(Simurgh.class::isInstance, "Simurghs"));
		list.add(new FixtureMatcher(Phoenix.class::isInstance, "Phoenixes"));
		list.add(new FixtureMatcher(Ogre.class::isInstance, "Ogres"));
		list.add(new FixtureMatcher(Minotaur.class::isInstance, "Minotaurs"));
		list.add(new FixtureMatcher(Mine.class::isInstance, "Mine"));
		list.add(new FixtureMatcher(Griffin.class::isInstance, "Griffins"));
		list.add(new FixtureMatcher(Djinn.class::isInstance, "Djinni"));
		list.add(new FixtureMatcher(Centaur.class::isInstance, "Centaurs"));
		// TODO: StoneDeposit through Animal were all 40; they too should be reviewed
		list.add(new FixtureMatcher(StoneDeposit.class::isInstance, "Stone Deposits"));
		list.add(new FixtureMatcher(MineralVein.class::isInstance, "Mineral Veins"));
		list.add(new FixtureMatcher(Giant.class::isInstance, "Giants"));
		list.add(new FixtureMatcher(Fairy.class::isInstance, "Fairies"));
		list.add(new FixtureMatcher(Dragon.class::isInstance, "Dragons"));
		list.add(new FixtureMatcher(Cave.class::isInstance, "Caves"));
		list.add(new FixtureMatcher(Battlefield.class::isInstance, "Battlefields"));
		list.add(new FixtureMatcher(Animal.class::isInstance, "Animals"));

		list.add(simpleMatcher(Grove.class, Grove::isOrchard, "Orchards"));
		list.add(simpleMatcher(Grove.class, fix -> !fix.isOrchard(), "Groves"));

		// TODO: Since rivers are usually handled specially, should this really be included?
		list.add(new FixtureMatcher(RiverFixture.class::isInstance, "Rivers"));

		// TODO: TextFixture through AdventureFixture were all 25, and should be considered
		list.add(new FixtureMatcher(TextFixture.class::isInstance,
										   "Arbitrary-Text Notes"));
		list.add(new FixtureMatcher(Portal.class::isInstance, "Portals"));
		list.add(new FixtureMatcher(Oasis.class::isInstance, "Oases"));
		list.add(new FixtureMatcher(AdventureFixture.class::isInstance, "Adventures"));

		list.add(new FixtureMatcher(CacheFixture.class::isInstance, "Caches"));

		list.add(new FixtureMatcher(Forest.class::isInstance, "Forests"));

		// TODO: Shrub and Meadow were both 15; consider
		list.add(new FixtureMatcher(Shrub.class::isInstance, "Shrubs"));
		list.add(simpleMatcher(Meadow.class, Meadow::isField, "Fields"));
		list.add(simpleMatcher(Meadow.class, fix -> !fix.isField(), "Meadows"));

		// TODO: Since mountains are now a separate aspect of a tile, should this be omitted?
		list.add(new FixtureMatcher(Mountain.class::isInstance, "Mountains"));

		// TODO: Sandbar and Hill were both 5; consider.
		list.add(new FixtureMatcher(Sandbar.class::isInstance, "Sandbars"));
		list.add(new FixtureMatcher(Hill.class::isInstance, "Hills"));

		list.add(simpleMatcher(Ground.class, Ground::isExposed, "Ground (exposed)"));
		list.add(simpleMatcher(Ground.class, fix -> !fix.isExposed(), "Ground"));
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
			fireTableCellUpdated(rowIndex, 0);
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
	/**
	 * @return an iterator over fixture-matchers.
	 */
	@Override
	public Iterator<FixtureMatcher> iterator() {
		return list.iterator();
	}
}

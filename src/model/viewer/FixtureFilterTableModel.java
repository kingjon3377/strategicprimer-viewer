package model.viewer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
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
	 * The backing collection.
	 */
	private final List<FixtureMatcher> list = new ArrayList<>();

	/**
	 * Constructor. Initialize with data that the default algorithm won't handle
	 * properly.
	 */
	public FixtureFilterTableModel() {
		// TODO: Maybe units should be broken up by owner?
		addTrivialMatcher(Unit.class, "Units");
		addTrivialMatcher(Fortress.class, "Fortresses");
		// TODO: Towns should be broken up by kind or size, and maybe by status or owner
		addTrivialMatcher(AbstractTown.class, "Cities, Towns, and Fortifications");
		// TODO: Village through Centaur were all 45, so their ordering happened by
		// chance
		addTrivialMatcher(Village.class, "Villages");
		addTrivialMatcher(Troll.class, "Trolls");
		addTrivialMatcher(Sphinx.class, "Sphinxes");
		addTrivialMatcher(Simurgh.class, "Simurghs");
		addTrivialMatcher(Phoenix.class, "Phoenixes");
		addTrivialMatcher(Ogre.class, "Ogres");
		addTrivialMatcher(Minotaur.class, "Minotaurs");
		addTrivialMatcher(Mine.class, "Mines");
		addTrivialMatcher(Griffin.class, "Griffins");
		addTrivialMatcher(Djinn.class, "Djinni");
		addTrivialMatcher(Centaur.class, "Centaurs");
		// TODO: StoneDeposit through Animal were all 40; they too should be reviewed
		addTrivialMatcher(StoneDeposit.class, "Stone Deposits");
		addTrivialMatcher(MineralVein.class, "Mineral Veins");
		addTrivialMatcher(Giant.class, "Giants");
		addTrivialMatcher(Fairy.class, "Fairies");
		addTrivialMatcher(Dragon.class, "Dragons");
		addTrivialMatcher(Cave.class, "Caves");
		addTrivialMatcher(Battlefield.class, "Battlefields");
		addTrivialMatcher(Animal.class, "Animals");

		addComplements(Grove.class, Grove::isOrchard, "Orchards", "Groves");

		// TODO: Rivers are usually handled specially, so should this really be included?
		addTrivialMatcher(RiverFixture.class, "Rivers");

		// TODO: TextFixture through AdventureFixture were all 25, and should be
		// considered
		addTrivialMatcher(TextFixture.class, "Arbitrary-Text Notes");
		addTrivialMatcher(Portal.class, "Portals");
		addTrivialMatcher(Oasis.class, "Oases");
		addTrivialMatcher(AdventureFixture.class, "Adventures");

		addTrivialMatcher(CacheFixture.class, "Caches");

		addTrivialMatcher(Forest.class, "Forests");

		// TODO: Shrub and Meadow were both 15; consider
		addTrivialMatcher(Shrub.class, "Shrubs");
		addComplements(Meadow.class, Meadow::isField, "Fields", "Meadows");

		// TODO: Mountains are now a separate aspect of a tile; should this be omitted?
		addTrivialMatcher(Mountain.class, "Mountains");

		// TODO: Sandbar and Hill were both 5; consider.
		addTrivialMatcher(Sandbar.class, "Sandbars");
		addTrivialMatcher(Hill.class, "Hills");

		addComplements(Ground.class, Ground::isExposed, "Ground (exposed)", "Ground");
	}
	/**
	 * Add a matcher.
	 * @param matcher the matcher to add.
	 */
	private final void addMatcher(final FixtureMatcher matcher) {
		list.add(matcher);
	}
	/**
	 * Add a matcher that matches all instances of a class.
	 * @param cls the class to match
	 * @param desc the description to use in the matcher
	 */
	private final void addTrivialMatcher(final Class<? extends TileFixture> cls,
										 final String desc) {
		addMatcher(new FixtureMatcher(cls::isInstance, desc));
	}
	/**
	 * Add matchers for a class when a predicate is true and false.
	 * @param <T> the type to match
	 * @param cls the class to match
	 * @param method the method to use as a second predicate
	 * @param firstDesc the description to use for the matcher using the predicate as-s
	 * @param secondDesc the description to use for the matcher using the predicate
	 *                      reversed
	 */
	private final <T extends TileFixture> void addComplements(
			final Class<? extends T> cls, final Predicate<T> method,
			final String firstDesc, final String secondDesc) {
		addMatcher(simpleMatcher(cls, method, firstDesc));
		addMatcher(simpleMatcher(cls, method.negate(), secondDesc));
	}

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
	 * @param rowIndex    the row being queried
	 * @param columnIndex the column being queried
	 * @return whether that cell is editable
	 */
	@Override
	public boolean isCellEditable(final int rowIndex, final int columnIndex) {
		return columnIndex == 0;
	}

	/**
	 * @param aValue      value assign to a cell
	 * @param rowIndex    the row of the cell
	 * @param columnIndex the column of the cell
	 */
	@Override
	public void setValueAt(final Object aValue, final int rowIndex,
						   final int columnIndex) {
		if (columnIndex == 0 && aValue instanceof Boolean) {
			list.get(rowIndex).setDisplayed(((Boolean) aValue).booleanValue());
			fireTableCellUpdated(rowIndex, 0);
		}
	}

	/**
	 * Move a row of a list or table from one position to another.
	 *
	 * @param fromIndex the index to remove from
	 * @param toIndex   the index to move to (its index *before* removing the old!)
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

package model.viewer;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Comparator;
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
import model.map.fixtures.mobile.Dragon;
import model.map.fixtures.mobile.Fairy;
import model.map.fixtures.mobile.Giant;
import model.map.fixtures.mobile.SimpleImmortal;
import model.map.fixtures.mobile.SimpleImmortal.SimpleImmortalKind;
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
		implements Reorderable, ZOrderFilter, Iterable<FixtureMatcher>,
						   Comparator<TileFixture> {
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
		addTrivialMatchers(Unit.class);
		addTrivialMatcher(Fortress.class, "Fortresses");
		// TODO: Towns should be broken up by kind or size, and maybe by status or owner
		addTrivialMatcher(AbstractTown.class, "Cities, Towns, and Fortifications");
		// TODO: Village through Centaur were all 45, so their ordering happened by
		// chance
		addTrivialMatchers(Village.class);
		addSimpleImmortalMatchers(SimpleImmortalKind.Troll, SimpleImmortalKind.Simurgh,
				SimpleImmortalKind.Ogre, SimpleImmortalKind.Minotaur);
		addTrivialMatchers(Mine.class);
		addSimpleImmortalMatchers(SimpleImmortalKind.Griffin, SimpleImmortalKind.Sphinx,
				SimpleImmortalKind.Phoenix, SimpleImmortalKind.Djinn);
		addTrivialMatchers(Centaur.class);
		// TODO: StoneDeposit through Animal were all 40; they too should be reviewed
		addTrivialMatcher(StoneDeposit.class, "Stone Deposits");
		addTrivialMatcher(MineralVein.class, "Mineral Veins");
		addTrivialMatcher(Fairy.class, "Fairies");
		addTrivialMatchers(Giant.class, Dragon.class, Cave.class, Battlefield.class,
				Animal.class);
		// TODO: Animal tracks should probably be matched separately

		addComplements(Grove.class, Grove::isOrchard, "Orchards", "Groves");

		// TODO: Rivers are usually handled specially, so should this really be included?
		addTrivialMatcher(RiverFixture.class, "Rivers");

		// TODO: TextFixture through AdventureFixture were all 25, and should be
		// considered
		addTrivialMatcher(TextFixture.class, "Arbitrary-Text Notes");
		addTrivialMatchers(Portal.class);
		addTrivialMatcher(Oasis.class, "Oases");
		addTrivialMatcher(AdventureFixture.class, "Adventures");

		addTrivialMatcher(CacheFixture.class, "Caches");

		addTrivialMatchers(Forest.class, Shrub.class);
		// TODO: Shrub and Meadow were both 15; consider

		addComplements(Meadow.class, Meadow::isField, "Fields", "Meadows");

		// TODO: Sandbar and Hill were both 5; consider.
		addTrivialMatchers(Sandbar.class, Hill.class);

		addComplements(Ground.class, Ground::isExposed, "Ground (exposed)", "Ground");
	}
	/**
	 * Add matchers for simple immortals.
	 * @param kinds the kinds of immortals to match
	 */
	@SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
	private void addSimpleImmortalMatchers(final SimpleImmortal
															   .SimpleImmortalKind...
														 kinds) {
		for (final SimpleImmortal.SimpleImmortalKind kind : kinds) {
			list.add(new FixtureMatcher(fix -> fix instanceof SimpleImmortal &&
													   ((SimpleImmortal) fix).kind() ==
															   kind, kind.plural()));
		}
	}
	/**
	 * Add matchers that match all instances of given classes, for which we can use
	 * the class names plus "s".
	 * @param classes the classes to match
	 */
	@SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
	@SafeVarargs
	private final void addTrivialMatchers(final Class<? extends TileFixture>... classes) {
		for (final Class<? extends TileFixture> cls : classes) {
			//noinspection StringConcatenationMissingWhitespace
			list.add(new FixtureMatcher(cls::isInstance, cls.getSimpleName() + 's'));
		}
	}

	/**
	 * Add a matcher that matches all instances of a class.
	 * @param cls the class to match
	 * @param desc the description to use in the matcher
	 */
	@SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
	private void addTrivialMatcher(final Class<? extends TileFixture> cls,
								   final String desc) {
		list.add(new FixtureMatcher(cls::isInstance, desc));
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
	@SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
	private <T extends TileFixture> void addComplements(
			final Class<? extends T> cls, final Predicate<T> method,
			final String firstDesc, final String secondDesc) {
		list.add(simpleMatcher(cls, method, firstDesc));
		list.add(simpleMatcher(cls, method.negate(), secondDesc));
	}

	/**
	 * How many rows there are.
	 * @return the number of rows in the model
	 */
	@Override
	public int getRowCount() {
		return list.size();
	}

	/**
	 * How many columns there are: 2.
	 * @return the number of columns in the model
	 */
	@Override
	public int getColumnCount() {
		return 2;
	}

	/**
	 * Get the item at the given cell.
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
	 * Get the name of the given column.
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
	 * Get the Class ob data in the given column.
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
	 * Only the first column is editable.
	 * @param rowIndex    the row being queried
	 * @param columnIndex the column being queried
	 * @return whether that cell is editable
	 */
	@Override
	public boolean isCellEditable(final int rowIndex, final int columnIndex) {
		return columnIndex == 0;
	}

	/**
	 * Set the value of a cell. Only honored for the first column.
	 * @param aValue      value to assign to a cell
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
	 * Whether the given fixture should be displayed.
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
	 * An iterator over the list.
	 * @return an iterator over fixture-matchers.
	 */
	@Override
	public Iterator<FixtureMatcher> iterator() {
		return list.iterator();
	}

	/**
	 * Compare two fixtures on the basis of which is matched first.
	 * @param first the first object to be compared.
	 * @param second the second object to be compared.
	 * @return a number indicating which one is "closer to the top"
	 */
	@SuppressWarnings("ParameterNameDiffersFromOverriddenParameter")
	@Override
	public int compare(final TileFixture first, final TileFixture second) {
		for (final FixtureMatcher matcher : list) {
			if (!matcher.isDisplayed()) {
				continue;
			}
			if (matcher.matches(first)) {
				if (matcher.matches(second)) {
					return 0;
				} else {
					return -1;
				}
			} else if (matcher.matches(second)) {
				return 1;
			}
		}
		return 0;
	}
	/**
	 * A simple toString().
	 * @return a String showing how many matchers we have
	 */
	@Override
	public String toString() {
		return String.format("FixtureFilterTableModel with %d matchers", list.size());
	}
	/**
	 * Prevent serialization.
	 *
	 * @param out ignored
	 * @throws IOException always
	 */
	@SuppressWarnings("unused")
	private void writeObject(final ObjectOutputStream out) throws IOException {
		throw new NotSerializableException("Serialization is not allowed");
	}

	/**
	 * Prevent serialization.
	 *
	 * @param in ignored
	 * @throws IOException            always
	 * @throws ClassNotFoundException never
	 */
	@SuppressWarnings("unused")
	private void readObject(final ObjectInputStream in)
			throws IOException, ClassNotFoundException {
		throw new NotSerializableException("Serialization is not allowed");
	}
}

package drivers.map_viewer;

import java.util.function.Predicate;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;
import java.util.Comparator;

import lovelace.util.Reorderable;

import common.map.TileFixture;
import common.map.fixtures.TextFixture;
import common.map.fixtures.Ground;

import common.map.fixtures.explorable.Cave;
import common.map.fixtures.explorable.Battlefield;
import common.map.fixtures.explorable.Portal;
import common.map.fixtures.explorable.AdventureFixture;

import common.map.fixtures.mobile.Dragon;
import common.map.fixtures.mobile.Centaur;
import common.map.fixtures.mobile.Fairy;
import common.map.fixtures.mobile.Giant;
import common.map.fixtures.mobile.IUnit;
import common.map.fixtures.mobile.Troll;
import common.map.fixtures.mobile.Simurgh;
import common.map.fixtures.mobile.Ogre;
import common.map.fixtures.mobile.Minotaur;
import common.map.fixtures.mobile.Griffin;
import common.map.fixtures.mobile.Sphinx;
import common.map.fixtures.mobile.Phoenix;
import common.map.fixtures.mobile.Djinn;
import common.map.fixtures.mobile.Snowbird;
import common.map.fixtures.mobile.Thunderbird;
import common.map.fixtures.mobile.Pegasus;
import common.map.fixtures.mobile.Unicorn;
import common.map.fixtures.mobile.Kraken;
import common.map.fixtures.mobile.Animal;
import common.map.fixtures.mobile.AnimalTracks;

import common.map.fixtures.resources.Grove;
import common.map.fixtures.resources.Meadow;
import common.map.fixtures.resources.MineralVein;
import common.map.fixtures.resources.CacheFixture;
import common.map.fixtures.resources.Mine;
import common.map.fixtures.resources.StoneDeposit;
import common.map.fixtures.resources.Shrub;

import common.map.fixtures.terrain.Oasis;
import common.map.fixtures.terrain.Hill;
import common.map.fixtures.terrain.Forest;

import common.map.fixtures.towns.Village;
import common.map.fixtures.towns.AbstractTown;
import common.map.fixtures.towns.IFortress;
import common.map.fixtures.towns.TownStatus;
import drivers.common.FixtureMatcher;

/**
 * A class to allow the Z-order of fixtures to be represented as a table (and
 * so dynamically controlled by the user).
 */
public class FixtureFilterTableModel extends AbstractTableModel
		implements Reorderable, ZOrderFilter, Iterable<FixtureMatcher>, Comparator<TileFixture> {
	private final List<FixtureMatcher> matchers = new ArrayList<>();

	private static <T> Predicate<T> not(final Predicate<T> p) {
		return t -> !p.test(t);
	}

	public FixtureFilterTableModel() {
		FixtureMatcher.complements(IUnit.class, u -> !u.getOwner().isIndependent(),
			"Units", "Independent Units").forEach(matchers::add);
		matchers.add(FixtureMatcher.trivialMatcher(IFortress.class, "Fortresses"));
		FixtureMatcher.complements(AbstractTown.class, t -> TownStatus.Active == t.getStatus(),
			"Active Cities, Towns, & Fortifications",
			"Ruined, Abandoned, & Burned Communities").forEach(matchers::add);
		// TODO: break up by owner beyond owned/independent
		FixtureMatcher.complements(Village.class, v -> v.getOwner().isIndependent(),
			"Independent Villages", "Villages With Suzerain").forEach(matchers::add);
		// TODO: Use for-each loops and Arrays.asList() to condense stretches where we don't provide the plural
		matchers.add(FixtureMatcher.trivialMatcher(Mine.class));
		matchers.add(FixtureMatcher.trivialMatcher(Troll.class));
		matchers.add(FixtureMatcher.trivialMatcher(Simurgh.class));
		matchers.add(FixtureMatcher.trivialMatcher(Ogre.class));
		matchers.add(FixtureMatcher.trivialMatcher(Minotaur.class));
		matchers.add(FixtureMatcher.trivialMatcher(Griffin.class));
		matchers.add(FixtureMatcher.trivialMatcher(Sphinx.class, "Sphinxes"));
		matchers.add(FixtureMatcher.trivialMatcher(Phoenix.class, "Phoenixes"));
		matchers.add(FixtureMatcher.trivialMatcher(Djinn.class, "Djinni"));
		matchers.add(FixtureMatcher.trivialMatcher(Centaur.class));
		matchers.add(FixtureMatcher.trivialMatcher(Fairy.class, "Fairies"));
		matchers.add(FixtureMatcher.trivialMatcher(Giant.class));
		matchers.add(FixtureMatcher.trivialMatcher(Dragon.class));
		matchers.add(FixtureMatcher.trivialMatcher(Pegasus.class, "Pegasi"));
		matchers.add(FixtureMatcher.trivialMatcher(Snowbird.class));
		matchers.add(FixtureMatcher.trivialMatcher(Thunderbird.class));
		matchers.add(FixtureMatcher.trivialMatcher(Unicorn.class));
		matchers.add(FixtureMatcher.trivialMatcher(Kraken.class));
		matchers.add(FixtureMatcher.trivialMatcher(Cave.class));
		matchers.add(FixtureMatcher.trivialMatcher(Battlefield.class));
		FixtureMatcher.complements(Animal.class, Animal::isTalking, "Talking Animals", "Animals")
			.forEach(matchers::add);
		matchers.add(FixtureMatcher.trivialMatcher(AnimalTracks.class, "Animal Tracks"));
		matchers.add(FixtureMatcher.trivialMatcher(StoneDeposit.class, "Stone Deposits"));
		matchers.add(FixtureMatcher.trivialMatcher(MineralVein.class, "Mineral Veins"));
		FixtureMatcher.complements(Grove.class, Grove::isOrchard, "Orchards", "Groves")
			.forEach(matchers::add);
		matchers.add(FixtureMatcher.trivialMatcher(TextFixture.class, "Arbitrary-Text Notes"));
		matchers.add(FixtureMatcher.trivialMatcher(Portal.class));
		matchers.add(FixtureMatcher.trivialMatcher(AdventureFixture.class, "Adventures"));
		matchers.add(FixtureMatcher.trivialMatcher(CacheFixture.class, "Caches"));
		matchers.add(FixtureMatcher.trivialMatcher(Oasis.class, "Oases"));
		matchers.add(FixtureMatcher.trivialMatcher(Forest.class));
		FixtureMatcher.complements(Meadow.class, Meadow::isField, "Fields", "Meadows")
			.forEach(matchers::add);
		matchers.add(FixtureMatcher.trivialMatcher(Shrub.class));
		matchers.add(FixtureMatcher.trivialMatcher(Hill.class));
		FixtureMatcher.complements(Ground.class, Ground::isExposed, "Ground (exposed)", "Ground")
			.forEach(matchers::add);
	}

	@Override
	public int getRowCount() {
		return matchers.size();
	}

	@Override
	public int getColumnCount() {
		return 2;
	}

	@Override
	public Object getValueAt(final int rowIndex, final int columnIndex) {
		if (rowIndex >= 0 && rowIndex < matchers.size()) {
			final FixtureMatcher matcher = matchers.get(rowIndex);
			switch (columnIndex) {
			case 0:
				return matcher.isDisplayed();
			case 1:
				return matcher.getDescription();
			default:
				throw new ArrayIndexOutOfBoundsException(columnIndex);
			}
		} else {
			throw new ArrayIndexOutOfBoundsException(rowIndex);
		}
	}

	@Override
	public String getColumnName(final int column) {
		switch (column) {
		case 0:
			return "Visible";
		case 1:
			return "Category";
		default:
			return super.getColumnName(column);
		}
	}

	@Override
	public Class<?> getColumnClass(final int columnIndex) {
		switch (columnIndex) {
		case 0:
			return Boolean.class;
		case 1:
			return String.class;
		default:
			return Object.class;
		}
	}

	@Override
	public boolean isCellEditable(final int rowIndex, final int columnIndex) {
		return columnIndex == 0; // TODO: restrict to existing rows as well?
	}

	@Override
	public void setValueAt(final Object val, final int rowIndex, final int columnIndex) {
		if (columnIndex == 0 && rowIndex >= 0 && rowIndex < matchers.size()) {
			final FixtureMatcher matcher = matchers.get(rowIndex);
			matcher.setDisplayed((Boolean) val);
			fireTableCellUpdated(rowIndex, 0);
		}
	}

	@Override
	public void reorder(final int fromIndex, final int toIndex) {
		if (fromIndex != toIndex) {
			final int actual;
			if (fromIndex < toIndex) {
				actual = toIndex - 1;
			} else {
				actual = toIndex;
			}
			final FixtureMatcher val = matchers.remove(fromIndex);
			matchers.add(toIndex, val);
			fireTableRowsDeleted(fromIndex, fromIndex);
			fireTableRowsInserted(toIndex, actual);
		}
	}

	@Override
	public boolean shouldDisplay(final TileFixture fixture) {
		for (final FixtureMatcher matcher : matchers) {
			if (matcher.matches(fixture)) {
				return matcher.isDisplayed();
			}
		}
		final Class<? extends TileFixture> cls = fixture.getClass();
		matchers.add(FixtureMatcher.trivialMatcher(cls, fixture.getPlural()));
		final int size = matchers.size();
		fireTableRowsInserted(size - 1, size - 1);
		return true;
	}

	@Override
	public Iterator<FixtureMatcher> iterator() {
		return matchers.iterator();
	}

	@Override
	public int compare(final TileFixture first, final TileFixture second) {
		for (final FixtureMatcher matcher : matchers) {
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
}

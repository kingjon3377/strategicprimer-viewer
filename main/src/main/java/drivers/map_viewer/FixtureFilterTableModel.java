package drivers.map_viewer;

import java.io.Serial;
import java.util.function.Predicate;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import javax.swing.table.AbstractTableModel;

import drivers.common.IterableComparator;
import legacy.map.fixtures.resources.ExposureStatus;
import lovelace.util.Reorderable;

import legacy.map.TileFixture;
import legacy.map.fixtures.TextFixture;
import legacy.map.fixtures.Ground;

import legacy.map.fixtures.explorable.Cave;
import legacy.map.fixtures.explorable.Battlefield;
import legacy.map.fixtures.explorable.Portal;
import legacy.map.fixtures.explorable.AdventureFixture;

import legacy.map.fixtures.mobile.Dragon;
import legacy.map.fixtures.mobile.Centaur;
import legacy.map.fixtures.mobile.Fairy;
import legacy.map.fixtures.mobile.Giant;
import legacy.map.fixtures.mobile.IUnit;
import legacy.map.fixtures.mobile.Troll;
import legacy.map.fixtures.mobile.Simurgh;
import legacy.map.fixtures.mobile.Ogre;
import legacy.map.fixtures.mobile.Minotaur;
import legacy.map.fixtures.mobile.Griffin;
import legacy.map.fixtures.mobile.Sphinx;
import legacy.map.fixtures.mobile.Phoenix;
import legacy.map.fixtures.mobile.Djinn;
import legacy.map.fixtures.mobile.Snowbird;
import legacy.map.fixtures.mobile.Thunderbird;
import legacy.map.fixtures.mobile.Pegasus;
import legacy.map.fixtures.mobile.Unicorn;
import legacy.map.fixtures.mobile.Kraken;
import legacy.map.fixtures.mobile.Animal;
import legacy.map.fixtures.mobile.AnimalTracks;

import legacy.map.fixtures.resources.Grove;
import legacy.map.fixtures.resources.Meadow;
import legacy.map.fixtures.resources.MineralVein;
import legacy.map.fixtures.resources.CacheFixture;
import legacy.map.fixtures.resources.Mine;
import legacy.map.fixtures.resources.StoneDeposit;
import legacy.map.fixtures.resources.Shrub;

import legacy.map.fixtures.terrain.Oasis;
import legacy.map.fixtures.terrain.Hill;
import legacy.map.fixtures.terrain.Forest;

import legacy.map.fixtures.towns.Village;
import legacy.map.fixtures.towns.AbstractTown;
import legacy.map.fixtures.towns.IFortress;
import common.map.fixtures.towns.TownStatus;
import drivers.common.FixtureMatcher;

/**
 * A class to allow the Z-order of fixtures to be represented as a table (and
 * so dynamically controlled by the user).
 */
public final class FixtureFilterTableModel extends AbstractTableModel
		implements Reorderable, ZOrderFilter, IterableComparator<FixtureMatcher, TileFixture> {
	@Serial
	private static final long serialVersionUID = 1L;
	private final List<FixtureMatcher> matchers = new ArrayList<>();

	private static <T> Predicate<T> not(final Predicate<T> p) {
		return t -> !p.test(t);
	}

	public FixtureFilterTableModel() {
		FixtureMatcher.complements(IUnit.class, u -> !u.owner().isIndependent(),
				"Units", "Independent Units").forEach(matchers::add);
		matchers.add(FixtureMatcher.trivialMatcher(IFortress.class, "Fortresses"));
		FixtureMatcher.complements(AbstractTown.class, t -> TownStatus.Active == t.getStatus(),
				"Active Cities, Towns, & Fortifications",
				"Ruined, Abandoned, & Burned Communities").forEach(matchers::add);
		// TODO: break up by owner beyond owned/independent
		FixtureMatcher.complements(Village.class, v -> v.owner().isIndependent(),
				"Independent Villages", "Villages With Suzerain").forEach(matchers::add);
		Stream.of(Mine.class, Troll.class, Simurgh.class, Ogre.class,
				Minotaur.class, Griffin.class).map(FixtureMatcher::trivialMatcher).forEach(matchers::add);
		matchers.add(FixtureMatcher.trivialMatcher(Sphinx.class, "Sphinxes"));
		matchers.add(FixtureMatcher.trivialMatcher(Phoenix.class, "Phoenixes"));
		matchers.add(FixtureMatcher.trivialMatcher(Djinn.class, "Djinni"));
		matchers.add(FixtureMatcher.trivialMatcher(Centaur.class));
		matchers.add(FixtureMatcher.trivialMatcher(Fairy.class, "Fairies"));
		matchers.add(FixtureMatcher.trivialMatcher(Giant.class));
		matchers.add(FixtureMatcher.trivialMatcher(Dragon.class));
		matchers.add(FixtureMatcher.trivialMatcher(Pegasus.class, "Pegasi"));
		Stream.of(Snowbird.class, Thunderbird.class, Unicorn.class, Kraken.class, Cave.class, Battlefield.class)
				.map(FixtureMatcher::trivialMatcher).forEach(matchers::add);
		FixtureMatcher.complements(Animal.class, Animal::isTalking, "Talking Animals", "Animals")
				.forEach(matchers::add);
		matchers.add(FixtureMatcher.trivialMatcher(AnimalTracks.class, "Animal Tracks"));
		matchers.add(FixtureMatcher.trivialMatcher(StoneDeposit.class, "Stone Deposits"));
		matchers.add(FixtureMatcher.trivialMatcher(MineralVein.class, "Mineral Veins"));
		FixtureMatcher.complements(Grove.class, g -> g.getType() == Grove.GroveType.ORCHARD, "Orchards", "Groves")
				.forEach(matchers::add);
		matchers.add(FixtureMatcher.trivialMatcher(TextFixture.class, "Arbitrary-Text Notes"));
		matchers.add(FixtureMatcher.trivialMatcher(Portal.class));
		matchers.add(FixtureMatcher.trivialMatcher(AdventureFixture.class, "Adventures"));
		matchers.add(FixtureMatcher.trivialMatcher(CacheFixture.class, "Caches"));
		matchers.add(FixtureMatcher.trivialMatcher(Oasis.class, "Oases"));
		matchers.add(FixtureMatcher.trivialMatcher(Forest.class));
		FixtureMatcher.complements(Meadow.class, m -> m.getType() == Meadow.MeadowType.FIELD, "Fields", "Meadows")
				.forEach(matchers::add);
		matchers.add(FixtureMatcher.trivialMatcher(Shrub.class));
		matchers.add(FixtureMatcher.trivialMatcher(Hill.class));
		FixtureMatcher.complements(Ground.class, g -> g.getExposure() == ExposureStatus.EXPOSED, "Ground (exposed)",
						"Ground").forEach(matchers::add);
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
			return switch (columnIndex) {
				case 0 -> matcher.isDisplayed();
				case 1 -> matcher.getDescription();
				default -> throw new ArrayIndexOutOfBoundsException(columnIndex);
			};
		} else {
			throw new ArrayIndexOutOfBoundsException(rowIndex);
		}
	}

	@Override
	public String getColumnName(final int column) {
		return switch (column) {
			case 0 -> "Visible";
			case 1 -> "Category";
			default -> super.getColumnName(column);
		};
	}

	@Override
	public Class<?> getColumnClass(final int columnIndex) {
		return switch (columnIndex) {
			case 0 -> Boolean.class;
			case 1 -> String.class;
			default -> Object.class;
		};
	}

	@Override
	public boolean isCellEditable(final int rowIndex, final int columnIndex) {
		return columnIndex == 0 && rowIndex >= 0 && rowIndex < matchers.size();
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

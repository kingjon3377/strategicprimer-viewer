package controller.map.misc;

import java.util.Comparator;

import model.map.HasKind;
import model.map.fixtures.resources.CacheFixture;
import model.map.fixtures.resources.Grove;
import model.map.fixtures.resources.HarvestableFixture;
import model.map.fixtures.resources.Meadow;
import model.map.fixtures.resources.Mine;
import model.map.fixtures.resources.MineralVein;
import model.map.fixtures.resources.Shrub;
import model.map.fixtures.resources.StoneDeposit;
import util.IntPair;

/**
 * A comparator for Harvestables. Needed for the report generator.
 *
 * TODO: Write tests.
 *
 * @author Jonathan Lovelace
 *
 */
public class HarvestableComparator implements Comparator<HarvestableFixture> { // NOPMD
	/**
	 * @param one the first
	 * @param two the second
	 * @return the result of the comparison.
	 */
	@Override
	public int compare(final HarvestableFixture one,
			final HarvestableFixture two) { // NOPMD
		if (one.getClass().equals(two.getClass())) {
			if (one instanceof CacheFixture
					&& two instanceof CacheFixture) {
				// FindBugs objected if we didn't do both instanceofs
				final CacheFixture oneT = (CacheFixture) one;
				final CacheFixture twoT = (CacheFixture) two;
				return repeatedCompare(kindPair(oneT, twoT),
						hashPair(oneT.getContents(), twoT.getContents()),
						idPair(one, two));
			} else if (one instanceof Grove && two instanceof Grove) {
				final Grove oneT = (Grove) one;
				final Grove twoT = (Grove) two;
				return repeatedCompare(kindPair(oneT, twoT),
						boolPair(oneT.isOrchard(), twoT.isOrchard()),
						boolPair(oneT.isCultivated(), twoT.isCultivated()),
						idPair(one, two));
			} else if (one instanceof Meadow && two instanceof Meadow) {
				final Meadow oneT = (Meadow) one;
				final Meadow twoT = (Meadow) two;
				return repeatedCompare(kindPair(oneT, twoT),
						boolPair(oneT.isField(), twoT.isField()),
						boolPair(oneT.isCultivated(), twoT.isCultivated()),
						enumPair(oneT.getStatus(), twoT.getStatus()),
						idPair(one, two));
			} else if (one instanceof Mine && two instanceof Mine) {
				final Mine oneT = (Mine) one;
				final Mine twoT = (Mine) two;
				return repeatedCompare(kindPair(oneT, twoT),
						enumPair(oneT.getStatus(), twoT.getStatus()),
						idPair(one, two));
			} else if (one instanceof MineralVein && two instanceof MineralVein) {
				final MineralVein oneT = (MineralVein) one;
				final MineralVein twoT = (MineralVein) two;
				return repeatedCompare(kindPair(oneT, twoT),
						boolPair(oneT.isExposed(), twoT.isExposed()),
						idPair(one, two));
			} else if (one instanceof Shrub && two instanceof Shrub) {
				return repeatedCompare(kindPair((Shrub) one, (Shrub) two),
						idPair(one, two));
			} else if (one instanceof StoneDeposit
					&& two instanceof StoneDeposit) {
				return repeatedCompare(
						enumPair(((StoneDeposit) one).stone(),
								((StoneDeposit) two).stone()), idPair(one, two));
			} else {
				return one.getID() - two.getID();
			}
		} else {
			return one.getClass().hashCode() - two.getClass().hashCode();
		}
	}

	/**
	 * @param pairs a series of pairs of integers.
	 * @return the result of subtracting the first pair that aren't equal, or 0
	 *         if they all are.
	 */
	private static int repeatedCompare(final IntPair... pairs) {
		for (final IntPair pair : pairs) {
			if (pair.first == pair.second) {
				continue;
			} else {
				return pair.first - pair.second; // NOPMD
			}
		}
		return 0;
	}

	/**
	 * @param one a fixture
	 * @param two another
	 * @return a pair of their IDs
	 */
	private static IntPair idPair(final HarvestableFixture one,
			final HarvestableFixture two) {
		return IntPair.of(one.getID(), two.getID());
	}
	/**
	 * @param one a boolean value
	 * @param two another boolean value
	 * @return an IntPair of the result of the comparison between them and zero.
	 */
	private static IntPair boolPair(final boolean one, final boolean two) {
		return IntPair.of(Boolean.compare(one, two), 0);
	}
	/**
	 * @param one an enum value
	 * @param two another enum value
	 * @param <T> their type
	 * @return an IntPair of their ordinals.
	 */
	private static <T extends Enum<T>> IntPair enumPair(final T one, final T two) {
		return IntPair.of(one.ordinal(), two.ordinal());
	}
	/**
	 * @param one a string value
	 * @param two another string value
	 * @return an IntPair of their hash values.
	 */
	private static IntPair hashPair(final String one, final String two) {
		return IntPair.of(one.hashCode(), two.hashCode());
	}
	/**
	 * @param one an object
	 * @param two another object
	 * @return an IntPair of the hash values of their "kind" properties
	 */
	private static IntPair kindPair(final HasKind one, final HasKind two) {
		return IntPair.of(one.getKind().hashCode(), two.getKind().hashCode());
	}
	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "HarvestableComparator";
	}
}

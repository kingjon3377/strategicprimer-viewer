package controller.map.misc;

import java.util.Comparator;

import model.map.fixtures.resources.Battlefield;
import model.map.fixtures.resources.CacheFixture;
import model.map.fixtures.resources.Cave;
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
			if (one instanceof Battlefield || one instanceof Cave) {
				return one.getID() - two.getID(); // NOPMD
			} else if (one instanceof CacheFixture
					&& two instanceof CacheFixture) {
				// FindBugs objected if we didn't do both instanceofs
				return repeatedCompare(IntPair.of(((CacheFixture) one)// NOPMD
						.getKind().hashCode(), ((CacheFixture) two).getKind()
						.hashCode()), IntPair.of(((CacheFixture) one)
						.getContents().hashCode(), ((CacheFixture) two)
						.getContents().hashCode()), idPair(one, two));
			} else if (one instanceof Grove && two instanceof Grove) {
				return repeatedCompare(IntPair.of(((Grove) one).getKind()// NOPMD
						.hashCode(), ((Grove) two).getKind().hashCode()),
						IntPair.of(Boolean.compare(((Grove) one).isOrchard(),
								((Grove) two).isOrchard()), 0), IntPair.of(
								Boolean.compare(((Grove) one).isCultivated(),
										((Grove) two).isCultivated()), 0),
						idPair(one, two));
			} else if (one instanceof Meadow && two instanceof Meadow) {
				return repeatedCompare(IntPair.of(((Meadow) one).getKind()// NOPMD
						.hashCode(), ((Meadow) two).getKind().hashCode()),
						IntPair.of(Boolean.compare(((Meadow) one).isField(),
								((Meadow) two).isField()), 0), IntPair.of(
								Boolean.compare(((Meadow) one).isCultivated(),
										((Meadow) two).isCultivated()), 0),
						IntPair.of(((Meadow) one).getStatus().ordinal(),
								((Meadow) two).getStatus().ordinal()),
						idPair(one, two));
			} else if (one instanceof Mine && two instanceof Mine) {
				return repeatedCompare(IntPair.of(((Mine) one).getKind()// NOPMD
						.hashCode(), ((Mine) two).getKind().hashCode()),
						IntPair.of(((Mine) one).getStatus().ordinal(),
								((Mine) two).getStatus().ordinal()),
						idPair(one, two));
			} else if (one instanceof MineralVein && two instanceof MineralVein) {
				return repeatedCompare(IntPair.of(((MineralVein) one).getKind()// NOPMD
						.hashCode(), ((MineralVein) two).getKind().hashCode()),
						IntPair.of(Boolean.compare(
								((MineralVein) one).isExposed(),
								((MineralVein) two).isExposed()), 0),
						idPair(one, two));
			} else if (one instanceof Shrub && two instanceof Shrub) {
				return repeatedCompare(IntPair.of(((Shrub) one).getKind()// NOPMD
						.hashCode(), ((Shrub) two).getKind().hashCode()),
						idPair(one, two));
			} else if (one instanceof StoneDeposit
					&& two instanceof StoneDeposit) {
				return repeatedCompare(IntPair.of(((StoneDeposit) one).stone()// NOPMD
						.ordinal(), ((StoneDeposit) two).stone().ordinal()),
						idPair(one, two));
			} else {
				throw new IllegalStateException(
						"Unhandled Harvestable implementation or impossible condition");
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
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "HarvestableComparator";
	}
}

package controller.map.misc;

import java.util.Comparator;

import model.map.fixtures.towns.AbstractTown;
import model.map.fixtures.towns.City;
import model.map.fixtures.towns.Fortification;
import model.map.fixtures.towns.Fortress;
import model.map.fixtures.towns.ITownFixture;
import model.map.fixtures.towns.Town;
import model.map.fixtures.towns.TownSize;
import model.map.fixtures.towns.TownStatus;

/**
 * A comparator for towns, to put active ones before abandoned ones before
 * ruined ones before burned-out ones, bigger ones before smaller ones, cities
 * before towns before fortifications, and thereafter alphabetically. We only
 * accept AbstractTowns because I don't want to have to deal with villages and
 * fortresses too ...
 *
 * TODO: Write tests.
 *
 * @author Jonathan Lovelace
 */
public final class TownComparator implements Comparator<AbstractTown> {
	/**
	 * A comparator for town-sizes.
	 */
	private static final Comparator<TownSize> SIZE_CMP = new Comparator<TownSize>() {
		/**
		 * @param one the first kind
		 * @param two the second kind
		 * @return a negative integer if the first is "less" than the second,
		 *         zero if they're the same, and a positive integer if the first
		 *         is "greater" than the second.
		 */
		@Override
		public int compare(final TownSize one, final TownSize two) {
			if (one.equals(two)) {
				return 0; // NOPMD
			} else if (TownSize.Large.equals(one)) {
				return -1; // NOPMD
			} else if (TownSize.Large.equals(two)) {
				return 1; // NOPMD
			} else if (TownSize.Medium.equals(one)) {
				return -1; // NOPMD
			} else {
				return 1;
			}
		}
	};

	/**
	 * A comparator for town-statuses, to put active towns before abandoned ones
	 * before ruined ones before burned-out ones.
	 */
	private static final Comparator<TownStatus> ST_CMP =
			new Comparator<TownStatus>() {
		/**
		 * @param one the first kind
		 * @param two the second kind
		 * @return a negative integer if the first is "less" than the second,
		 *         zero if they're the same, and a positive integer if the first
		 *         is "greater" than the second.
		 */
		@Override
		public int compare(final TownStatus one, final TownStatus two) {
			if (one.equals(two)) {
				return 0; // NOPMD
			} else if (TownStatus.Active.equals(one)) {
				return -1; // NOPMD
			} else if (TownStatus.Active.equals(two)) {
				return 1; // NOPMD
			} else if (TownStatus.Abandoned.equals(one)) {
				return -1; // NOPMD
			} else if (TownStatus.Abandoned.equals(two)) {
				return 1; // NOPMD
			} else if (TownStatus.Ruined.equals(one)) {
				return -1; // NOPMD
			} else {
				return 1;
			}
		}
	};

	/**
	 * A comparator for towns, sorting them *only* on the basis of kind, putting
	 * fortresses before cities before towns before fortifications before villages.
	 */
	private static final Comparator<ITownFixture> KIND_CMP =
			new Comparator<ITownFixture>() {
		/**
		 * @param one the first kind
		 * @param two the second kind
		 * @return a negative integer if the first is "less" than the second,
		 *         zero if they're the same, and a positive integer if the first
		 *         is "greater" than the second.
		 */
		@Override
		public int compare(final ITownFixture one, final ITownFixture two) {
			if (one instanceof Fortress) {
				if (two instanceof Fortress) {
					return 0; // NOPMD
				} else {
					return -1; // NOPMD
				}
			} else if (two instanceof Fortress) {
				return 1; // NOPMD
			} else if (one instanceof City) {
				if (two instanceof City) {
					return 0; // NOPMD
				} else {
					return -1; // NOPMD
				}
			} else if (two instanceof City) {
				return 1; // NOPMD
			} else if (one instanceof Town) {
				if (two instanceof Town) {
					return 0; // NOPMD
				} else {
					return -1; // NOPMD
				}
			} else if (two instanceof Town) {
				return 1; // NOPMD
			} else if (one instanceof Fortification) {
				if (two instanceof Fortification) {
					return 0; // NOPMD
				} else {
					return -1; // NOPMD
				}
			} else if (two instanceof Fortification) {
				return 1; // NOPMD
			} else {
				// They should be both villages ...
				return 0;
			}
		}
	};
	/**
	 * This is hackishly implemented; fortunately, in each case I can rely on
	 * there being only three (or four, for status) possibilities and the two
	 * towns' values for them not being the same.
	 *
	 * @param one the first town
	 * @param two the second
	 * @return a negative integer if the first is "less" than the second, zero
	 *         if they're the same, and a positive integer if the first is
	 *         "greater" than the second.
	 */
	@Override
	public int compare(final AbstractTown one, final AbstractTown two) {
		if (one.status().equals(two.status())) {
			if (one.size().equals(two.size())) {
				if (one.getClass().equals(two.getClass())) {
					return one.getName().compareTo(two.getName()); // NOPMD
				} else {
					return KIND_CMP.compare(one, two); // NOPMD
				}
			} else {
				return SIZE_CMP.compare(one.size(), two.size()); // NOPMD
			}
		} else {
			return ST_CMP.compare(one.status(), two.status());
		}
	}

	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "TownComparator";
	}
}

package controller.map.misc;

import java.io.Serializable;
import java.util.Comparator;

import model.map.fixtures.towns.AbstractTown;
import model.map.fixtures.towns.TownKind;
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
public final class TownComparator implements Comparator<AbstractTown>, Serializable {
	/**
	 * A comparator for town-sizes.
	 */
	static final class TownSizeComparator implements Comparator<TownSize>, Serializable {
		/**
		 * Version UID for serialization.
		 */
		// ESCA-JAVA0096:
		private static final long serialVersionUID = 1L;
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
	}
	/**
	 * A comparator for town-statuses, to put active towns before abandoned ones before ruined ones before burned-out ones.
	 */
	static final class TownStatusComparator implements Comparator<TownStatus>, Serializable {
		/**
		 * Version UID for serialization.
		 */
		// ESCA-JAVA0096:
		private static final long serialVersionUID = 1L;
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
	}
	/**
	 * A comparator for town-kinds, to put cities before towns before fortifications.
	 */
	static final class TownKindComparator implements Comparator<TownKind>, Serializable {
		/**
		 * Version UID for serialization.
		 */
		// ESCA-JAVA0096:
		private static final long serialVersionUID = 1L;
		/**
		 * @param one the first kind
		 * @param two the second kind
		 * @return a negative integer if the first is "less" than the second,
		 *         zero if they're the same, and a positive integer if the first
		 *         is "greater" than the second.
		 */
		@Override
		public int compare(final TownKind one, final TownKind two) {
			if (one.equals(two)) {
				return 0; // NOPMD
			} else if (TownKind.City.equals(one)) {
				return -1; // NOPMD
			} else if (TownKind.City.equals(two)) {
				return 1; // NOPMD
			} else if (TownKind.Town.equals(one)) {
				return -1; // NOPMD
			} else {
				return 1;
			}
		}
	}
	/**
	 * Version UID for serialization.
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * Comparator for TownStatuses.
	 */
	private static final TownStatusComparator STAT_COMP = new TownStatusComparator();
	/**
	 * Comparator for TownKinds.
	 */
	private static final TownKindComparator KIND_COMP = new TownKindComparator();
	/**
	 * Comparator for TownSizes.
	 */
	private static final TownSizeComparator SIZE_COMP = new TownSizeComparator();
	/**
	 * This is hackishly implemented; fortunately, in each case I can rely
	 * on there being only three (or four, for status) possibilities and the
	 * two towns' values for them not being the same.
	 *
	 * @param one the first town
	 * @param two the second
	 * @return a negative integer if the first is "less" than the second,
	 *         zero if they're the same, and a positive integer if the first
	 *         is "greater" than the second.
	 */
	@Override
	public int compare(final AbstractTown one, final AbstractTown two) {
		if (one.status().equals(two.status())) {
			if (one.size().equals(two.size())) {
				if (one.kind().equals(two.kind())) {
					return one.getName().compareTo(two.getName()); // NOPMD
				} else {
					return KIND_COMP.compare(one.kind(), two.kind()); // NOPMD
				}
			} else {
				return SIZE_COMP.compare(one.size(), two.size()); // NOPMD
			}
		} else {
			return STAT_COMP.compare(one.status(), two.status());
		}
	}

}
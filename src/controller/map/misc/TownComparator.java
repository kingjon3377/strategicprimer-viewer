package controller.map.misc;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import model.map.fixtures.towns.City;
import model.map.fixtures.towns.Fortification;
import model.map.fixtures.towns.Fortress;
import model.map.fixtures.towns.ITownFixture;
import model.map.fixtures.towns.Town;
import model.map.fixtures.towns.TownSize;
import model.map.fixtures.towns.TownStatus;
import org.eclipse.jdt.annotation.NonNull;

/**
 * A comparator for towns, to put active ones before abandoned ones before ruined ones
 * before burned-out ones, bigger ones before smaller ones, cities before towns before
 * fortifications, and thereafter alphabetically. We only accept AbstractTowns because I
 * don't want to have to deal with villages and fortresses too ...
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2013-2014 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program. If not, see
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public final class TownComparator implements Comparator<@NonNull ITownFixture> {
	/**
	 * @param one One town-size
	 * @param two Another
	 * @return the result of a comparison between them
	 */
	@SuppressWarnings("QuestionableName")
	public static int compareTownSize(final TownSize one, final TownSize two) {
		if (one == two) {
			return 0;
		} else if (TownSize.Large == one) {
			return -1;
		} else if (TownSize.Large == two) {
			return 1;
		} else if (TownSize.Medium == one) {
			return -1;
		} else {
			return 1;
		}
	}

	/**
	 * A comparator for town-statuses, to put active towns before abandoned ones before
	 * ruined ones before burned-out ones.
	 * @param one one town-status
	 * @param two another
	 * @return the result of the comparison
	 */
	@SuppressWarnings("QuestionableName")
	public static int compareTownStatus(final TownStatus one, final TownStatus two) {
		if (one == two) {
			return 0;
		} else if (TownStatus.Active == one) {
			return -1;
		} else if (TownStatus.Active == two) {
			return 1;
		} else if (TownStatus.Abandoned == one) {
			return -1;
		} else if (TownStatus.Abandoned == two) {
			return 1;
		} else if (TownStatus.Ruined == one) {
			return -1;
		} else {
			return 1;
		}
	}

	/**
	 * A comparator for towns, sorting them *only* on the basis of kind, putting
	 * fortresses before cities before towns before fortifications before villages.
	 * @param one a town
	 * @param two another
	 * @return the result of a comparison between them only on the basis of town-kind.
	 */
	@SuppressWarnings("QuestionableName")
	public static int compareTownKind(final ITownFixture one,
									  final ITownFixture two) {
		if (one instanceof Fortress) {
			if (two instanceof Fortress) {
				return 0;
			} else {
				return -1;
			}
		} else if (two instanceof Fortress) {
			return 1;
		} else if (one instanceof City) {
			if (two instanceof City) {
				return 0;
			} else {
				return -1;
			}
		} else if (two instanceof City) {
			return 1;
		} else if (one instanceof Town) {
			if (two instanceof Town) {
				return 0;
			} else {
				return -1;
			}
		} else if (two instanceof Town) {
			return 1;
		} else if (one instanceof Fortification) {
			if (two instanceof Fortification) {
				return 0;
			} else {
				return -1;
			}
		} else if (two instanceof Fortification) {
			return 1;
		} else {
			// They should be both villages ...
			return 0;
		}
	}
	/**
	 * A list of comparators in the order to use them.
	 */
	private static final List<Comparator<ITownFixture>> COMPARATORS =
			Arrays.asList((one, two) -> compareTownStatus(one.status(), two.status()),
					(one, two) -> compareTownSize(one.size(), two.size()), TownComparator::compareTownKind,
					(one, two) -> one.getName().compareTo(two.getName()));
	/**
	 * This implementation is rather a hack; fortunately, in each case I can rely on
	 * there being only three (or four, for status) possibilities and the two towns'
	 * values for them not being the same.
	 *
	 * @param townOne the first town
	 * @param townTwo the second
	 * @return a negative integer if the first is "less" than the second, zero if they're
	 * the same, and a positive integer if the first is "greater" than the second.
	 */
	@SuppressWarnings("ParameterNameDiffersFromOverriddenParameter")
	@Override
	public int compare(final ITownFixture townOne, final ITownFixture townTwo) {
		int retval = 0;
		for (final Comparator<ITownFixture> comparator : COMPARATORS) {
			if (retval != 0) {
				return retval;
			} else {
				retval = comparator.compare(townOne, townTwo);
			}
		}
		return retval;
	}

	/**
	 * @return a String representation of the object
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "TownComparator";
	}
}

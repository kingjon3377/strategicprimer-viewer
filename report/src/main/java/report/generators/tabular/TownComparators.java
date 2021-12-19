package report.generators.tabular;

import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;

import common.map.Player;
import common.map.PlayerImpl;
import common.map.fixtures.towns.TownStatus;
import common.map.fixtures.towns.TownSize;
import common.map.fixtures.towns.FortressImpl;
import common.map.fixtures.towns.ITownFixture;
import common.map.fixtures.towns.IFortress;
import common.map.fixtures.towns.Village;
import common.map.fixtures.towns.City;
import common.map.fixtures.towns.Town;
import common.map.fixtures.towns.Fortification;
import java.util.Collections;

/**
 * A class to provide {@link compareTowns a total ordering} for {@link
 * ITownFixture towns}. The methods it uses to derive that ordering are useful
 * by themselves as well, and so are also public.
 */
public class TownComparators {
	/**
	 * A comparator for towns, sorting them <em>only</em> on the basis of
	 * what kind of town they are, putting fortresses before cities before
	 * towns before fortifications before villages.
	 */
	public static int compareTownKind(ITownFixture one, ITownFixture two) {
		if (one instanceof IFortress) {
			if (two instanceof IFortress) {
				return 0;
			} else {
				return -1;
			}
		} else if (two instanceof IFortress) {
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
		} else if (one instanceof Village && two instanceof Village) {
			return 0;
		} else {
			throw new IllegalStateException("Unexpected town types");
		}
	}

	/**
	 * A total ordering for towns.
	 */
	public static int compareTowns(ITownFixture one, ITownFixture two) {
		return Comparator.comparing(ITownFixture::getStatus)
			.thenComparing(Comparator.comparing(ITownFixture::getTownSize,
				Comparator.reverseOrder()))
			.thenComparing(TownComparators::compareTownKind)
			.thenComparing(Comparator.comparing(ITownFixture::getName)).compare(one, two);
	}
}

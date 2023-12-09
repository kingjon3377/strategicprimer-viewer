package report.generators.tabular;

import java.util.Comparator;

import legacy.map.fixtures.towns.ITownFixture;
import legacy.map.fixtures.towns.IFortress;
import legacy.map.fixtures.towns.Village;
import legacy.map.fixtures.towns.City;
import legacy.map.fixtures.towns.Town;
import legacy.map.fixtures.towns.Fortification;

/**
 * A class to provide {@link #compareTowns a total ordering} for {@link
 * ITownFixture towns}. The methods it uses to derive that ordering are useful
 * by themselves as well, and so are also public.
 */
public final class TownComparators {
    private TownComparators() {
    }

    /**
     * A comparator for towns, sorting them <em>only</em> on the basis of
     * what kind of town they are, putting fortresses before cities before
     * towns before fortifications before villages.
     */
    public static int compareTownKind(final ITownFixture one, final ITownFixture two) {
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
    public static int compareTowns(final ITownFixture one, final ITownFixture two) {
        return Comparator.comparing(ITownFixture::getStatus)
                .thenComparing(ITownFixture::getTownSize, Comparator.reverseOrder())
                .thenComparing(TownComparators::compareTownKind)
                .thenComparing(ITownFixture::getName).compare(one, two);
    }
}

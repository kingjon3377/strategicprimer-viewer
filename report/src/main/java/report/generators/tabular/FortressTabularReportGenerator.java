package report.generators.tabular;

import java.util.List;

import org.javatuples.Pair;
import org.jetbrains.annotations.Nullable;

import lovelace.util.DelayedRemovalMap;

import legacy.DistanceComparator;
import legacy.map.Player;
import legacy.map.IFixture;
import legacy.map.MapDimensions;
import legacy.map.Point;
import legacy.map.fixtures.towns.IFortress;

import java.util.Comparator;
import java.util.Map;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;

/**
 * A tabular report generator for fortresses.
 */
public class FortressTabularReportGenerator implements ITableGenerator<IFortress> {
    @Override
    public boolean canHandle(final IFixture fixture) {
        return fixture instanceof IFortress;
    }

    private final Player player;
    private final @Nullable Point hq;
    private final MapDimensions dimensions;

    public FortressTabularReportGenerator(final Player player, final @Nullable Point hq, final MapDimensions dimensions) {
        this.player = player;
        this.hq = hq;
        this.dimensions = dimensions;
	    if (Objects.isNull(hq)) {
            distanceComparator = (one, two) -> 0;
        } else {
            distanceComparator = new DistanceComparator(hq, dimensions);
        }
    }

    /**
     * The header fields are Distance, Location, Owner, and Name.
     */
    @Override
    public List<String> getHeaderRow() {
        return Arrays.asList("Distance", "Location", "Owner", "Name");
    }

    /**
     * The file-name to (by default) write this table to.
     */
    @Override
    public String getTableName() {
        return "fortresses";
    }

    private final Comparator<Point> distanceComparator;

    /**
     * Create a table row representing the fortress.
     */
    @Override
    public List<List<String>> produce(
            final DelayedRemovalMap<Integer, Pair<Point, IFixture>> fixtures,
            final IFortress item, final int key, final Point loc, final Map<Integer, Integer> parentMap) {
        final List<String> retval = Arrays.asList(distanceString(loc, hq, dimensions),
                locationString(loc), ownerString(player, item.owner()), item.getName());
        // Players shouldn't be able to see the contents of others' fortresses
        // in other tables.
        if (!player.equals(item.owner())) {
            item.stream().map(IFixture::getId).forEach(fixtures::remove);
        }
        fixtures.remove(key);
        return Collections.singletonList(retval);
    }

    /**
     * Compare two fortresses based on whether they are owned by the player
     * for whom the report is being produced.
     */
    private int compareOwners(final IFortress one, final IFortress two) {
        if (player.equals(one.owner()) && !player.equals(two.owner())) {
            return -1;
        } else if (player.equals(two.owner()) && !player.equals(one.owner())) {
            return 1;
        } else {
            return 0;
        }
    }

    /**
     * Compare two fortresses' names, with a special case so HQ goes at the top.
     */
    private static int compareNames(final IFortress one, final IFortress two) {
        if ("HQ".equals(one.getName()) && !"HQ".equals(two.getName())) {
            return -1;
        } else if ("HQ".equals(two.getName()) && !"HQ".equals(one.getName())) {
            return 1;
        } else {
            return one.getName().compareTo(two.getName());
        }
    }

    /**
     * Compare two Point-IFortress pairs.
     * @return
     */
    @Override
    public Comparator<Pair<Point, IFortress>> comparePairs() {
        return Comparator.<Pair<Point, IFortress>, IFortress>comparing(Pair::getValue1, this::compareOwners)
                .thenComparing(Pair::getValue0, distanceComparator)
                .thenComparing(Pair::getValue1, FortressTabularReportGenerator::compareNames)
                .thenComparing(p -> p.getValue1().owner());
    }
}

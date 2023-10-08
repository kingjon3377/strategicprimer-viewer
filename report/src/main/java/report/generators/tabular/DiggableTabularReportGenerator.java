package report.generators.tabular;

import java.util.List;

import org.javatuples.Pair;
import org.jetbrains.annotations.Nullable;

import lovelace.util.DelayedRemovalMap;

import common.DistanceComparator;
import common.map.IFixture;
import common.map.MapDimensions;
import common.map.Point;
import common.map.fixtures.Ground;
import common.map.fixtures.MineralFixture;
import common.map.fixtures.resources.MineralVein;
import common.map.fixtures.resources.Mine;
import common.map.fixtures.resources.StoneDeposit;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.Collections;

/**
 * A tabular report generator for resources that can be mined---mines, mineral
 * veins, stone deposits, and Ground.
 */
public class DiggableTabularReportGenerator implements ITableGenerator<MineralFixture> {
    @Override
    public boolean canHandle(final IFixture fixture) {
        return fixture instanceof MineralFixture;
    }

    public DiggableTabularReportGenerator(final @Nullable Point hq, final MapDimensions dimensions) {
        this.hq = hq;
        this.dimensions = dimensions;
        if (hq == null) {
            distanceComparator = (one, two) -> 0;
        } else {
            distanceComparator = new DistanceComparator(hq, dimensions);
        }
    }

    private final @Nullable Point hq;

    private final MapDimensions dimensions;

    /**
     * The header row for the table.
     */
    @Override
    public List<String> getHeaderRow() {
        return Arrays.asList("Distance", "Location", "Kind", "Product", "Status");
    }

    /**
     * The file-name to (by default) write this table to.
     */
    @Override
    public String getTableName() {
        return "minerals";
    }

    private final Comparator<Point> distanceComparator;

    /**
     * Create a table row representing a fixture.
     */
    @Override
    public List<List<String>> produce(
            final DelayedRemovalMap<Integer, Pair<Point, IFixture>> fixtures, final MineralFixture item,
            final int key, final Point loc, final Map<Integer, Integer> parentMap) {
        final String classField;
        final String statusField;
        if (item instanceof Ground g) {
            classField = "ground";
            statusField = g.isExposed() ? "exposed" : "not exposed";
        } else if (item instanceof Mine m) {
            classField = "mine";
            statusField = m.getStatus().toString();
        } else if (item instanceof StoneDeposit) {
            classField = "deposit";
            statusField = "exposed";
        } else if (item instanceof MineralVein mv) {
            classField = "vein";
            statusField = mv.isExposed() ? "exposed" : "not exposed";
        } else {
            return Collections.emptyList();
        }
        fixtures.remove(key);
        return Collections.singletonList(Arrays.asList(distanceString(loc, hq, dimensions),
                locationString(loc), classField, item.getKind(), statusField));
    }

    /**
     * Compare two Point-fixture pairs.
     */
    @Override
    public Comparator<Pair<Point, MineralFixture>> comparePairs() {
        return Comparator.<Pair<Point, MineralFixture>, String>comparing(p -> p.getValue1().getKind())
                .thenComparing(Pair::getValue0, distanceComparator)
                .thenComparing(p -> p.getValue1().hashCode());
    }
}
